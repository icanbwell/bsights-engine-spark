package com.bwell.services.domain;

import ca.uhn.fhir.context.FhirVersionEnum;
import com.bwell.fhir.exception.FhirException;
import com.bwell.fhir.model.SupportedResourceType;
import com.bwell.fhir.parser.Parser;
import com.bwell.infrastructure.FhirJsonExporter;
import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class loads a FHIR resource from a file or a string
 */
public class ResourceLoader {

    private static final org.slf4j.Logger myLogger = org.slf4j.LoggerFactory.getLogger(ResourceLoader.class);
    /*
      Reads a FHIR resource from a file

      @param path: path to file
     * @return IBaseBundle
     */
/*¬
    @Nullable
    public static IBaseBundle loadResourceFromFile(String path) {
        File f = new File(path);
        String resource = null;
        try {
            resource = FileUtils.readFileToString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadResourceFromString(resource);
    }
*/

    /**
     * Load a FHIR resource from a string
     *
     * @param resourceJson: resource as a string
     * @return IBaseBundle
     */
    @Nullable
    public IBaseBundle loadResourceFromString(String fhirVersion, String resourceJson) throws IOException {
        try {
            Resource baseResource;
            try {
                baseResource = (Resource) FhirJsonExporter.getResourceFromJson(fhirVersion, resourceJson);
            }catch (Exception e) {
                /*
                    The Resource failed to parse on its own - check to see
                    if we support the ResourceType and have special parsing
                 */
                //Parse the FHIR String
                JsonNode rootNode = Parser.readTree(resourceJson);

                //Grab the Contained off of it
                JsonNode node = Parser.findJsonNode(rootNode, Parser.RESOURCE_TYPE);

                //Check if we support this ResourceType and try to parse it
                com.bwell.fhir.resource.BaseResource supportedResource = SupportedResourceType.getNewResource(node.asText());
                if(supportedResource == null){
                    //We do not support this ResourceType
                    throw e;
                }

                //We support the ResourceType, so let's parse
                com.bwell.fhir.resource.BaseResource resource = supportedResource.parse(FhirVersionEnum.forVersionString(fhirVersion), resourceJson);

                baseResource = (Resource) resource.getBaseResource();
            }

            ResourceType resourceType = baseResource.getResourceType();
            IBaseBundle bundle;
            if (resourceType != ResourceType.Bundle) {
                bundle = new Bundle();
                bundle.setId(baseResource.getIdElement().getValue());

                List<Bundle.BundleEntryComponent> newEntries = new ArrayList<>();
                Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
                entryComponent.setResource(baseResource);

                newEntries.add(entryComponent);
                ((Bundle) bundle).setEntry(newEntries);
            } else {
                bundle = (Bundle) baseResource;
            }

            String resourceId = null;
            List<Bundle.BundleEntryComponent> entries = ((Bundle)bundle).getEntry();
            if (entries.size() > 0) {
                resourceId = entries.get(0).getResource().getIdElement().getIdPart();
            }

            myLogger.info("Read resources for resource Id: {}", resourceId);
            bundle = moveContainedResourcesToTopLevel(bundle);

            //noinspection ConstantConditions
            bundle = cleanFixBundle(bundle);
            myLogger.info("Cleaned resources for resource Id: {}", resourceId);

            return bundle;
        } catch (FHIRFormatError | FhirException ex) {
            myLogger.error("Bad FHIR data {}: {}", resourceJson, ex.toString());
            return new Bundle(); // bad FHIR.  log error and continue processing other records
        } catch (IOException ex) {
            myLogger.error("Error parsing {}: {}", resourceJson, ex.toString());
            throw ex;
        }
    }

    private IBaseBundle moveContainedResourcesToTopLevel(IBaseBundle bundle) {
        if (bundle instanceof Bundle) {
            List<Bundle.BundleEntryComponent> newEntries = new ArrayList<>();

            List<Bundle.BundleEntryComponent> entries = ((Bundle) bundle).getEntry();
            for (Bundle.BundleEntryComponent entry : entries) {
                Resource resource = entry.getResource();

                if (resource instanceof DomainResource) {
                    List<Resource> contained = ((DomainResource) resource).getContained();

                    if (contained.size() > 0) {
                        for (Resource containedResource : contained) {
                            Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
                            entryComponent.setResource(containedResource);
                            newEntries.add(entryComponent);
                        }
                        contained.clear();
                    }
                }
            }
            entries.addAll(newEntries);
        }
        return bundle;
    }

    private IBaseBundle cleanFixBundle(IBaseBundle bundle) {
        // some data we get is bad FHIR ,so we have to fix it
        return bundle;
    }
}
