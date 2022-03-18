package com.bwell.infrastructure;

import com.bwell.services.domain.ResourceLoader;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * This class loads a FHIR resource from a file or a string
 */
public class FileManager {

    /**
     * Reads a FHIR resource from a file
     *
     * @param path: path to file
     * @return IBaseBundle
     */
    @Nullable
    public static IBaseBundle loadResourceFromFile(String path) throws IOException {
        File f = new File(path);
        String resource = null;
        try {
            resource = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResourceLoader().loadResourceFromString(resource);
    }
}
