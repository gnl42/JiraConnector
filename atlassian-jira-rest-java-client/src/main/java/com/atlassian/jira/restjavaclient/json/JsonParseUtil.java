package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.RestClientException;
import com.atlassian.jira.restjavaclient.domain.User;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.net.URI;
import java.net.URISyntaxException;

public class JsonParseUtil {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();


	public static URI getSelfUri(JSONObject jsonObject) throws JSONException {
		return parseURI(jsonObject.getString("self"));
	}

	public static JSONObject getNestedObject(JSONObject json, String... path) throws JSONException {
		for (String s : path) {
			json = json.getJSONObject(s);
		}
		return json;
	}

	public static String getNestedString(JSONObject json, String... path) throws JSONException {

		for (int i = 0; i < path.length - 1; i++) {
			String s = path[i];
			json = json.getJSONObject(s);
		}
		return json.getString(path[path.length - 1]);
	}
	

	public static URI parseURI(String str) {
		try {
			return new URI(str);
		} catch (URISyntaxException e) {
			throw new RestClientException(e);
		}
	}

	public static User parseAuthor(JSONObject json) throws JSONException {
		return new User(getSelfUri(json), json.getString("name"), json.optString("displayName", null));
	}

	public static DateTime parseDateTime(String str) {
		try {
			return DATE_TIME_FORMATTER.parseDateTime(str);
		} catch (Exception e) {
			throw new RestClientException(e);
		}
	}
}