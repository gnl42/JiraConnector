package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.IssueLink;
import com.atlassian.jira.restjavaclient.domain.IssueLinkType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueLinkJsonParser {
	private final IssueLinkTypeJsonParser issueLinkTypeJsonParser = new IssueLinkTypeJsonParser();

	public IssueLink parseIssueLink(JSONObject json) throws JSONException {
		final String key = json.getString("key");
		final URI targetIssueUri = JsonParseUtil.getSelfUri(json);
		final IssueLinkType issueLinkType = issueLinkTypeJsonParser.parseIssueLinkType(json.getJSONObject("type"));
		return new IssueLink(key, targetIssueUri, issueLinkType);
	}
}
