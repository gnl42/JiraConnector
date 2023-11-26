package me.glindholm.jira.rest.client.internal.json.gen;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;

/**
 * @since v2.0
 */
public class AuditAssociatedItemJsonGenerator implements JsonGenerator<AuditAssociatedItem> {
    @Override
    public JSONObject generate(final AuditAssociatedItem bean) throws JSONException {
        return new JSONObject().put("id", bean.getId()).put("name", bean.getName()).put("typeName", bean.getTypeName()).put("parentId", bean.getParentId())
                .put("parentName", bean.getParentName());
    }
}
