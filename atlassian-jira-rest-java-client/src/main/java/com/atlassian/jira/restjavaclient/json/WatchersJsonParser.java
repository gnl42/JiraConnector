package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class WatchersJsonParser {
	Watchers parseWatchers(JSONObject json) throws JSONException {
		final URI self = JsonParseUtil.getSelfUri(json);
		final boolean isWatching = json.getBoolean("isWatching");
		final ExpandableProperty<User> list = JsonParseUtil.parseExpandableProperty(json.getJSONObject("list"),
				new JsonParseUtil.ExpandablePropertyBuilder<User>() {
			public User parse(JSONObject json) throws JSONException {
				return JsonParseUtil.parseUser(json);
			}
		});
		return new Watchers(self, isWatching, list);
	}
}
