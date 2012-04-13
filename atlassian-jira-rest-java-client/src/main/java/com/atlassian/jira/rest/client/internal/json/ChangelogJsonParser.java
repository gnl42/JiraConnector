package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.domain.ChangelogItem;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;

public class ChangelogJsonParser implements JsonParser<ChangelogGroup> {
	private final ChangelogItemJsonParser changelogItemJsonParser = new ChangelogItemJsonParser();

	@Override
	public ChangelogGroup parse(JSONObject json) throws JSONException {
		final DateTime created = JsonParseUtil.parseDateTime(json, "created");
		final BasicUser author = JsonParseUtil.parseBasicUser(json.getJSONObject("author"));
		final Collection<ChangelogItem> items = JsonParseUtil.parseJsonArray(json.getJSONArray("items"), changelogItemJsonParser);
		return new ChangelogGroup(author, created, items);
	}
}
