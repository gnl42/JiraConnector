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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.ExpandableProperty;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.BasicUser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

public class JsonParseUtil {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();
	private static final DateTimeFormatter DATE_FORMATTER = ISODateTimeFormat.date();
    public static final String VALUE_KEY = "value";
	public static final String SELF_ATTR = "self";

//	public interface ExpandablePropertyBuilder<T> {
//		T parse(JSONObject json) throws JSONException;
//	}

    public static <T> Collection<T> parseJsonArray(JSONArray jsonArray, JsonParser<T> jsonParser) throws JSONException {
        final Collection<T> res = new ArrayList<T>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            res.add(jsonParser.parse(jsonArray.getJSONObject(i)));
        }
        return res;
    }
    
	public static <T> ExpandableProperty<T> parseExpandableProperty(JSONObject json, JsonParser<T> expandablePropertyBuilder)
			throws JSONException {
		final int numItems = json.getInt("size");
		final Collection<T> items;
		JSONArray itemsJa = json.getJSONArray("items");

		if (itemsJa.length() > 0) {
			items = new ArrayList<T>(numItems);
			for (int i = 0; i < itemsJa.length(); i++) {
				final T item = expandablePropertyBuilder.parse(itemsJa.getJSONObject(i));
				items.add(item);
			}
		} else {
			items = null;
		}

		return new ExpandableProperty<T>(numItems, items);
	}



	public static URI getSelfUri(JSONObject jsonObject) throws JSONException {
		return parseURI(jsonObject.getString(SELF_ATTR));
	}

	public static JSONObject getNestedObject(JSONObject json, String... path) throws JSONException {
		for (String s : path) {
			json = json.getJSONObject(s);
		}
		return json;
	}

    @Nullable
    public static JSONObject getNestedOptionalObject(JSONObject json, String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.optJSONObject(path[path.length - 1]);
    }


	public static JSONArray getNestedArray(JSONObject json, String... path) throws JSONException {
		for (int i = 0; i < path.length - 1; i++) {
			String s = path[i];
			json = json.getJSONObject(s);
		}
		return json.getJSONArray(path[path.length - 1]);
	}

    public static JSONArray getNestedOptionalArray(JSONObject json, String... path) throws JSONException {
        for (int i = 0; i < path.length - 1; i++) {
            String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.optJSONArray(path[path.length - 1]);
    }


	public static String getNestedString(JSONObject json, String... path) throws JSONException {

		for (int i = 0; i < path.length - 1; i++) {
			String s = path[i];
			json = json.getJSONObject(s);
		}
		return json.getString(path[path.length - 1]);
	}

    public static boolean getNestedBoolean(JSONObject json, String... path) throws JSONException {

        for (int i = 0; i < path.length - 1; i++) {
            String s = path[i];
            json = json.getJSONObject(s);
        }
        return json.getBoolean(path[path.length - 1]);
    }


	public static URI parseURI(String str) {
		try {
			return new URI(str);
		} catch (URISyntaxException e) {
			throw new RestClientException(e);
		}
	}

	@Nullable
	public static URI parseOptionalURI(JSONObject jsonObject, String attributeName) {
		final String s = getOptionalString(jsonObject, attributeName);
		return s != null ? parseURI(s) : null;
	}

	@Nullable
	public static BasicUser parseBasicUser(@Nullable JSONObject json) throws JSONException {
		if (json == null) {
			return null;
		}
		final String username = json.getString("name");
		if (!json.has(JsonParseUtil.SELF_ATTR) && "Anonymous".equals(username)) {
			return null; // insane representation for unassigned user - JRADEV-4262
		}
		return new BasicUser(getSelfUri(json), username, json.optString("displayName", null));
	}

	public static DateTime parseDateTime(JSONObject jsonObject, String attributeName) throws JSONException {
		return parseDateTime(jsonObject.getString(attributeName));
	}

	@Nullable
	public static DateTime parseOptionalDateTime(JSONObject jsonObject, String attributeName) throws JSONException {
		final String s = getOptionalString(jsonObject, attributeName);
		return s != null ? parseDateTime(s) : null;
	}

	public static DateTime parseDateTime(String str) {
		try {
			return DATE_TIME_FORMATTER.parseDateTime(str);
		} catch (Exception e) {
			throw new RestClientException(e);
		}
	}

	public static DateTime parseDate(String str) {
		try {
			return DATE_FORMATTER.parseDateTime(str);
		} catch (Exception e) {
			throw new RestClientException(e);
		}
	}

	public static String format(DateTime dateTime) {
		return DATE_FORMATTER.print(dateTime);
	}


	@Nullable
	public static String getNullableString(JSONObject jsonObject, String attributeName) throws JSONException {
		final Object o = jsonObject.get(attributeName);
		if (o == JSONObject.NULL) {
			return null;
		}
		return o.toString();
	}


    @Nullable
    public static String getOptionalString(JSONObject jsonObject, String attributeName) {
		final Object res = jsonObject.opt(attributeName);
		if (res == JSONObject.NULL || res == null) {
			return null;
		}
		return res.toString();
    }

	@Nullable
	public static JSONObject getOptionalJsonObject(JSONObject jsonObject, String attributeName) {
		final JSONObject res = jsonObject.optJSONObject(attributeName);
		if (res == JSONObject.NULL || res == null) {
			return null;
		}
		return res;
	}


	public static Collection<String> toStringCollection(JSONArray jsonArray) throws JSONException {
		final ArrayList<String> res = new ArrayList<String>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			res.add(jsonArray.getString(i));
		}
		return res;
	}

	public static Integer parseOptionInteger(JSONObject json, final String attributeName) throws JSONException {
		return json.has(attributeName) ? json.getInt(attributeName) : null;
	}
}