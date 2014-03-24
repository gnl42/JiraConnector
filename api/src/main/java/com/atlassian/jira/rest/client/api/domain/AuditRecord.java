package com.atlassian.jira.rest.client.api.domain;

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

/**
 * TODO: Document this class / interface here
 *
 * @since v2.0
 */
public class AuditRecord {

    @Nonnull
    private final Long id;

    @Nonnull
    private final String summary;

    @Nonnull
    private final Long created;

    @Nonnull
    private final String category;

    private final String remoteAddress;
    private final AuditAssociatedItem objectItem;
    private final OptionalIterable<AuditAssociatedItem> associatedItem;
    private final OptionalIterable<AuditChangedValue> changedValues;

    public AuditRecord(@Nonnull final Long id, @Nonnull final String summary, final String remoteAddress, @Nonnull final Long created,
                       @Nonnull final String category, final AuditAssociatedItem objectItem,
                       final OptionalIterable<AuditAssociatedItem> associatedItem, final OptionalIterable<AuditChangedValue> changedValues) {
        this.id = id;
        this.summary = summary;
        this.remoteAddress = remoteAddress;
        this.created = created;
        this.category = category;
        this.objectItem = objectItem;
        this.associatedItem = associatedItem;
        this.changedValues = changedValues;
    }

    @Nonnull
    public Long getId() {
        return id;
    }

    @Nonnull
    public String getSummary() {
        return summary;
    }

    @Nonnull
    public Long getCreated() {
        return created;
    }

    @Nonnull
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
