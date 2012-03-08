package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.IssuelinksType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IssueLinkTypesJsonParser implements JsonParser<Iterable<IssuelinksType>> {
	private final IssuelinksTypeJsonParserV5 issueLinkTypeJsonParser = new IssuelinksTypeJsonParserV5();

	@Override
	public Iterable<IssuelinksType> parse(JSONObject json) throws JSONException {
		return JsonParseUtil.parseJsonArray(json.optJSONArray("issueLinkTypes"), issueLinkTypeJsonParser);
	}
}
