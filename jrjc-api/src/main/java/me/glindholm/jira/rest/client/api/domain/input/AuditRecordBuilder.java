package me.glindholm.jira.rest.client.api.domain.input;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import me.glindholm.jira.rest.client.api.domain.AuditAssociatedItem;
import me.glindholm.jira.rest.client.api.domain.AuditChangedValue;
import me.glindholm.jira.rest.client.api.domain.AuditRecordInput;

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

    public AuditRecordBuilder(final String category, final String summary) {
        this.category = category;
        this.summary = summary;
    }

    public AuditRecordBuilder setObject(final AuditAssociatedItem objectItem) {
        this.objectItem = objectItem;
        return this;
    }

    public AuditRecordBuilder setObject(@Nullable final String id, final String name, final String typeName) {
        this.objectItem = new AuditAssociatedItem(id, name, typeName, null, null);
        return this;
    }

    public AuditRecordBuilder setChangedValues(final List<AuditChangedValue> values) {
        this.values = ImmutableList.copyOf(values);
        return this;
    }

    public AuditRecordBuilder setAssociatedItems(final List<AuditAssociatedItem> associatedItems) {
        this.associatedItems = ImmutableList.copyOf(associatedItems);
        return this;
    }

    @Nonnull
    public AuditRecordInput build() {
        return new AuditRecordInput(category, summary, objectItem, associatedItems, values);
    }
}
