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

package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.IssueField;

public class IssueFieldJsonParser {
    private static final String VALUE_ATTRIBUTE = "value";

    private final Map<String, JsonObjectParser<?>> registeredValueParsers = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put("com.atlassian.jira.plugin.system.customfieldtypes:float", new FloatingPointFieldValueParser());
            put("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", new FieldValueJsonParser<>(new BasicUserJsonParser()));
            put("java.lang.String", new StringFieldValueParser());
        }
    };

    public IssueField parse(final JSONObject jsonObject, final String id) throws JSONException, URISyntaxException {
        String type = jsonObject.getString("type");
        final String name = jsonObject.getString("name");
        final Object valueObject = jsonObject.opt(VALUE_ATTRIBUTE);
        final Object value;
        // @todo ugly hack until https://jdog.atlassian.com/browse/JRADEV-3220 is fixed
        if ("comment".equals(name)) {
            type = "com.atlassian.jira.Comment";
        }

        if (valueObject == null) {
            value = null;
        } else {
            final JsonObjectParser<?> valueParser = registeredValueParsers.get(type);
            if (valueParser != null) {
                value = valueParser.parse(jsonObject);
            } else {
                value = valueObject.toString();
            }
        }
        return new IssueField(id, name, type, value);
    }

    static class FieldValueJsonParser<T> implements JsonObjectParser<T> {
        private final JsonObjectParser<T> jsonParser;

        public FieldValueJsonParser(final JsonObjectParser<T> jsonParser) {
            this.jsonParser = jsonParser;
        }

        @Override
        public T parse(final JSONObject json) throws JSONException, URISyntaxException {
            final JSONObject valueObject = json.optJSONObject(VALUE_ATTRIBUTE);
            if (valueObject == null) {
                throw new JSONException("Expected JSONObject with [" + VALUE_ATTRIBUTE + "] attribute present.");
            }
            return jsonParser.parse(valueObject);
        }
    }

    static class FloatingPointFieldValueParser implements JsonObjectParser<Double> {

        @Override
        public Double parse(final JSONObject jsonObject) throws JSONException {
            final String s = JsonParseUtil.getNullableString(jsonObject, VALUE_ATTRIBUTE);
            if (s == null) {
                return null;
            }
            try {
                return Double.parseDouble(s);
            } catch (final NumberFormatException e) {
                throw new JSONException("[" + s + "] is not a valid floating point number");
            }
        }
    }

    static class StringFieldValueParser implements JsonObjectParser<String> {

        @Override
        public String parse(final JSONObject jsonObject) throws JSONException {
            return JsonParseUtil.getNullableString(jsonObject, VALUE_ATTRIBUTE);
        }
    }

}
