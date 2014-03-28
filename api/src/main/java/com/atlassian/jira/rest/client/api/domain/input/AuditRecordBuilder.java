package com.atlassian.jira.rest.client.api.domain.input;

import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.domain.AuditAssociatedItem;
import com.atlassian.jira.rest.client.api.domain.AuditChangedValue;
import com.atlassian.jira.rest.client.api.domain.AuditRecordInput;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Input used for creating new Audit records.
 *
 * @since v2.0.0
 */
public class AuditRecordBuilder {
    String category;
    String summary;
    AuditAssociatedItem objectItem;
    List<AuditChangedValue> values;
    List<AuditAssociatedItem> associatedItems;

    public AuditRecordBuilder(String category, String summary) {
        this.category = category;
        this.summary = summary;
    }

    public AuditRecordBuilder setObject(AuditAssociatedItem objectItem) {
        this.objectItem = objectItem;
        return this;
    }

    public AuditRecordBuilder setObject(@Nullable String id, String name, String typeName) {
        this.objectItem = new AuditAssociatedItem(id, name, typeName, null, null);
        return this;
    }

    public AuditRecordBuilder setChangedValues(Iterable<AuditChangedValue> values) {
        this.values = ImmutableList.copyOf(values);
        return this;
    }

    public AuditRecordBuilder setAssociatedItems(Iterable<AuditAssociatedItem> associatedItems) {
        this.associatedItems = ImmutableList.copyOf(associatedItems);
        return this;
    }

    @Nonnull
    public AuditRecordInput build() {
        return new AuditRecordInput(category, summary, objectItem, associatedItems, values);
    }
}
