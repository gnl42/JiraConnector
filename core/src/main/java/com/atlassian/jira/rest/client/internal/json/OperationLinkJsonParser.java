package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

public class OperationLinkJsonParser implements JsonObjectParser<OperationLink> {
	@Override
	public OperationLink parse(JSONObject json) throws JSONException {
		final String id = json.getString("id");
		final String styleClass = json.getString("styleClass");
		final String label = json.getString("label");
		final String title = json.getString("title");
		final URI href = JsonParseUtil.parseURI(json.getString("href"));
		Integer integer = json.getInt("weight");
		String iconClass = JsonParseUtil.getOptionalString(json, "iconClass");;
		return new OperationLink(id, styleClass, label, title, href, integer, iconClass);
	}
}
