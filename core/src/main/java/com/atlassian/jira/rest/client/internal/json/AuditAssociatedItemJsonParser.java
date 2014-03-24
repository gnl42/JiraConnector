package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.AuditAssociatedItem;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v2.0
 */
public class AuditAssociatedItemJsonParser implements JsonObjectParser<AuditAssociatedItem> {

    @Override
    @Nullable
    public AuditAssociatedItem parse(JSONObject json) throws JSONException {

        if (json == null) {
            return null;
        }

        final String id = JsonParseUtil.getOptionalString(json, "id");
        final String name = json.getString("name");
        final String typeName = json.getString("typeName");
        final String parentId = JsonParseUtil.getOptionalString(json, "parentId");
        final String parentName = JsonParseUtil.getOptionalString(json, "parentName");

        return new AuditAssociatedItem(id, name, typeName, parentId, parentName);
    }
}
