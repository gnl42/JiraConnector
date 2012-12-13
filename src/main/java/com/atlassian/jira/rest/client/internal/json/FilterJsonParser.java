package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Filter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * JSON parser for Field
 *
 * @since v2.0
 */
public class FilterJsonParser implements JsonObjectParser<Filter> {

    @Override
    public Filter parse(JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final long id = json.getLong("id");
        final String name = json.getString("name");
        final String jql = json.getString("jql");
		final String description = json.optString("description");
        final URI searchUrl = JsonParseUtil.parseURI(json.getString("searchUrl"));
        final URI viewUrl = JsonParseUtil.parseURI(json.getString("viewUrl"));
		final BasicUser owner = JsonParseUtil.parseBasicUser(json.getJSONObject("owner"));
		final boolean favourite = json.getBoolean("favourite");
        return new Filter(selfUri, id, name, description, jql, viewUrl, searchUrl, owner, favourite);
    }
}
