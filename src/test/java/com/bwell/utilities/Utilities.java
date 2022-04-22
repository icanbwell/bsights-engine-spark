package com.bwell.utilities;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Utilities {

    public static String getBundle(String testResourcePath, String folder) throws JSONException {
        return getBundleJsonString(testResourcePath, folder, "expected.json");
    }

    public static String getContainedBundle(String testResourcePath, String folder) throws JSONException {
        return getBundleJsonString(testResourcePath, folder, "expected_contained.json");
    }

    private static String getBundleJsonString(String testResourcePath, String folder, String filename) throws JSONException {
        String bundleJson;

        bundleJson = getRawJson(testResourcePath, folder, filename);

        if (bundleJson.stripLeading().startsWith("[")) {
            JSONArray jsonArray = new JSONArray(bundleJson);
            JSONObject firstItem = (JSONObject) jsonArray.get(0);
            bundleJson = firstItem.getJSONObject("bundle").toString();
        }
        return bundleJson;
    }

    public static String getRawJson(String testResourcePath, String folder, String filename) {
        String rawJson = null;

        String path = testResourcePath + "/" + folder + "/bundles/" + filename;
        System.out.printf("Reading file: %s%n", path);

        File f = new File(path);
        try {
            rawJson = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rawJson;
    }

    public static String separateResourcesFromContainedJson(String rawContainedJson) throws JSONException {
        UUID uuid = UUID.randomUUID();
        String bundleContainedJson = "[{\"bundle\": "
                + "{\"resourceType\":\"Bundle\", \"id\":\""
                + uuid
                + "\", \"entry\":[" + rawContainedJson + "]}"
                + "}]";

        JSONArray jsonArray = new JSONArray(bundleContainedJson);
        JSONObject firstBundle = (JSONObject) jsonArray.get(0);

        JSONObject convertedJsonObj = convertContainedBundleToNormalBundle(firstBundle);
        return convertedJsonObj.toString();
    }

    public static JSONObject convertContainedBundleToNormalBundle(JSONObject jsonBundleObject) throws JSONException {
        JSONArray entryArray = jsonBundleObject.getJSONArray("entry");
        JSONObject firstElement = (JSONObject) entryArray.get(0);

        JSONObject firstResource = firstElement.getJSONObject("resource");
        if (firstResource != null) {
            JSONArray containedArray = firstResource.getJSONArray("contained");
            if (containedArray.length() > 0) {
                for(int i=0; i<containedArray.length(); i++) {
                    JSONObject resourceObj = containedArray.getJSONObject(i);
                    jsonBundleObject
                            .getJSONArray("entry")
                            .put(new JSONObject("{\"resource\":" + resourceObj.toString() + "}"));
                }
            }
        }

        ((JSONObject) jsonBundleObject.getJSONArray("entry").get(0))
                .getJSONObject("resource")
                .remove("contained");

        System.out.println(jsonBundleObject);

        return jsonBundleObject;
    }

    public static String tempConvert(Object value) {
        if (value == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder();
        if (value instanceof Iterable) {
            result.append("[");
            Iterable<?> values = (Iterable<?>) value;
            for (Object o : values) {

                result.append(tempConvert(o)).append(", ");
            }

            if (result.length() > 1) {
                result = new StringBuilder(result.substring(0, result.length() - 2));
            }

            result.append("]");
        } else if (value instanceof IBaseResource) {
            IBaseResource resource = (IBaseResource) value;
            result = new StringBuilder(resource.fhirType() + (resource.getIdElement() != null && resource.getIdElement().hasIdPart()
                    ? "(id=" + resource.getIdElement().getIdPart() + ")"
                    : ""));
        } else if (value instanceof IBase) {
            result = new StringBuilder(((IBase) value).fhirType());
        } else //noinspection ConstantConditions
            if (value instanceof IBaseDatatype) {
                result = new StringBuilder(((IBaseDatatype) value).fhirType());
            } else {
                result = new StringBuilder(value.toString());
            }

        return result.toString();
    }
}
