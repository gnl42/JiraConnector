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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.Field;
import com.atlassian.jira.restjavaclient.domain.FloatingPointFieldValue;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class JsonFieldParser implements JsonParser<Field>{
    private static final String VALUE_ATTRIBUTE = "value";

    private Map<String, FieldValueParser> registeredValueParsers = new HashMap<String, FieldValueParser>() {{
        put("com.atlassian.jira.plugin.system.customfieldtypes:float", new FloatingPointFieldValueParser());
        put("java.lang.String", new StringFieldValueParser());
    }};

    @Override
    public Field parse(JSONObject jsonObject) throws JSONException {
        final String type = jsonObject.getString("type");
        final String name = jsonObject.getString("name");
        final Object valueObject = jsonObject.opt(VALUE_ATTRIBUTE);
        final Object value;
        if (valueObject == null) {
            value = null;
        } else {
            final FieldValueParser valueParser = registeredValueParsers.get(type);
            if (valueParser != null) {
                value = valueParser.parse(jsonObject);
            } else {
                value = valueObject.toString();
        }
        }
        return new Field(name, type, value);
    }


    interface FieldValueParser {
        @Nullable
        Object parse(JSONObject jsonObject) throws JSONException;
    }

    class FloatingPointFieldValueParser implements FieldValueParser {

        @Override
        public Double parse(JSONObject jsonObject) throws JSONException {
            final String s = JsonParseUtil.getNullableString(jsonObject, VALUE_ATTRIBUTE);
            if (s == null) {
                return null;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new JSONException("[" + s + "] is not a valid floating point number");
            }
        }
    }

    class StringFieldValueParser implements FieldValueParser {

        @Override
        public String parse(JSONObject jsonObject) throws JSONException {
            return JsonParseUtil.getNullableString(jsonObject, VALUE_ATTRIBUTE);
        }
    }

}
