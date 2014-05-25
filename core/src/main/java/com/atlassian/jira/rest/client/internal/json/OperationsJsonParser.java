package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.Operations;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collection;

public class OperationsJsonParser implements JsonObjectParser<Operations> {
	private final JsonObjectParser<OperationGroup> groupParser = new OperationGroupJsonParser();

	@Override
	public Operations parse(JSONObject json) throws JSONException {
		final Collection<OperationGroup> linkGroups = JsonParseUtil.parseJsonArray(json.getJSONArray("linkGroups"), groupParser);
		return new Operations(linkGroups);
	}
}
