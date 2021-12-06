package com.bwell.common;

public class LibraryParameter {
    public String libraryUrl;
    public String libraryName;
    public String libraryVersion;
    public String terminologyUrl;
    public ModelParameter model;
    public ContextParameter context;

    public static class ContextParameter {

        public String contextName;

        public String contextValue;

    }

    public static class ModelParameter {

        public String modelName;

        public String modelUrl;

        public String modelBundle;
    }
}
