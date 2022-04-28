package com.atlassian.jira.rest.client.api.domain;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents record from JIRA Audit Log.
 *
 * @since v2.0
 */
public class AuditRecordInput {

    @Override
    public String toString() {
        return "AuditRecordInput [summary=" + summary + ", category=" + category + ", objectItem=" + objectItem + ", associatedItem=" + associatedItem
                + ", changedValues=" + changedValues + "]";
    }

    private final String summary;

    private final String category;

    @Nullable
    private final AuditAssociatedItem objectItem;

    @Nullable
    private final Iterable<AuditAssociatedItem> associatedItem;

    @Nullable
    private final Iterable<AuditChangedValue> changedValues;

    public AuditRecordInput(final String category, final String summary,
            @Nullable final AuditAssociatedItem objectItem,
            @Nullable final Iterable<AuditAssociatedItem> associatedItem,
            @Nullable final Iterable<AuditChangedValue> changedValues) {
        this.summary = summary;
        this.category = category;
        this.objectItem = objectItem;
        this.associatedItem = associatedItem;
        this.changedValues = changedValues;
    }

    public String getSummary() {
        return summary;
    }

    public String getCategory() {
        return category;
    }

    public AuditAssociatedItem getObjectItem() {
        return objectItem;
    }

    public Iterable<AuditAssociatedItem> getAssociatedItems() {
        return associatedItem;
    }

    public Iterable<AuditChangedValue> getChangedValues() {
        return changedValues;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AuditRecordInput) {
            final AuditRecordInput that = (AuditRecordInput) o;
            return Objects.equals(this.summary, that.summary)
                    && Objects.equals(this.category, that.category)
                    && Objects.equals(this.objectItem, that.objectItem)
                    && Objects.equals(this.associatedItem, that.associatedItem)
                    && Objects.equals(this.changedValues, that.changedValues);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary, category, objectItem, associatedItem, changedValues);
    }

}
