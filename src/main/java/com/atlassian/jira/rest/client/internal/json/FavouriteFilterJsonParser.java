package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.FavouriteFilter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

/**
 * User: kalamon
 * Date: 16.11.12
 * Time: 11:44
 */
public class FavouriteFilterJsonParser implements JsonObjectParser<FavouriteFilter>  {
    @Override
    public FavouriteFilter parse(JSONObject json) throws JSONException {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final long id = json.getLong("id");
        final String name = json.getString("name");
        final String jql = json.getString("jql");
        final URI searchUrl = JsonParseUtil.parseURI(json.getString("searchUrl"));
        final URI viewUrl = JsonParseUtil.parseURI(json.getString("viewUrl"));
        return new FavouriteFilter(selfUri, name, id, jql, viewUrl, searchUrl);
    }
}
