package me.glindholm.jira.rest.client.api.domain;

import java.util.List;
import java.util.Objects;

/**
 * Represents audit search metadata and audit result records
 */
public class AuditRecordsData {

    @Override
    public String toString() {
        return "AuditRecordsData [offset=" + offset + ", limit=" + limit + ", total=" + total + ", records=" + records + "]";
    }

    private final Integer offset;
    private final Integer limit;
    private final Integer total;
    private final List<AuditRecord> records;

    public AuditRecordsData(final Integer offset, final Integer limit, final Integer total, final List<AuditRecord> records) {
        this.offset = offset;
        this.limit = limit;
        this.total = total;
        this.records = records;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getTotal() {
        return total;
    }

    public List<AuditRecord> getRecords() {
        return records;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final AuditRecordsData that) {
            return Objects.equals(offset, that.offset) && Objects.equals(limit, that.limit) && Objects.equals(total, that.total)
                    && Objects.equals(records, that.records);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, total, records);
    }
}
