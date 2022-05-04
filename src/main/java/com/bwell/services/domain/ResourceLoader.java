package com.bwell.services.domain;

import com.bwell.infrastructure.FhirJsonExporter;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.*;
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
        JsonParser parser = new JsonParser();

        try {
            Resource resource = parser.parse(resourceJson);
            ResourceType resourceType = resource.getResourceType();
            IBaseBundle bundle;

            if (resourceType != ResourceType.Bundle) {
                bundle = new Bundle();
                bundle.setId(resource.getId());

                List<Bundle.BundleEntryComponent> newEntries = new ArrayList<>();
                Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
                entryComponent.setResource(resource);

                newEntries.add(entryComponent);
                ((Bundle) bundle).setEntry(newEntries);
            } else {
                bundle = (Bundle) resource;
            }

            myLogger.info("Read resources from {}: {}", resourceJson, FhirJsonExporter.getResourceAsJson(fhirVersion, bundle));
            bundle = moveContainedResourcesToTopLevel(bundle);

            //noinspection ConstantConditions
            bundle = clean_and_fix_bundle(bundle);
            myLogger.info("Cleaned resources from {}: {}", resourceJson, FhirJsonExporter.getResourceAsJson(fhirVersion, bundle));

            return bundle;
        } catch (FHIRFormatError ex) {
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

    private IBaseBundle clean_and_fix_bundle(IBaseBundle bundle) {
        // some data we get is bad FHIR ,so we have to fix it
        return bundle;
    }
}
