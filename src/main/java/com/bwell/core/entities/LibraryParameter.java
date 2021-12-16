package com.bwell.core.entities;

import java.util.List;

/**
 * This entity class represents a measure library context parameter
 */
public class LibraryParameter {

    /**
     * The URL of the library
     */
    public String libraryUrl;

    /**
     * The name of the library
     */
    public String libraryName;

    /**
     * The version of the library
     */
    public String libraryVersion;

    /**
     * headers for library url
     */
    public List<String> libraryUrlHeaders;
    /**
     * The URL of the terminology service
     */
    public String terminologyUrl;
    /**
     * headers for terminology url
     */
    public List<String> terminologyUrlHeaders;

    /**
     * The library model parameter
     */
    public ModelParameter model;

    /**
     * The library context parameter
     */
    public ContextParameter context;
}
