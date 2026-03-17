package me.glindholm.jira.rest.client.internal.json.gen;

import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;

/**
 * @since v2.0
 */
public class AuditChangedValueJsonGenerator implements JsonGenerator<AuditChangedValue> {
    @Override
    public JSONObject generate(final AuditChangedValue bean) throws JsonProcessingException {
        final JSONObject obj = new JSONObject().put("fieldName", bean.getFieldName());
        if (bean.getChangedTo() != null) {
            obj.put("changedTo", bean.getChangedTo());
        }
        if (bean.getChangedFrom() != null) {
            obj.put("changedFrom", bean.getChangedFrom());
        }
        return obj;
    }
}