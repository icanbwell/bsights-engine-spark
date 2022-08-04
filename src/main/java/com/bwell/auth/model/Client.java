package com.bwell.auth.model;

public class Client {
    private String authServerUrl;
    private String id;
    private String secret;
    private String authScopes;

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAuthScopes() {
        return authScopes;
    }

    public void setAuthScopes(String authScopes) {
        this.authScopes = authScopes;
    }
}
