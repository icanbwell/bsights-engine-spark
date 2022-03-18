package com.bwell.services.domain;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
    public IBaseBundle loadResourceFromString(String resourceJson) throws IOException {
        JsonParser parser = new JsonParser();
        IBaseBundle bundle;
        try {
            Resource resource = parser.parse(resourceJson);
            ResourceType resourceType = resource.getResourceType();
            if (resourceType != ResourceType.Bundle) {
                if (resourceJson.contains("contained")) {
                    // get the contained array
                    List<Resource> contained = ((Patient) resource).getContained();

                    resourceJson = resourceJson.trim();

                    // separate contained resources
                    String separatedRawResources = separateContainedResources(resourceJson, contained.size());

                    // bundle separated resources
                    String separatedResourcesBundleJson = bundleSeparateResourcesJson(separatedRawResources);
                    Resource r = parser.parse(separatedResourcesBundleJson);
                    bundle = (IBaseBundle) r;
                } else {
                    // wrap in a bundle
                    resourceJson = "{\"resourceType\":\"Bundle\", \"id\":\"" + resource.getId() + "\", \"entry\":[" + resourceJson + "]}";
                    bundle = (IBaseBundle) parser.parse(resourceJson);
                }
            } else {
                bundle = (IBaseBundle) resource;
            }
        } catch (IOException e) {
            myLogger.error("Error parsing {}: {}", resourceJson, e.toString());
            throw e;
        }
        myLogger.debug("Read resources from {}: {}", resourceJson, bundle);

        return bundle;
    }

    private String separateContainedResources(String rawContainedJson, int numberOfContainedResources) {
        int[] indexLocations = new int[numberOfContainedResources];
        int count = 0;

        String token = "{\"resourceType\":";
        int index = rawContainedJson.indexOf(token);
        while (index >= 0) {
            index = rawContainedJson.indexOf(token, index + 1);
            if (index != -1)
                indexLocations[count++] = index;
        }

        String resourceStr;
        String separatedContainedResourcesJson = rawContainedJson;
        for (int i = 0; i < indexLocations.length; i++) {
            if (i + 1 <= indexLocations.length - 1) {
                resourceStr = rawContainedJson.substring(indexLocations[i], indexLocations[i + 1]);
                // trim
                resourceStr = resourceStr.trim();
                // remove comma, if exists
                if (resourceStr.charAt(resourceStr.length() - 1) == ',') {
                    resourceStr = resourceStr.substring(0, resourceStr.length() - 1);
                }
            } else {
                resourceStr = rawContainedJson.substring(indexLocations[i], rawContainedJson.length() - 2);
                // trim
                resourceStr = resourceStr.trim();
            }

            separatedContainedResourcesJson += ("\r\n" + resourceStr);
        }

        return separatedContainedResourcesJson;
    }

    private String bundleSeparateResourcesJson(String rawSeparatedJson) {
        // the JSON string from the FhirTextReader in the CQL pipeline has the separated resources,
        String separatedResourcesBundleJson = "";
        String[] lines = rawSeparatedJson.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            // wrap in a resource
            separatedResourcesBundleJson += ((i != 0 ? "," : "") + "{\"resource\":" + lines[i] + "}");
        }

        // wrap in a bundle
        separatedResourcesBundleJson = "{\"resourceType\":\"Bundle\", \"entry\":[" + separatedResourcesBundleJson + "]}";

        return separatedResourcesBundleJson;
    }
}
