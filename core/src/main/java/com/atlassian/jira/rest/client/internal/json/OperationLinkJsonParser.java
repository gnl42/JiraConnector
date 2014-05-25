package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class OperationLinkJsonParser implements JsonObjectParser<OperationLink> {
	@Override
	public OperationLink parse(JSONObject json) throws JSONException {
		final String id = JsonParseUtil.getOptionalString(json, "id");
		final String styleClass = JsonParseUtil.getOptionalString(json, "styleClass");
		final String label = json.getString("label");
		final String title = JsonParseUtil.getOptionalString(json, "title");
		final String href = json.getString("href");
		final Integer weight = JsonParseUtil.parseOptionInteger(json, "weight");
		final String iconClass = JsonParseUtil.getOptionalString(json, "iconClass");
		return new OperationLink(id, styleClass, label, title, href, weight, iconClass);
	}
}
