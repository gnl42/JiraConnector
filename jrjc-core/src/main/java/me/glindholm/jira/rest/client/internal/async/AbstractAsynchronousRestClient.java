/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.glindholm.jira.rest.client.internal.async;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;
import me.glindholm.jira.rest.client.internal.json.JsonArrayParser;
import me.glindholm.jira.rest.client.internal.json.JsonObjectParser;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.JsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.JsonGenerator;

/**
 * This is a base class for asynchronous REST clients using native Java 21 HttpClient.
 *
 * @since v2.0
 */
public abstract class AbstractAsynchronousRestClient {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final DisposableHttpClient client;

    protected AbstractAsynchronousRestClient(final DisposableHttpClient client) {
        this.client = client;
    }

    protected interface ResponseHandler<T> {
        T handle(int statusCode, String body) throws JsonProcessingException, IOException, URISyntaxException;
    }

    protected final <T> CompletableFuture<T> getAndParse(final URI uri, final JsonParser<?, T> parser) {
        final HttpRequest request = client.newRequest(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        return callAndParse(client.execute(request), parser);
    }

    protected final <I, T> CompletableFuture<T> postAndParse(final URI uri, final I entity, final JsonGenerator<I> jsonGenerator, final JsonObjectParser<T> parser) {
        final String body = toJsonString(jsonGenerator, entity);
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return callAndParse(client.execute(request), parser);
    }

    protected final <T> CompletableFuture<T> postAndParse(final URI uri, final JSONObject entity, final JsonObjectParser<T> parser) {
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(entity.toString(), StandardCharsets.UTF_8))
                .build();
        return callAndParse(client.execute(request), parser);
    }

    protected final CompletableFuture<Void> post(final URI uri, final String entity) {
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(entity, StandardCharsets.UTF_8))
                .build();
        return call(client.execute(request));
    }

    protected final CompletableFuture<Void> post(final URI uri, final JSONObject entity) {
        return post(uri, entity.toString());
    }

    protected final <T> CompletableFuture<Void> post(final URI uri, final T entity, final JsonGenerator<T> jsonGenerator) {
        final String body = toJsonString(jsonGenerator, entity);
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return call(client.execute(request));
    }

    protected final CompletableFuture<Void> post(final URI uri) {
        return post(uri, StringUtils.EMPTY);
    }

    protected final <I, T> CompletableFuture<T> putAndParse(final URI uri, final I entity, final JsonGenerator<I> jsonGenerator, final JsonObjectParser<T> parser) {
        final String body = toJsonString(jsonGenerator, entity);
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return callAndParse(client.execute(request), parser);
    }

    protected final <T> CompletableFuture<Void> put(final URI uri, final T entity, final JsonGenerator<T> jsonGenerator) {
        final String body = toJsonString(jsonGenerator, entity);
        final HttpRequest request = client.newRequest(uri)
                .header("Content-Type", JSON_CONTENT_TYPE)
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return call(client.execute(request));
    }

    protected final CompletableFuture<Void> delete(final URI uri) {
        final HttpRequest request = client.newRequest(uri)
                .DELETE()
                .build();
        return call(client.execute(request));
    }

    protected final <T> CompletableFuture<T> callAndParse(final CompletableFuture<HttpResponse<String>> responseFuture, final ResponseHandler<T> responseHandler) {
        return new DelegatingPromise<>(responseFuture.thenApply(response -> {
            try {
                final int status = response.statusCode();
                final String body = response.body();
                if (status >= 200 && status < 300) {
                    return responseHandler.handle(status, body);
                } else {
                    try {
                        final List<ErrorCollection> errorMessages = extractErrors(status, body);
                        throw new RestClientException(errorMessages, status);
                    } catch (final JsonProcessingException e) {
                        throw new RestClientException(e, status);
                    }
                }
            } catch (final RestClientException e) {
                throw e;
            } catch (final IOException | URISyntaxException e) {
                throw new RestClientException(e);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    protected final <T> CompletableFuture<T> callAndParse(final CompletableFuture<HttpResponse<String>> responseFuture, final JsonParser<?, T> parser) {
        final ResponseHandler<T> responseHandler = (statusCode, body) -> parser instanceof JsonObjectParser
                ? ((JsonObjectParser<T>) parser).parse(new JSONObject(body))
                : ((JsonArrayParser<T>) parser).parse(new JSONArray(body));
        return callAndParse(responseFuture, responseHandler);
    }

    protected final CompletableFuture<Void> call(final CompletableFuture<HttpResponse<String>> responseFuture) {
        return new DelegatingPromise<>(responseFuture.thenApply(response -> {
            final int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return (Void) null;
            } else {
                try {
                    final List<ErrorCollection> errorMessages = extractErrors(status, response.body());
                    throw new RestClientException(errorMessages, status);
                } catch (final JsonProcessingException e) {
                    throw new RestClientException(e, status);
                }
            }
        }));
    }

    protected DisposableHttpClient client() {
        return client;
    }

    public static List<ErrorCollection> extractErrors(final int status, final String body) throws JsonProcessingException {
        if (body == null) {
            return Collections.emptyList();
        }
        final List<ErrorCollection> results = new ArrayList<>();

        if (!body.startsWith("{")) {
            final List<String> msgs = new ArrayList<>(1);
            final String httpMsg = getReasonPhrase(status);
            if (httpMsg != null) {
                msgs.add(httpMsg);
            }
            if (!body.isBlank()) {
                msgs.add(body.strip());
            }
            results.add(new ErrorCollection(status, msgs, Collections.emptyMap()));
        } else {
            final JSONObject jsonObject = new JSONObject(body);
            final JSONArray issues = jsonObject.optJSONArray("issues");
            if (issues != null && issues.length() == 0) {
                final JSONArray errors = jsonObject.optJSONArray("errors");
                for (int i = 0; i < errors.length(); i++) {
                    final JSONObject currentJsonObject = errors.getJSONObject(i);
                    results.add(getErrorsFromJson(currentJsonObject.getInt("status"), currentJsonObject.optJSONObject("elementErrors")));
                }
            } else {
                results.add(getErrorsFromJson(status, jsonObject));
            }
        }
        return List.copyOf(results);
    }

    private static String getReasonPhrase(final int status) {
        // Common HTTP reason phrases
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> null;
        };
    }

    private static ErrorCollection getErrorsFromJson(final int status, final JSONObject jsonObject) throws JsonProcessingException {
        final JSONObject jsonErrors = jsonObject.optJSONObject("errors");
        final List<String> errorMessages;
        final Map<String, String> errors;
        if (jsonErrors == null) {
            final String error = jsonObject.optString("error", null);
            if (error == null) {
                return new ErrorCollection(status, Collections.emptyList(), Collections.emptyMap());
            }
            errorMessages = new ArrayList<>();
            errorMessages.add(error);
            errors = Collections.emptyMap();
        } else {
            final JSONArray jsonErrorMessages = jsonObject.optJSONArray("errorMessages");

            if (jsonErrorMessages != null) {
                errorMessages = JsonParseUtil.toStringList(jsonErrorMessages);
            } else {
                errorMessages = Collections.emptyList();
            }

            if (jsonErrors != null && jsonErrors.length() > 0) {
                errors = JsonParseUtil.toStringMap(jsonErrors.names(), jsonErrors);
            } else {
                errors = Collections.emptyMap();
            }
        }
        return new ErrorCollection(status, errorMessages, errors);
    }

    private <T> String toJsonString(final JsonGenerator<T> generator, final T bean) {
        try {
            return generator.generate(bean).toString();
        } catch (final JsonProcessingException e) {
            throw new RestClientException(e);
        }
    }
}
