package com.bwell.common;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.formats.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResourceLoader {
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

    @Nullable
    public static IBaseBundle loadResourceFromString(String resource) {
        JsonParser parser = new JsonParser();
        IBaseBundle bundle = null;
        try {
            bundle = (IBaseBundle) parser.parse(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }
}
