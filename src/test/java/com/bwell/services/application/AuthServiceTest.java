package com.bwell.services.application;

import com.bwell.auth.model.Client;
import com.bwell.auth.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.testng.Assert.assertNotNull;

public class AuthServiceTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    // Staging Env values
    private final String authServerUrl = "https://staging-icanbwell.auth.us-east-1.amazoncognito.com/oauth2/token";
    private final String authScopes = "user/*.read access/*.* user/*.write";
    private final String clientId = "6g2mb917f8m0h3nfk5bcr1d01o";
    private final String clientSecret = null;

    @BeforeMethod
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void restoreStreams() {
        String sysOut = outContent.toString();
        String sysError = errContent.toString();

        System.setOut(originalOut);
        System.setErr(originalErr);

        System.out.println(sysOut);
        System.err.println(sysError);
    }

    @Ignore("Need to set 'clientSecret' string value above for the environment specific value")
    @Test
    public void testGetToken() throws JsonProcessingException {
        try {
            AuthService authService = new AuthService();

            Client client = new Client();
            client.setAuthServerUrl(authServerUrl);
            client.setId(clientId);
            client.setSecret(clientSecret);
            client.setAuthScopes(authScopes);

            String token = authService.getToken(client);
            System.out.println(token);

            assertNotNull(token);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        System.out.println();
    }
}
