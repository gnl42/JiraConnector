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

import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Compatibility shim replacing Jettison's JSONObject with a Jackson-backed implementation.
 * The API mirrors Jettison's JSONObject so that existing code requires minimal changes.
 */
public class JSONObject {

    /** Sentinel for JSON null values (equivalent to Jettison's JSONObject.NULL). */
    public static final Object NULL = NullNode.getInstance();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectNode node;

    /** Creates an empty JSON object. */
    public JSONObject() {
        this.node = MAPPER.createObjectNode();
    }

    /** Creates a JSON object from a JSON string. */
    public JSONObject(final String json) throws JSONException {
        try {
            final JsonNode parsed = MAPPER.readTree(json);
            if (!parsed.isObject()) {
                throw new JSONException("Not a JSON object: " + json);
            }
            this.node = (ObjectNode) parsed;
        } catch (final JsonProcessingException e) {
            throw new JSONException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /** Creates a wrapper around an existing Jackson ObjectNode. */
    public JSONObject(final ObjectNode node) {
        this.node = node;
    }

    /** Returns the underlying Jackson ObjectNode. */
    public ObjectNode getObjectNode() {
        return node;
    }

    // -------------------------------------------------------------------------
    // put methods – all return 'this' for chaining
    // -------------------------------------------------------------------------

    public JSONObject put(final String key, final String value) throws JSONException {
        if (value == null) {
            node.putNull(key);
        } else {
            node.put(key, value);
        }
        return this;
    }

    public JSONObject put(final String key, final int value) throws JSONException {
        node.put(key, value);
        return this;
    }

    public JSONObject put(final String key, final long value) throws JSONException {
        node.put(key, value);
        return this;
    }

    public JSONObject put(final String key, final boolean value) throws JSONException {
        node.put(key, value);
        return this;
    }

    public JSONObject put(final String key, final double value) throws JSONException {
        node.put(key, value);
        return this;
    }

    public JSONObject put(final String key, final Object value) throws JSONException {
        putValue(key, value);
        return this;
    }

    /** Puts the value only if it is non-null. */
    public JSONObject putOpt(final String key, final Object value) throws JSONException {
        if (value != null) {
            putValue(key, value);
        }
        return this;
    }

    private void putValue(final String key, final Object value) throws JSONException {
        if (value == null || value == NULL || value instanceof NullNode) {
            node.putNull(key);
        } else if (value instanceof JSONObject j) {
            node.set(key, j.node);
        } else if (value instanceof JSONArray a) {
            node.set(key, a.getArrayNode());
        } else if (value instanceof ObjectNode on) {
            node.set(key, on);
        } else if (value instanceof ArrayNode an) {
            node.set(key, an);
        } else if (value instanceof JsonNode jn) {
            node.set(key, jn);
        } else if (value instanceof String s) {
            node.put(key, s);
        } else if (value instanceof Integer i) {
            node.put(key, i);
        } else if (value instanceof Long l) {
            node.put(key, l);
        } else if (value instanceof Double d) {
            node.put(key, d);
        } else if (value instanceof Float f) {
            node.put(key, f);
        } else if (value instanceof Boolean b) {
            node.put(key, b);
        } else if (value instanceof Number n) {
            node.put(key, n.doubleValue());
        } else if (value instanceof Iterable<?> it) {
            // Handle lists / collections
            final ArrayNode array = MAPPER.createArrayNode();
            for (final Object item : it) {
                addToArray(array, item);
            }
            node.set(key, array);
        } else {
            // Fallback: convert via ObjectMapper
            try {
                final JsonNode jn = MAPPER.valueToTree(value);
                node.set(key, jn);
            } catch (final IllegalArgumentException e) {
                throw new JSONException("Cannot serialize value of type " + value.getClass().getName() + ": " + e.getMessage(), e);
            }
        }
    }

    private static void addToArray(final ArrayNode array, final Object item) throws JSONException {
        if (item == null || item == NULL || item instanceof NullNode) {
            array.addNull();
        } else if (item instanceof JSONObject j) {
            array.add(j.node);
        } else if (item instanceof JSONArray a) {
            array.add(a.getArrayNode());
        } else if (item instanceof ObjectNode on) {
            array.add(on);
        } else if (item instanceof ArrayNode an) {
            array.add(an);
        } else if (item instanceof JsonNode jn) {
            array.add(jn);
        } else if (item instanceof String s) {
            array.add(s);
        } else if (item instanceof Integer i) {
            array.add(i);
        } else if (item instanceof Long l) {
            array.add(l);
        } else if (item instanceof Double d) {
            array.add(d);
        } else if (item instanceof Boolean b) {
            array.add(b);
        } else if (item instanceof Number n) {
            array.add(n.doubleValue());
        } else {
            try {
                array.add(MAPPER.valueToTree(item));
            } catch (final IllegalArgumentException e) {
                throw new JSONException("Cannot serialize array item: " + e.getMessage(), e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // get methods – throw JSONException if key missing or wrong type
    // -------------------------------------------------------------------------

    public String getString(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (n.isNull()) {
            return null;
        }
        return n.asText();
    }

    public int getInt(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (n.isNumber()) {
            return n.intValue();
        }
        if (n.isTextual()) {
            try {
                return Integer.parseInt(n.textValue());
            } catch (final NumberFormatException e) {
                // fall through to exception below
            }
        }
        throw new JSONException("JSONObject[\"" + key + "\"] is not a number.");
    }

    public long getLong(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (n.isNumber()) {
            return n.longValue();
        }
        if (n.isTextual()) {
            try {
                return Long.parseLong(n.textValue());
            } catch (final NumberFormatException e) {
                // fall through to exception below
            }
        }
        throw new JSONException("JSONObject[\"" + key + "\"] is not a number.");
    }

    public boolean getBoolean(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (n.isBoolean()) {
            return n.booleanValue();
        }
        final String text = n.asText();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        throw new JSONException("JSONObject[\"" + key + "\"] is not a boolean.");
    }

    public double getDouble(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (n.isNumber()) {
            return n.doubleValue();
        }
        if (n.isTextual()) {
            try {
                return Double.parseDouble(n.textValue());
            } catch (final NumberFormatException e) {
                // fall through to exception below
            }
        }
        throw new JSONException("JSONObject[\"" + key + "\"] is not a number.");
    }

    public JSONObject getJSONObject(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (!n.isObject()) {
            throw new JSONException("JSONObject[\"" + key + "\"] is not a JSONObject.");
        }
        return new JSONObject((ObjectNode) n);
    }

    public JSONArray getJSONArray(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        if (!n.isArray()) {
            throw new JSONException("JSONObject[\"" + key + "\"] is not a JSONArray.");
        }
        return new JSONArray((ArrayNode) n);
    }

    public Object get(final String key) throws JSONException {
        final JsonNode n = requireNode(key);
        return toObject(n);
    }

    // -------------------------------------------------------------------------
    // opt methods – return null / default if key missing or wrong type
    // -------------------------------------------------------------------------

    public Object opt(final String key) {
        final JsonNode n = node.get(key);
        if (n == null) {
            return null;
        }
        if (n.isNull()) {
            return NULL;
        }
        return toObject(n);
    }

    public String optString(final String key) {
        return optString(key, "");
    }

    public String optString(final String key, final String defaultValue) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull()) {
            return defaultValue;
        }
        return n.asText();
    }

    public int optInt(final String key) {
        return optInt(key, 0);
    }

    public int optInt(final String key, final int defaultValue) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull()) {
            return defaultValue;
        }
        if (n.isNumber()) {
            return n.intValue();
        }
        if (n.isTextual()) {
            try {
                return Integer.parseInt(n.textValue());
            } catch (final NumberFormatException e) {
                // fall through
            }
        }
        return defaultValue;
    }

    public long optLong(final String key) {
        return optLong(key, 0L);
    }

    public long optLong(final String key, final long defaultValue) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull()) {
            return defaultValue;
        }
        if (n.isNumber()) {
            return n.longValue();
        }
        if (n.isTextual()) {
            try {
                return Long.parseLong(n.textValue());
            } catch (final NumberFormatException e) {
                // fall through
            }
        }
        return defaultValue;
    }

