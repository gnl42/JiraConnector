/*
 * Copyright (C) 2024 George Lindholm
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
package me.glindholm.jira.rest.client.shim.jettison.json;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Compatibility shim: JSONException backed by Jackson's JsonProcessingException.
 * Extends JsonProcessingException so callers can catch either this class or the
 * Jackson supertype. For parsing errors use JsonMappingException; for generation
 * errors use JsonGenerationException directly.
 */
public class JSONException extends JsonProcessingException {

    private static final long serialVersionUID = 1L;

    public JSONException(final String message) {
        super(message, (com.fasterxml.jackson.core.JsonLocation) null);
    }

    public JSONException(final String message, final Throwable cause) {
        super(message, (com.fasterxml.jackson.core.JsonLocation) null, cause);
    }

    public JSONException(final Throwable cause) {
        super(cause == null ? null : cause.getMessage(), (com.fasterxml.jackson.core.JsonLocation) null, cause);
    }
}
