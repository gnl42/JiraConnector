package me.glindholm.jira.rest.client.internal.json.gen;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;

/**
 * @since v2.0
 */
public class AuditRecordInputJsonGenerator implements JsonGenerator<AuditRecordInput> {
    final AuditAssociatedItemJsonGenerator associatedItemJsonGenerator = new AuditAssociatedItemJsonGenerator();

    @Override
    public JSONObject generate(final AuditRecordInput bean) throws JSONException {
        return new JSONObject().put("category", bean.getCategory()).put("summary", bean.getSummary())
                .put("objectItem", bean.getObjectItem() != null ? associatedItemJsonGenerator.generate(bean.getObjectItem()) : null)
                .put("associatedItems", generateAssociatedItems(bean.getAssociatedItems()))
                .put("changedValues", generateChangedValues(bean.getChangedValues()));
    }

    private JSONArray generateChangedValues(@Nullable final List<AuditChangedValue> changedValues) throws JSONException {
        final AuditChangedValueJsonGenerator generator = new AuditChangedValueJsonGenerator();
        final JSONArray array = new JSONArray();
        if (changedValues != null) {
            for (final AuditChangedValue value : changedValues) {
                array.put(generator.generate(value));
            }
        }
        return array;
    }

    protected JSONArray generateAssociatedItems(@Nullable final List<AuditAssociatedItem> associatedItems) throws JSONException {
        final JSONArray array = new JSONArray();
        if (associatedItems != null) {
            for (final AuditAssociatedItem item : associatedItems) {
                array.put(associatedItemJsonGenerator.generate(item));
            }
        }
        return array;
    }
}
