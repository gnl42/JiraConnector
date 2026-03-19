package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal URI builder replacing {@code org.apache.hc.core5.net.URIBuilder}.
 */
public class UriBuilder {

    private final URI base;
    private final List<String> pathSegments = new ArrayList<>();
    private final List<String[]> queryParams = new ArrayList<>();

    public UriBuilder(final URI base) {
        this.base = base;
    }

    public UriBuilder appendPath(final String path) {
        if (path != null && !path.isEmpty()) {
            pathSegments.add(path.startsWith("/") ? path.substring(1) : path);
        }
        return this;
    }

    public UriBuilder addParameter(final String name, final String value) {
        queryParams.add(new String[]{name, value});
        return this;
    }

    public URI build() throws URISyntaxException {
        final String rawBase = base.toString();
        final StringBuilder sb = new StringBuilder();

        // strip existing query from base
        final int qIdx = rawBase.indexOf('?');
        final String basePath = qIdx >= 0 ? rawBase.substring(0, qIdx) : rawBase;
        final String existingQuery = qIdx >= 0 ? rawBase.substring(qIdx + 1) : null;

        sb.append(basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath);

        for (final String segment : pathSegments) {
            sb.append('/');
            sb.append(segment);
        }

        final List<String> parts = new ArrayList<>();
        if (existingQuery != null && !existingQuery.isEmpty()) {
            parts.add(existingQuery);
        }
        for (final String[] param : queryParams) {
            parts.add(enc(param[0]) + "=" + enc(param[1]));
        }
        if (!parts.isEmpty()) {
            sb.append('?');
            sb.append(String.join("&", parts));
        }
        return new URI(sb.toString());
    }

    private static String enc(final String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
