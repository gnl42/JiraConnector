package me.glindholm.jira.rest.client.internal.json;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

/**
 * @since v2.0
 */
public class AuditAssociatedItemJsonParser implements JsonObjectParser<AuditAssociatedItem> {

    @Override
    public AuditAssociatedItem parse(final JSONObject json) throws JSONException {

        final String id = JsonParseUtil.getOptionalString(json, "id");
        final String name = json.getString("name");
        final String typeName = json.getString("typeName");
        final String parentId = JsonParseUtil.getOptionalString(json, "parentId");
        final String parentName = JsonParseUtil.getOptionalString(json, "parentName");

        return new AuditAssociatedItem(id, name, typeName, parentId, parentName);
    }
}
