package com.atlassian.jira.rest.client.api.domain;

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.util.concurrent.Nullable;
import com.google.common.base.Objects;

/**
 * Represents record from JIRA Audit Log.
 *
 * @since v2.0
 */
public class AuditRecord {

    private final Long id;

    private final String summary;

    private final Long created;

    private final String category;

    @Nullable
    private final String remoteAddress;

    @Nullable
    private final AuditAssociatedItem objectItem;

    @Nullable
    private final OptionalIterable<AuditAssociatedItem> associatedItem;

    @Nullable
    private final OptionalIterable<AuditChangedValue> changedValues;

    public AuditRecord( final Long id,  final String summary, @Nullable final String remoteAddress,
                        final Long created, final String category,
                        @Nullable final AuditAssociatedItem objectItem,
                        @Nullable final OptionalIterable<AuditAssociatedItem> associatedItem,
                        @Nullable final OptionalIterable<AuditChangedValue> changedValues) {
        this.id = id;
        this.summary = summary;
        this.remoteAddress = remoteAddress;
        this.created = created;
        this.category = category;
        this.objectItem = objectItem;
        this.associatedItem = associatedItem;
        this.changedValues = changedValues;
    }

    public Long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public Long getCreated() {
        return created;
    }

    public String getCategory() {
        return category;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public AuditAssociatedItem getObjectItem() {
        return objectItem;
    }

    public OptionalIterable<AuditAssociatedItem> getAssociatedItem() {
        return associatedItem;
    }

    public OptionalIterable<AuditChangedValue> getChangedValues() {
        return changedValues;
    }

    protected Objects.ToStringHelper getToStringHelper() {
        return Objects.toStringHelper(this).
                add("id", id).
                add("summary", summary).
                add("remoteAddress", remoteAddress).
                add("created", created).
                add("category", category).
                add("objectItem", objectItem).
                add("associatedItem", associatedItem).
                add("changedValues", changedValues);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AuditRecord) {
            final AuditRecord that = (AuditRecord) o;
            return  Objects.equal(this.id, that.id)
                    && Objects.equal(this.summary, that.summary)
                    && Objects.equal(this.remoteAddress, that.remoteAddress)
                    && Objects.equal(this.created, that.created)
                    && Objects.equal(this.category, that.category)
                    && Objects.equal(this.objectItem, that.objectItem)
                    && Objects.equal(this.associatedItem, that.associatedItem)
                    && Objects.equal(this.changedValues, that.changedValues);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, summary, remoteAddress, created, category, objectItem, associatedItem, changedValues);
    }

}
