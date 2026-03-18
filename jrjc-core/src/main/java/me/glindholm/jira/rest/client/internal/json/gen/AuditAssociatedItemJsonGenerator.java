package me.glindholm.jira.rest.client.internal.json.gen;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

/**
 * @since v2.0
 */
public class AuditAssociatedItemJsonGenerator implements JsonGenerator<AuditAssociatedItem> {
    @Override
    public JSONObject generate(final AuditAssociatedItem bean) throws JsonProcessingException {
        return new JSONObject().put("id", bean.getId()).put("name", bean.getName()).put("typeName", bean.getTypeName()).put("parentId", bean.getParentId())
                .put("parentName", bean.getParentName());
    }
}