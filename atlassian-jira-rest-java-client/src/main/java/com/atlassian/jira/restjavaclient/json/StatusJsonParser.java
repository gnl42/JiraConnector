package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.domain.BasicStatus;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class StatusJsonParser {
	public BasicStatus parseBasicStatus(JSONObject json) throws JSONException {
		final URI self = JsonParseUtil.getSelfUri(json);
		final String name = json.getString("name");
		return new BasicStatus(self, name);
	}
}
