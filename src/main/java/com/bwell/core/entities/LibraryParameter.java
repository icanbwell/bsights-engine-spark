package com.bwell.core.entities;

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
     * The URL of the terminology service
     */
    public String terminologyUrl;

    /**
     * The library model parameter
     */
    public ModelParameter model;

    /**
     * The library context parameter
     */
    public ContextParameter context;
}