    public boolean optBoolean(final String key) {
        return optBoolean(key, false);
    }

    public boolean optBoolean(final String key, final boolean defaultValue) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull()) {
            return defaultValue;
        }
        if (n.isBoolean()) {
            return n.booleanValue();
        }
        final String text = n.asText();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        return defaultValue;
    }

    public JSONObject optJSONObject(final String key) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull() || !n.isObject()) {
            return null;
        }
        return new JSONObject((ObjectNode) n);
    }

    public JSONArray optJSONArray(final String key) {
        final JsonNode n = node.get(key);
        if (n == null || n.isNull() || !n.isArray()) {
            return null;
        }
        return new JSONArray((ArrayNode) n);
    }

    // -------------------------------------------------------------------------
    // Query / utility methods
    // -------------------------------------------------------------------------

    public boolean has(final String key) {
        return node.has(key);
    }

    public int length() {
        return node.size();
    }

    @SuppressWarnings("unchecked")
    public Iterator<String> keys() {
        return node.fieldNames();
    }

    /**
     * Returns a JSONArray of all field names, or null if empty.
     * Mirrors Jettison's names() behaviour.
     */
    public JSONArray names() {
        if (node.size() == 0) {
            return null;
        }
        final ArrayNode array = MAPPER.createArrayNode();
        node.fieldNames().forEachRemaining(array::add);
        return new JSONArray(array);
    }

    // -------------------------------------------------------------------------
    // Static utilities
    // -------------------------------------------------------------------------

    /**
     * Produces a string in double quotes with backslash sequences in all the
     * right places (mirrors Jettison's JSONObject.quote).
     */
    public static String quote(final String string) {
        if (string == null || string.isEmpty()) {
            return "\"\"";
        }
        final StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            switch (c) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (c < ' ') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
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

    private JsonNode requireNode(final String key) throws JSONException {
        final JsonNode n = node.get(key);
        if (n == null) {
            throw new JSONException("JSONObject[\"" + key + "\"] not found.");
        }
        return n;
    }

    private static Object toObject(final JsonNode n) {
        if (n == null || n.isNull()) {
            return NULL;
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
