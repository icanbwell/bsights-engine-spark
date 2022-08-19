package com.bwell.auth.service;

import com.bwell.auth.model.Client;
import com.bwell.auth.model.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);
    private static final String SCOPE = "scope";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final RestTemplate restTemplate;
    private static final ObjectMapper objectMapper;
    private static final PassiveExpiringMap<String, Token> cache;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        cache = new PassiveExpiringMap<>(60, TimeUnit.MINUTES);

        restTemplate = new RestTemplate();
    }

    public String getToken(Client client) throws JsonProcessingException {
        //Only grab new token if Cache is empty
        if(cache.isEmpty()){
            //Log Request
            log.info("Requesting Auth token: client_id={} URL={} scope={} grant_type={}", client.getId(), client.getAuthServerUrl(), client.getAuthScopes(), CLIENT_CREDENTIALS);

            //Set Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(Base64.getUrlEncoder().encodeToString(String.format("%s:%s", client.getId(), client.getSecret()).getBytes()));
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            //Set Params
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.put(GRANT_TYPE, Collections.singletonList(CLIENT_CREDENTIALS));
            map.put(SCOPE, Collections.singletonList(client.getAuthScopes()));

            //Set Request
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            //Make call
            ResponseEntity<String> response = restTemplate.postForEntity(client.getAuthServerUrl(), request, String.class);

            //Parse Token
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Token token = new Token();
            token.setAccessToken(jsonNode.get("access_token").asText());
            token.setExpiresIn(jsonNode.get("expires_in").asText());

            //Add to cache
            cache.put(token.getTokenType(), token);
        }

        /*
            Since there is only 1 token at the moment, with all access,
            there will only be 1 token in the map, and we can just get
            first value
         */
        return cache.values().stream().findFirst().get().getAccessToken();
    }
}
