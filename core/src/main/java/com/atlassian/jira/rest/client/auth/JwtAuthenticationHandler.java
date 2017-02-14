package com.atlassian.jira.rest.client.auth;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriterFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationHandler implements AuthenticationHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private String key;
    private String secret;

    public JwtAuthenticationHandler(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    @Override
    public void configure(Request.Builder builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "JWT " + createJwtToken(builder.build()));
    }

    private String createJwtToken(final Request request)  {

        try {

            long issuedAt = System.currentTimeMillis() / 1000L;
            long expiresAt = issuedAt + 180L;

            final String method = (request.getMethod() != null) ? request.getMethod().name().toUpperCase() : "GET";
            final String path = request.getUri().getPath();
            final String contextPath = "/";

            final List<NameValuePair> params = URLEncodedUtils.parse(new URI(request.getUri().toString()), "UTF-8");
            final Map<String, String[]> paramMap = new HashMap<>();
            params.forEach(nameValuePair -> {
                final String name = nameValuePair.getName();
                final String value = nameValuePair.getValue();
                paramMap.compute(name, (k, v) -> {
                    if (v == null) {
                        return new String[]{value};
                    }
                    else {
                        final String[] array = Arrays.copyOf(v, v.length+1);
                        array[v.length+1] = value;
                        return array;
                    }
                });
            });

            final JwtJsonBuilder jwtBuilder = new JsonSmartJwtJsonBuilder()
                    .issuedAt(issuedAt)
                    .expirationTime(expiresAt)
                    .issuer(key);

            final CanonicalHttpUriRequest canonical = new CanonicalHttpUriRequest(method, path, contextPath, paramMap);

            JwtClaimsBuilder.appendHttpRequestClaims(jwtBuilder, canonical);
            final JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
            final String jwtbuilt = jwtBuilder.build();
            return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, secret).jsonToJwt(jwtbuilt);
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

}
