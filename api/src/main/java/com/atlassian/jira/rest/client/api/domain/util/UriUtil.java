package com.atlassian.jira.rest.client.api.domain.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {

    public static URI path(URI uri, String path) {
        String uriString = uri.toString();
        StringBuilder sb = new StringBuilder(uriString);
        if (!uriString.endsWith("/")) {
            sb.append('/');
        }
        sb.append(path);
        return createUri(sb.toString());
    }

    private static URI createUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
