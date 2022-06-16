/*
 * Copyright (C) 2010 Atlassian
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

package me.glindholm.jira.rest.client.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;

/**
 * Basic exception which may be thrown by any remote operation encapsulated by the REST me.glindholm.jira.rest.client.api.
 * Usually some more specific exception will be chained here and available via {@link #getCause()}
 *
 * @since v0.1
 */
public class RestClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Optional<Integer> statusCode;
    private final List<ErrorCollection> ErrorCollections;

    public RestClientException(final RestClientException exception) {
        super(exception.getMessage(), exception);
        this.statusCode = exception.getStatusCode();
        this.ErrorCollections = exception.ErrorCollections;
    }

    public RestClientException(final Throwable cause) {
        super(cause);
        this.ErrorCollections = Collections.emptyList();
        this.statusCode = Optional.empty();
    }

    public RestClientException(final Throwable cause, final int statusCode) {
        super(cause);
        this.ErrorCollections = Collections.emptyList();
        this.statusCode = Optional.of(statusCode);
    }

    public RestClientException(final String errorMessage, final Throwable cause) {
        super(errorMessage, cause);
        this.ErrorCollections = List.of(new ErrorCollection(errorMessage));
        statusCode = Optional.empty();
    }

    public RestClientException(final List<ErrorCollection> ErrorCollections, final int statusCode) {
        super(ErrorCollections.toString());
        this.ErrorCollections = List.copyOf(ErrorCollections);
        this.statusCode = Optional.of(statusCode);
    }

    public RestClientException(final List<ErrorCollection> ErrorCollections, final Throwable cause, final int statusCode) {
        super(ErrorCollections.toString(), cause);
        this.ErrorCollections = List.copyOf(ErrorCollections);
        this.statusCode = Optional.of(statusCode);
    }

    /**
     * @return error messages used while building this exception object
     */
    public List<ErrorCollection> getErrorCollections() {
        return ErrorCollections;
    }

    /**
     * @return optional error code of failed http request.
     */
    public Optional<Integer> getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "RestClientException{" +
                "statusCode=" + statusCode +
                ", ErrorCollections=" + ErrorCollections +
                '}';
    }
}
