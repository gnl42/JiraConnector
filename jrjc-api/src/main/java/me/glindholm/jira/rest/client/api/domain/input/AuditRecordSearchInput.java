package me.glindholm.jira.rest.client.api.domain.input;

import java.time.OffsetDateTime;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Input data for searching audit records
 *
 * @since v2.0.0
 */
public class AuditRecordSearchInput {

    @Nullable
    private final Integer offset;
    @Nullable
    private final Integer limit;
    @Nullable
    private final String textFilter;
    @Nullable
    private final OffsetDateTime from;
    @Nullable
    private final OffsetDateTime to;

    public AuditRecordSearchInput(final Integer offset, final Integer limit, final String textFilter, final OffsetDateTime from, final OffsetDateTime to) {
        this.offset = offset;
        this.limit = limit;
        this.textFilter = textFilter;
        this.from = from;
        this.to = to;
    }

    @Nullable
    public Integer getOffset() {
        return offset;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    @Nullable
    public String getTextFilter() {
        return textFilter;
    }

    @Nullable
    public OffsetDateTime getFrom() {
        return from;
    }

    @Nullable
    public OffsetDateTime getTo() {
        return to;
    }
}
