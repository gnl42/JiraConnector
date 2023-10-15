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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.ExpandableProperty;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

public class JsonParseUtil {
    public static final String JIRA_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(JIRA_DATE_TIME_PATTERN);
    public static final DateTimeFormatter JIRA_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    public static final String SELF_ATTR = "self";

    public static <T> List<T> parseJsonArray(final JSONArray jsonArray, final JsonObjectParser<T> jsonParser) throws JSONException, URISyntaxException {
        final List<T> res = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            res.add(jsonParser.parse(jsonArray.getJSONObject(i)));
        }
        return res;
    }

    public static <T> List<T> parseOptionalJsonArray(final JSONArray jsonArray, final JsonObjectParser<T> jsonParser) throws JSONException, URISyntaxException {
        if (jsonArray == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(JsonParseUtil.<T>parseJsonArray(jsonArray, jsonParser));
        }
    }

    public static <T> T parseOptionalJsonObject(final JSONObject json, final String attributeName, final JsonObjectParser<T> jsonParser)
            throws JSONException, URISyntaxException {
        final JSONObject attributeObject = getOptionalJsonObject(json, attributeName);
        return attributeObject != null ? jsonParser.parse(attributeObject) : null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static <T> ExpandableProperty<T> parseExpandableProperty(final JSONObject json, final JsonObjectParser<T> expandablePropertyBuilder)
            throws JSONException, URISyntaxException {
        return parseExpandableProperty(json, false, expandablePropertyBuilder);
    }

    @Nullable
    public static <T> ExpandableProperty<T> parseOptionalExpandableProperty(@Nullable final JSONObject json,
            final JsonObjectParser<T> expandablePropertyBuilder) throws JSONException, URISyntaxException {
        return parseExpandableProperty(json, true, expandablePropertyBuilder);
    }

    @Nullable
    private static <T> ExpandableProperty<T> parseExpandableProperty(@Nullable final JSONObject json, final Boolean optional,
            final JsonObjectParser<T> expandablePropertyBuilder) throws JSONException, URISyntaxException {
        if (json == null) {
            if (!optional) {
                throw new IllegalArgumentException("json object cannot be null while optional is false");
            }
            return null;
        }

        final int numItems = json.getInt("size");
        final List<T> items;
        final JSONArray itemsJa = json.getJSONArray("items");

        if (itemsJa.length() > 0) {
            items = new ArrayList<>(numItems);
            for (int i = 0; i < itemsJa.length(); i++) {
                final T item = expandablePropertyBuilder.parse(itemsJa.getJSONObject(i));
                items.add(item);
            }
        } else {
            items = null;
        }

        return new ExpandableProperty<>(numItems, items);
    }

    public static URI getSelfUri(final JSONObject jsonObject) throws JSONException {
        return parseURI(jsonObject.getString(SELF_ATTR));
    }

    public static URI optSelfUri(final JSONObject jsonObject, final URI defaultUri) throws JSONException {
        final String selfUri = jsonObject.optString(SELF_ATTR, null);
        return selfUri != null ? parseURI(selfUri) : defaultUri;
    }

    @SuppressWarnings("unused")
    public static JSONObject getNestedObject(JSONObject json, final String... path) throws JSONException {
        for (final String s : path) {
            json = json.getJSONObject(s);
        }
        return json;
    }

    @Nullable
    public static JSONObject getNestedOptionalObject(JSONObject json, final String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            final String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.optJSONObject(path[path.length - 1]);
    }

    @SuppressWarnings("unused")
    public static JSONArray getNestedArray(JSONObject json, final String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            final String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.getJSONArray(path[path.length - 1]);
    }

    public static JSONArray getNestedOptionalArray(JSONObject json, final String... path) throws JSONException {
        for (int i = 0; json != null && i < path.length - 1; i++) {
            final String s = path[i];
            json = json.optJSONObject(s);
        }
        return json == null ? null : json.optJSONArray(path[path.length - 1]);
    }

    public static String getNestedString(JSONObject json, final String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            final String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.getString(path[path.length - 1]);
    }

    @SuppressWarnings("unused")
    public static boolean getNestedBoolean(JSONObject json, final String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            final String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.getBoolean(path[path.length - 1]);
    }

    public static URI parseURI(final String str) {
        try {
            return new URI(str);
        } catch (final URISyntaxException e) {
            throw new RestClientException(e);
        }
    }

    @Nullable
    public static URI parseOptionalURI(final JSONObject jsonObject, final String attributeName) {
        final String s = getOptionalString(jsonObject, attributeName);
        return s != null ? parseURI(s) : null;
    }

    @Nullable
    public static BasicUser parseBasicUser(@Nullable final JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        final String username = json.optString("name", null);
        if (!json.has(JsonParseUtil.SELF_ATTR) && "Anonymous".equals(username)) {
            return null; // insane representation for unassigned user - JRADEV-4262
        }

        // deleted user? BUG in REST API: JRA-30263
        final URI selfUri = optSelfUri(json, BasicUser.INCOMPLETE_URI);
        // e-mail may be not set in response if e-mail visibility in jira configuration
        // is set to hidden (in jira 4.3+)
        final String emailAddress = JsonParseUtil.getOptionalString(json, "emailAddress");
        final boolean active = Boolean.parseBoolean(JsonParseUtil.getOptionalString(json, "active"));
        return new BasicUser(selfUri, username, json.optString("displayName", null), json.optString("accountId", null), emailAddress, active);
    }

    public static OffsetDateTime parseOffsetDateTime(final JSONObject jsonObject, final String attributeName) throws JSONException {
        return parseOffsetDateTime(jsonObject.getString(attributeName));
    }

    @Nullable
    public static OffsetDateTime parseOptionalOffsetDateTime(final JSONObject jsonObject, final String attributeName) throws JSONException {
        final String s = getOptionalString(jsonObject, attributeName);
        return s != null ? parseOffsetDateTime(s) : null;
    }

    public static OffsetDateTime parseOffsetDateTime(final String str) {
        try {
            return OffsetDateTime.parse(str, JIRA_DATE_TIME_FORMATTER);
        } catch (final Exception e) {
            throw new RestClientException(e);
        }
    }

    /**
     * Tries to parse date and time and return that. If fails then tries to parse date only.
     *
     * @param str String contains either date and time or date only
     * @return date and time or date only
     */
    public static OffsetDateTime parseOffsetDateTimeOrDate(final String str) {
        try {
            return OffsetDateTime.parse(str, JIRA_DATE_TIME_FORMATTER);
        } catch (final Exception ignored) {
            try {
                return parseDate(str);
            } catch (final Exception e) {
                throw new RestClientException(e);
            }
        }
    }

    public static OffsetDateTime parseDate(final String str) {
        try {
            final LocalDate date = LocalDate.parse(str, JIRA_DATE_FORMATTER);
            return OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        } catch (final Exception e) {
            throw new RestClientException(e);
        }
    }

    public static String formatDate(final OffsetDateTime dateTime) {
        return dateTime.format(JIRA_DATE_FORMATTER);
    }

    @SuppressWarnings("unused")
    public static String formatOffsetDateTime(final OffsetDateTime dateTime) {
        return dateTime.format(JIRA_DATE_TIME_FORMATTER);
    }

    @Nullable
    public static String getNullableString(final JSONObject jsonObject, final String attributeName) throws JSONException {
        final Object o = jsonObject.get(attributeName);
        if (o == JSONObject.NULL) {
            return null;
        }
        return o.toString();
    }

    @Nullable
    public static String getOptionalString(final JSONObject jsonObject, final String attributeName) {
        final Object res = jsonObject.opt(attributeName);
        if (res == JSONObject.NULL || res == null) {
            return null;
        }
        return res.toString();
    }

    @Nullable
    public static <T> T getOptionalJsonObject(final JSONObject jsonObject, final String attributeName, final JsonObjectParser<T> jsonParser)
            throws JSONException, URISyntaxException {
        final JSONObject res = jsonObject.optJSONObject(attributeName);
        if (res == JSONObject.NULL || res == null) {
            return null;
        }
        return jsonParser.parse(res);
    }

    @SuppressWarnings("unused")
    @Nullable
    public static JSONObject getOptionalJsonObject(final JSONObject jsonObject, final String attributeName) {
        final JSONObject res = jsonObject.optJSONObject(attributeName);
        if (res == JSONObject.NULL || res == null) {
            return null;
        }
        return res;
    }

    public static List<String> toStringList(final JSONArray jsonArray) throws JSONException {
        final ArrayList<String> res = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            res.add(jsonArray.getString(i));
        }
        return res;
    }

    public static Integer parseOptionInteger(final JSONObject json, final String attributeName) throws JSONException {
        return json.has(attributeName) ? json.getInt(attributeName) : null;
    }

    @Nullable
    public static Long getOptionalLong(final JSONObject jsonObject, final String attributeName) throws JSONException {
        return jsonObject.has(attributeName) ? jsonObject.getLong(attributeName) : null;
    }

    public static Optional<JSONArray> getOptionalArray(final JSONObject jsonObject, final String attributeName) throws JSONException {
        return jsonObject.has(attributeName) ? Optional.of(jsonObject.getJSONArray(attributeName)) : Optional.<JSONArray>empty();
    }

    public static Map<String, URI> getAvatarUris(final JSONObject jsonObject) throws JSONException {
        final Map<String, URI> uris = new HashMap<>();

        final Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            final Object o = iterator.next();
            if (!(o instanceof final String key)) {
                throw new JSONException("Cannot parse URIs: key is expected to be valid String. Got " + (o == null ? "null" : o.getClass()) + " instead.");
            }
            uris.put(key, JsonParseUtil.parseURI(jsonObject.getString(key)));
        }
        return uris;
    }

    @SuppressWarnings("unchecked")
    public static Iterator<String> getStringKeys(final JSONObject json) {
        return json.keys();
    }

    public static Map<String, String> toStringMap(final JSONArray names, final JSONObject values) throws JSONException {
        final Map<String, String> result = new HashMap<>();
        for (int i = 0; i < names.length(); i++) {
            final String key = names.getString(i);
            result.put(key, values.getString(key));
        }
        return result;
    }
}