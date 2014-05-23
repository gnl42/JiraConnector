package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class OperationHeaderJsonParser implements JsonObjectParser<OperationHeader> {
	@Override
	public OperationHeader parse(JSONObject json) throws JSONException {
		final String id = json.getString("id");
		final String label = json.getString("label");
		return new OperationHeader(id, label);
	}
}
