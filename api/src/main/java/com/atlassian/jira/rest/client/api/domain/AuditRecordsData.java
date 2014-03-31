package com.atlassian.jira.rest.client.api.domain;

/**
 * Represents audit search metadata and audit result records
 */
public class AuditRecordsData {

    private final Integer offset;
    private final Integer limit;
    private final Integer total;
    private final Iterable<AuditRecord> records;

    public AuditRecordsData(final Integer offset, final Integer limit, final Integer total, final Iterable<AuditRecord> records) {
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

    public Iterable<AuditRecord> getRecords() {
        return records;
    }

    //todo - add missing methods: equals, hashcode ...
}
