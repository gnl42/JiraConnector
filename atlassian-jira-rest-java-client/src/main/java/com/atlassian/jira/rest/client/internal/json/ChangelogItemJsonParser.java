package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.ChangelogItem;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ChangelogItemJsonParser implements JsonParser<ChangelogItem> {
	@Override
	public ChangelogItem parse(JSONObject json) throws JSONException {
		final String fieldtype = JsonParseUtil.getNestedString(json, "fieldtype");
		final String field = JsonParseUtil.getNestedString(json, "field");
		final String from = JsonParseUtil.getNullableString(json, "from");
		final String fromString = JsonParseUtil.getNullableString(json, "fromString");
		final String to = JsonParseUtil.getNullableString(json, "to");
		final String toString = JsonParseUtil.getNullableString(json, "toString");
		return new ChangelogItem(fieldtype, field, from, fromString, to, toString);
	}
}
