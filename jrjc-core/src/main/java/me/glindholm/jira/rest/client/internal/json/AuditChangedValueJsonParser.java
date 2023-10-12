package me.glindholm.jira.rest.client.internal.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;

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
