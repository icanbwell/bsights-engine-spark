package com.bwell.utilities;

import com.bwell.auth.model.Client;
import com.bwell.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AuthServiceTest {
    public static void main(String[] args) throws JsonProcessingException {
        AuthService authService = new AuthService();

        Client client = new Client();
        client.setSecret("");
        client.setId("");
        client.setAuthScopes("");
        client.setAuthServerUrl("");

        String token = authService.getToken(client);

        System.out.println(token);
    }
}
