package com.bwell.common;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This class loads a FHIR resource from a file or a string
 */
public class ResourceLoader {
    /**
     * Reads a FHIR resource from a file
     *
     * @param path: path to file
     * @return IBaseBundle
     */
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

    /**
     * Load a FHIR resource from a string
     *
     * @param resourceJson: resource as a string
     * @return IBaseBundle
     */
    @Nullable
    public static IBaseBundle loadResourceFromString(String resourceJson) {
        JsonParser parser = new JsonParser();
        IBaseBundle bundle = null;
        try {
            Resource resource = parser.parse(resourceJson);
            ResourceType resourceType = resource.getResourceType();
            if (resourceType != ResourceType.Bundle) {
                // wrap in a bundle
                resourceJson = "{\"resourceType\":\"Bundle\", \"id\":\"" + resource.getId() + "\", \"entry\":[" + "{\"resource\":" + resourceJson + "}" + "]}";
                bundle = (IBaseBundle) parser.parse(resourceJson);
            }
            else {
                bundle = (IBaseBundle) resource;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }
}
