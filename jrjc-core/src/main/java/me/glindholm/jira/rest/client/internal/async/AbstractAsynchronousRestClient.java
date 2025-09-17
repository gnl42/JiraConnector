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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import com.atlassian.httpclient.api.DefaultResponseTransformation;
import com.atlassian.httpclient.api.EntityBuilder;
import com.atlassian.httpclient.api.EntityBuilder.Entity;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;

import io.atlassian.util.concurrent.Promise;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;
import me.glindholm.jira.rest.client.internal.json.JsonArrayParser;
import me.glindholm.jira.rest.client.internal.json.JsonObjectParser;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.JsonParser;
import me.glindholm.jira.rest.client.internal.json.gen.JsonGenerator;

/**
 * This is a base class for asynchronous REST clients.
 *
 * @since v2.0
 */
public abstract class AbstractAsynchronousRestClient {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final HttpClient client;

    protected AbstractAsynchronousRestClient(final HttpClient client) {
        this.client = client;
    }

    protected interface ResponseHandler<T> {
        T handle(Response request) throws JSONException, IOException, URISyntaxException;
    }

    protected final <T> Promise<T> getAndParse(final URI uri, final JsonParser<?, T> parser) {
        return callAndParse(client.newRequest(uri).setAccept("application/json").get(), parser);
    }

    protected final <I, T> Promise<T> postAndParse(final URI uri, final I entity, final JsonGenerator<I> jsonGenerator, final JsonObjectParser<T> parser) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(toEntity(jsonGenerator, entity)).post();
        return callAndParse(responsePromise, parser);
    }

    protected final <T> Promise<T> postAndParse(final URI uri, final JSONObject entity, final JsonObjectParser<T> parser) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(entity.toString()).setContentType(JSON_CONTENT_TYPE).post();
        return callAndParse(responsePromise, parser);
    }

    protected final Promise<Void> post(final URI uri, final String entity) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(entity).setContentType(JSON_CONTENT_TYPE).post();
        return call(responsePromise);
    }

    protected final Promise<Void> post(final URI uri, final JSONObject entity) {
        return post(uri, entity.toString());
    }

    protected final <T> Promise<Void> post(final URI uri, final T entity, final JsonGenerator<T> jsonGenerator) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(toEntity(jsonGenerator, entity)).post();
        return call(responsePromise);
    }

    protected final Promise<Void> post(final URI uri) {
        return post(uri, StringUtils.EMPTY);
    }

    protected final <I, T> Promise<T> putAndParse(final URI uri, final I entity, final JsonGenerator<I> jsonGenerator, final JsonObjectParser<T> parser) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(toEntity(jsonGenerator, entity)).put();
        return callAndParse(responsePromise, parser);
    }

    protected final <T> Promise<Void> put(final URI uri, final T entity, final JsonGenerator<T> jsonGenerator) {
        final ResponsePromise responsePromise = client.newRequest(uri).setEntity(toEntity(jsonGenerator, entity)).put();
        return call(responsePromise);
    }

    protected final Promise<Void> delete(final URI uri) {
        final ResponsePromise responsePromise = client.newRequest(uri).delete();
        return call(responsePromise);
    }

    protected final <T> Promise<T> callAndParse(final ResponsePromise responsePromise, final ResponseHandler<T> responseHandler) {
        final Function<Response, T> transformFunction = toFunction(responseHandler);
        final ResponseTransformation<Object> responseTransformation = DefaultResponseTransformation.builder().ok(transformFunction).created(transformFunction)
                .others(AbstractAsynchronousRestClient.errorFunction()).build();
        return new DelegatingPromise(responsePromise.transform(responseTransformation));
    }

    @SuppressWarnings("unchecked")
    protected final <T> Promise<T> callAndParse(final ResponsePromise responsePromise, final JsonParser<?, T> parser) {
        final ResponseHandler<T> responseHandler = response -> {
            final String body = response.getEntity();
            return parser instanceof JsonObjectParser ? ((JsonObjectParser<T>) parser).parse(new JSONObject(body))
                    : ((JsonArrayParser<T>) parser).parse(new JSONArray(body));
        };
        return callAndParse(responsePromise, responseHandler);
    }

    protected final Promise<Void> call(final ResponsePromise responsePromise) {
        final ResponseTransformation<Object> responseTransformation = DefaultResponseTransformation.builder().ok(constant((Void) null))
                .created(constant((Void) null)).noContent(constant((Void) null)).others(AbstractAsynchronousRestClient.errorFunction()).build();
        return new DelegatingPromise(responsePromise.transform(responseTransformation));
    }

    protected HttpClient client() {
        return client;
    }

    private static <T> Function<Response, T> errorFunction() {
        return response -> {
            try {
                final String body = response.getEntity();
                final List<ErrorCollection> errorMessages = extractErrors(response.getStatusCode(), body);
                throw new RestClientException(errorMessages, response.getStatusCode());
            } catch (final JSONException e) {
                throw new RestClientException(e, response.getStatusCode());
            }
        };
    }

    private static <T> Function<Response, T> toFunction(final ResponseHandler<T> responseHandler) {
        return (@Nullable final Response input) -> {
            try {
                return responseHandler.handle(input);
            } catch (JSONException | IOException | URISyntaxException e) {
                throw new RestClientException(e);
            }
        };
    }

    private static <T> Function<Response, T> constant(final T value) {
        return input -> value;
    }

    public static List<ErrorCollection> extractErrors(final int status, final String body) throws JSONException {
        if (body == null) {
            return Collections.emptyList();
        }
        final List<ErrorCollection> results = new ArrayList<>();

        if (!body.startsWith("{")) {
            final List<String> msgs = new ArrayList<>(1);
            final String httpMsg = EnglishReasonPhraseCatalog.INSTANCE.getReason(status, null);
            if (httpMsg != null) {
                msgs.add(httpMsg);
            }
            msgs.add(body.strip());
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

	private static ErrorCollection getErrorsFromJson(final int status, final JSONObject jsonObject)
			throws JSONException {
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

    private <T> EntityBuilder toEntity(final JsonGenerator<T> generator, final T bean) {
        return () -> new Entity() {
            @Override
            public Map<String, String> getHeaders() {
                return Collections.singletonMap("Content-Type", JSON_CONTENT_TYPE);
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return new ByteArrayInputStream(generator.generate(bean).toString().getBytes(Charset.forName("UTF-8")));
                } catch (final JSONException e) {
                    throw new RestClientException(e);
                }
            }
        };
    }

}
