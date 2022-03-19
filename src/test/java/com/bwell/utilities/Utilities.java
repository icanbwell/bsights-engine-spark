package com.bwell.utilities;

import org.apache.commons.io.FileUtils;
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
}
