package me.glindholm.jira.rest.client.internal.json;

import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONException;
import me.glindholm.jira.rest.client.shim.jettison.json.JSONObject;

/**
 * @since v2.0
 */
public class AuditChangedValueJsonParser implements JsonObjectParser<AuditChangedValue> {

    @Override
    public AuditChangedValue parse(final JSONObject json) throws JSONException {
        final String fieldName = json.getString("fieldName");
        final String changedFrom = JsonParseUtil.getOptionalString(json, "changedFrom");
        final String changedTo = JsonParseUtil.getOptionalString(json, "changedTo");

        return new AuditChangedValue(fieldName, changedTo, changedFrom);
    }
}
