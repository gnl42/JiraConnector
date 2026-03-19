package me.glindholm.jira.rest.client.internal.json;

import java.net.URI;

import me.glindholm.jira.rest.client.api.StatusCategory;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

public class StatusCategoryJsonParser implements JsonObjectParser<StatusCategory> {

    @Override
    public StatusCategory parse(final JSONObject json) throws JSONException {
        final URI self = JsonParseUtil.getSelfUri(json);
        final String name = json.getString("name");
        final Long id = JsonParseUtil.getOptionalLong(json, "id");
        final String key = json.getString("key");
        final String colorName = json.getString("colorName");
        return new StatusCategory(self, name, id, key, colorName);
    }
}
