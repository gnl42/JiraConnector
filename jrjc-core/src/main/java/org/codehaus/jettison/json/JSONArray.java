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
package org.codehaus.jettison.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Compatibility shim replacing Jettison's JSONArray with a Jackson-backed implementation.
 * The API mirrors Jettison's JSONArray so that existing code requires minimal changes.
 */
public class JSONArray {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ArrayNode node;

    /** Creates an empty JSON array. */
    public JSONArray() {
        this.node = MAPPER.createArrayNode();
    }

    /** Creates a JSON array from a JSON string. */
    public JSONArray(final String json) throws JSONException {
        try {
            final JsonNode parsed = MAPPER.readTree(json);
            if (!parsed.isArray()) {
                throw new JSONException("Not a JSON array: " + json);
            }
            this.node = (ArrayNode) parsed;
        } catch (final JsonProcessingException e) {
            throw new JSONException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /** Creates a JSON array from a Collection of values. */
    public JSONArray(final java.util.Collection<?> collection) throws JSONException {
        this.node = MAPPER.createArrayNode();
        if (collection != null) {
            for (final Object item : collection) {
                put(item);
            }
        }
    }

    /** Creates a wrapper around an existing Jackson ArrayNode. */
    public JSONArray(final ArrayNode node) {
        this.node = node;
    }

    /** Returns the underlying Jackson ArrayNode. */
    public ArrayNode getArrayNode() {
        return node;
    }

    // -------------------------------------------------------------------------
    // put methods – all return 'this' for chaining
    // -------------------------------------------------------------------------

    public JSONArray put(final Object value) throws JSONException {
        if (value == null || value == JSONObject.NULL || value instanceof NullNode) {
            node.addNull();
        } else if (value instanceof JSONObject j) {
            node.add(j.getObjectNode());
        } else if (value instanceof JSONArray a) {
            node.add(a.node);
        } else if (value instanceof ObjectNode on) {
            node.add(on);
        } else if (value instanceof ArrayNode an) {
            node.add(an);
        } else if (value instanceof JsonNode jn) {
            node.add(jn);
        } else if (value instanceof String s) {
            node.add(s);
        } else if (value instanceof Integer i) {
            node.add(i);
        } else if (value instanceof Long l) {
            node.add(l);
        } else if (value instanceof Double d) {
            node.add(d);
        } else if (value instanceof Boolean b) {
            node.add(b);
        } else if (value instanceof Number n) {
            node.add(n.doubleValue());
        } else {
            try {
                node.add(MAPPER.valueToTree(value));
            } catch (final IllegalArgumentException e) {
                throw new JSONException("Cannot serialize value: " + e.getMessage(), e);
            }
        }
        return this;
    }

    public JSONArray put(final int value) {
        node.add(value);
        return this;
    }

    public JSONArray put(final long value) {
        node.add(value);
        return this;
    }

    public JSONArray put(final boolean value) {
        node.add(value);
        return this;
    }

    public JSONArray put(final double value) {
        node.add(value);
        return this;
    }

    public JSONArray put(final String value) {
        node.add(value);
        return this;
    }

    // -------------------------------------------------------------------------
    // get methods
    // -------------------------------------------------------------------------

    public Object get(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        return toObject(n);
    }

    public String getString(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        if (n.isNull()) {
            return null;
        }
        return n.asText();
    }

    public int getInt(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        if (!n.isNumber()) {
            throw new JSONException("JSONArray[" + index + "] is not a number.");
        }
        return n.intValue();
    }

    public long getLong(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        if (!n.isNumber()) {
            throw new JSONException("JSONArray[" + index + "] is not a number.");
        }
        return n.longValue();
    }

    public JSONObject getJSONObject(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        if (!n.isObject()) {
            throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
        }
        return new JSONObject((ObjectNode) n);
    }

    public JSONArray getJSONArray(final int index) throws JSONException {
        final JsonNode n = requireNode(index);
        if (!n.isArray()) {
            throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
        }
        return new JSONArray((ArrayNode) n);
    }

    // -------------------------------------------------------------------------
    // opt methods
    // -------------------------------------------------------------------------

    public Object opt(final int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        final JsonNode n = node.get(index);
        return toObject(n);
    }

    public String optString(final int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        final JsonNode n = node.get(index);
        if (n == null || n.isNull()) {
            return null;
        }
        return n.asText();
    }

    public JSONObject optJSONObject(final int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        final JsonNode n = node.get(index);
        if (n == null || !n.isObject()) {
            return null;
        }
        return new JSONObject((ObjectNode) n);
    }

    public JSONArray optJSONArray(final int index) {
        if (index < 0 || index >= node.size()) {
            return null;
        }
        final JsonNode n = node.get(index);
        if (n == null || !n.isArray()) {
            return null;
        }
        return new JSONArray((ArrayNode) n);
    }

    // -------------------------------------------------------------------------
    // Query / utility methods
    // -------------------------------------------------------------------------

    public int length() {
        return node.size();
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return node.toString();
    }

    public String toString(final int indentFactor) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (final JsonProcessingException e) {
            return node.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private JsonNode requireNode(final int index) throws JSONException {
        if (index < 0 || index >= node.size()) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return node.get(index);
    }

    private static Object toObject(final JsonNode n) {
        if (n == null || n.isNull()) {
            return JSONObject.NULL;
        }
        if (n.isObject()) {
            return new JSONObject((ObjectNode) n);
        }
        if (n.isArray()) {
            return new JSONArray((ArrayNode) n);
        }
        if (n.isBoolean()) {
            return n.booleanValue();
        }
        if (n.isIntegralNumber()) {
            if (n.canConvertToInt()) {
                return n.intValue();
            }
            return n.longValue();
        }
        if (n.isFloatingPointNumber()) {
            return n.doubleValue();
        }
        return n.asText();
    }
}
