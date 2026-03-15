package me.glindholm.jira.rest.client.auth;

import java.net.http.HttpRequest;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

public class BearerHttpAuthenticationHandler implements AuthenticationHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final String token;

    public BearerHttpAuthenticationHandler(final String token) {
        this.token = token;
    }

    @Override
    public void configure(final HttpRequest.Builder builder) {
        builder.header(AUTHORIZATION_HEADER, "Bearer " + token);
        builder.header("X-client", "jrjc");
    }
}