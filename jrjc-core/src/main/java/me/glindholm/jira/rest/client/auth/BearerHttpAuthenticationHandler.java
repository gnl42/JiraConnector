package me.glindholm.jira.rest.client.auth;

import com.atlassian.httpclient.api.Request.Builder;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

public class BearerHttpAuthenticationHandler implements AuthenticationHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final String token;

    public BearerHttpAuthenticationHandler(final String token) {
        this.token = token;
    }

    @Override
    public void configure(Builder builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "Bearer " + token);
        builder.setHeader("X-client", "jrjc");
    }
}