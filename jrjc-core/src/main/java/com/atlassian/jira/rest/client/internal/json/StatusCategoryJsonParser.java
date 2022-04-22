package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.StatusCategory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

public class StatusCategoryJsonParser implements JsonObjectParser<StatusCategory>  {

    @Override
    public StatusCategory parse(JSONObject json) throws JSONException {
        final URI self = JsonParseUtil.getSelfUri(json);
        final String name = json.getString("name");
        final Long id = JsonParseUtil.getOptionalLong(json, "id");
        final String key = json.getString("key");
        final String colorName = json.getString("colorName");
        return new StatusCategory(self, name, id, key, colorName);
    }
}
