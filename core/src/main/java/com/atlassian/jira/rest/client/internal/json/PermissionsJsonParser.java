package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class PermissionsJsonParser implements JsonObjectParser<Permissions> {
    private final PermissionJsonParser permissionJsonParser = new PermissionJsonParser();

    @Override
    public Permissions parse(JSONObject json) throws JSONException {
        JSONObject permissionsObject = json.getJSONObject("permissions");

        List<Permission> permissions = Lists.newArrayList();
        Iterator it = permissionsObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            JSONObject permissionObject = permissionsObject.getJSONObject(key);
            Permission permission = permissionJsonParser.parse(permissionObject);
            permissions.add(permission);
        }
        return new Permissions(permissions);
    }
}
