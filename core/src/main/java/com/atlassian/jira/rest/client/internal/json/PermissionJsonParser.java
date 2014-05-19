package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Permission;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PermissionJsonParser implements JsonObjectParser<Permission> {
    @Override
    public Permission parse(JSONObject json) throws JSONException {
        final Integer id = json.getInt("id");
        final String key = json.getString("key");
        final String name = json.getString("name");
        final String description = json.getString("description");
        final boolean havePermission = json.getBoolean("havePermission");
        return new Permission(id, key, name, description, havePermission);
    }
}
