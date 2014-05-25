package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class OperationHeaderJsonParser implements JsonObjectParser<OperationHeader> {
	@Override
	public OperationHeader parse(JSONObject json) throws JSONException {
		final String id = JsonParseUtil.getOptionalString(json, "id");
		final String label = json.getString("label");
		final String title = JsonParseUtil.getOptionalString(json, "title");
		final String iconClass = JsonParseUtil.getOptionalString(json, "iconClass");
		return new OperationHeader(id, label, title, iconClass);
	}
}
