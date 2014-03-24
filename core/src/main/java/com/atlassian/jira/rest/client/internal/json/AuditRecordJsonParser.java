package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.domain.AuditAssociatedItem;
import com.atlassian.jira.rest.client.api.domain.AuditChangedValue;
import com.atlassian.jira.rest.client.api.domain.AuditRecord;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * TODO: Document this class / interface here
 *
 * @since v2.0
 */
public class AuditRecordJsonParser implements JsonObjectParser<AuditRecord> {

    private final AuditAssociatedItemJsonParser associatedItemJsonParser = new AuditAssociatedItemJsonParser();
    private final AuditChangedValueJsonParser changedValueJsonParser = new AuditChangedValueJsonParser();

    @Override
    public AuditRecord parse(JSONObject json) throws JSONException {

        final Long id =  json.getLong("id");
        final String summary = json.getString("summary");
        final Long created = json.getLong("created");
        final String category = json.getString("category");
        final String remoteAddress = json.optString("remoteAddr");
        final AuditAssociatedItem objectItem = associatedItemJsonParser.parse(JsonParseUtil.getOptionalJsonObject(json, "objectItem"));
        final OptionalIterable<AuditAssociatedItem> associatedItem = JsonParseUtil.parseOptionalJsonArray(json.optJSONArray("associatedItems"), associatedItemJsonParser);
        final OptionalIterable<AuditChangedValue> changedValues = JsonParseUtil.parseOptionalJsonArray(json.optJSONArray("values"), changedValueJsonParser);

        return new AuditRecord(id, summary, remoteAddress, created, category, objectItem, associatedItem, changedValues);
    }


}
