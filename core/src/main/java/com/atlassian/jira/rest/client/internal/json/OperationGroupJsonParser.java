package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.OperationHeader;
import com.atlassian.jira.rest.client.api.domain.OperationLink;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class OperationGroupJsonParser implements JsonObjectParser<OperationGroup> {
	final private OperationLinkJsonParser linkJsonParser;
	final private OperationHeaderJsonParser headerJsonParser;

	public OperationGroupJsonParser() {
		linkJsonParser = new OperationLinkJsonParser();
		headerJsonParser = new OperationHeaderJsonParser();
	}

	@Override
	public OperationGroup parse(JSONObject json) throws JSONException {
		String id = JsonParseUtil.getOptionalString(json, "id");
		Iterable<OperationLink> links = JsonParseUtil.parseJsonArray(json.getJSONArray("links"), linkJsonParser);
		Iterable<OperationGroup> groups = JsonParseUtil.parseJsonArray(json.getJSONArray("groups"), this);
		JSONObject headerJson = JsonParseUtil.getOptionalJsonObject(json, "header");
		OperationHeader header = headerJson != null ? headerJsonParser.parse(headerJson) : null;
		Integer weight = JsonParseUtil.parseOptionInteger(json, "weight");
		return new OperationGroup(id, links, groups, header, weight);
	}
}
