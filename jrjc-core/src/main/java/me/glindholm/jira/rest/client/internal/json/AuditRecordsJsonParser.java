package me.glindholm.jira.rest.client.internal.json;

import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;
import me.glindholm.jira.rest.client.api.domain.AuditRecord;
import me.glindholm.jira.rest.client.api.domain.AuditRecordsData;

/**
 * @since v2.0
 */
public class AuditRecordsJsonParser implements JsonObjectParser<AuditRecordsData> {

    private final AuditAssociatedItemJsonParser associatedItemJsonParser = new AuditAssociatedItemJsonParser();
    private final AuditChangedValueJsonParser changedValueJsonParser = new AuditChangedValueJsonParser();
    private final SingleAuditRecordJsonParser singleAuditRecordJsonParser = new SingleAuditRecordJsonParser();

    @Override
    public AuditRecordsData parse(final JSONObject json) throws JSONException, URISyntaxException {
        final Integer offset = json.getInt("offset");
        final Integer limit = json.getInt("limit");
        final Integer total = json.getInt("total");
        final List<AuditRecord> records = JsonParseUtil.parseOptionalJsonArray(json.optJSONArray("records"), singleAuditRecordJsonParser);

        return new AuditRecordsData(offset, limit, total, records);
    }

    class SingleAuditRecordJsonParser implements JsonObjectParser<AuditRecord> {
        @Override
        public AuditRecord parse(final JSONObject json) throws JSONException, URISyntaxException {
            final Long id = json.getLong("id");
            final String summary = json.getString("summary");

            final String createdString = json.getString("created");
            final OffsetDateTime created = JsonParseUtil.parseOffsetDateTime(json, "created");
            final String category = json.getString("category");
            final String eventSource = json.getString("eventSource");
            final String authorKey = JsonParseUtil.getOptionalString(json, "authorKey");
            final String remoteAddress = JsonParseUtil.getOptionalString(json, "remoteAddress");
            final AuditAssociatedItem objectItem = JsonParseUtil.getOptionalJsonObject(json, "objectItem", associatedItemJsonParser);
            final List<AuditAssociatedItem> associatedItem = JsonParseUtil.parseOptionalJsonArray(json.optJSONArray("associatedItems"),
                    associatedItemJsonParser);
            final List<AuditChangedValue> changedValues = JsonParseUtil.parseOptionalJsonArray(json.optJSONArray("changedValues"), changedValueJsonParser);

            return new AuditRecord(id, summary, remoteAddress, created, category, eventSource, authorKey, objectItem, associatedItem, changedValues);
        }

    }
}
