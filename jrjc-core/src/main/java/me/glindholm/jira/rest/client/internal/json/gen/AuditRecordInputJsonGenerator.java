package me.glindholm.jira.rest.client.internal.json.gen;

import java.util.List;

import javax.annotation.Nullable;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;

/**
 * @since v2.0
 */
public class AuditRecordInputJsonGenerator implements JsonGenerator<AuditRecordInput> {
    final AuditAssociatedItemJsonGenerator associatedItemJsonGenerator = new AuditAssociatedItemJsonGenerator();

    @Override
    public JSONObject generate(AuditRecordInput bean) throws JSONException {
        return new JSONObject()
                .put("category", bean.getCategory())
                .put("summary", bean.getSummary())
                .put("objectItem", bean.getObjectItem() != null ? associatedItemJsonGenerator.generate(bean.getObjectItem()) : null)
                .put("associatedItems", generateAssociatedItems(bean.getAssociatedItems()))
                .put("changedValues", generateChangedValues(bean.getChangedValues()));
    }

    private JSONArray generateChangedValues(@Nullable List<AuditChangedValue> changedValues) throws JSONException {
        final AuditChangedValueJsonGenerator generator = new AuditChangedValueJsonGenerator();
        final JSONArray array = new JSONArray();
        if (changedValues != null) {
            for (AuditChangedValue value : changedValues) {
                array.put(generator.generate(value));
            }
        }
        return array;
    }

    protected JSONArray generateAssociatedItems(@Nullable List<AuditAssociatedItem> associatedItems) throws JSONException {
        final JSONArray array = new JSONArray();
        if (associatedItems != null) {
            for (AuditAssociatedItem item : associatedItems) {
                array.put(associatedItemJsonGenerator.generate(item));
            }
        }
        return array;
    }
}
