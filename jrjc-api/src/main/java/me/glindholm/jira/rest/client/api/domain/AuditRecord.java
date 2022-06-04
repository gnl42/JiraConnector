package me.glindholm.jira.rest.client.api.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;


/**
 * Represents record from JIRA Audit Log.
 *
 * @since v2.0
 */
public class AuditRecord {

    @Override
    public String toString() {
        return "AuditRecord [id=" + id + ", summary=" + summary + ", category=" + category + ", eventSource=" + eventSource + ", authorKey=" + authorKey
                + ", remoteAddress=" + remoteAddress + ", objectItem=" + objectItem + ", associatedItem=" + associatedItem + ", changedValues=" + changedValues
                + "]";
    }

    private final Long id;

    private final String summary;

    private final OffsetDateTime created;

    private final String category;

    private final String eventSource;

    @Nullable
    private final String authorKey;

    @Nullable
    private final String remoteAddress;

    @Nullable
    private final AuditAssociatedItem objectItem;

    private final List<AuditAssociatedItem> associatedItem;

    private final List<AuditChangedValue> changedValues;

    public AuditRecord(final Long id, final String summary, @Nullable final String remoteAddress,
            final OffsetDateTime created, final String category, String eventSource,
            @Nullable final String authorKey,
            @Nullable final AuditAssociatedItem objectItem,
            final List<AuditAssociatedItem> associatedItem,
            final List<AuditChangedValue> changedValues) {
        this.id = id;
        this.summary = summary;
        this.remoteAddress = remoteAddress;
        this.created = created;
        this.category = category;
        this.eventSource = eventSource;
        this.authorKey = authorKey;
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

    public OffsetDateTime getCreated() {
        return created;
    }

    public String getCategory() {
        return category;
    }

    public String getEventSource() {
        return eventSource;
    }

    @Nullable
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Nullable
    public String getAuthorKey() {
        return authorKey;
    }

    @Nullable
    public AuditAssociatedItem getObjectItem() {
        return objectItem;
    }

    public List<AuditAssociatedItem> getAssociatedItems() {
        return associatedItem;
    }

    public List<AuditChangedValue> getChangedValues() {
        return changedValues;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AuditRecord) {
            final AuditRecord that = (AuditRecord) o;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.summary, that.summary)
                    && Objects.equals(this.remoteAddress, that.remoteAddress)
                    && Objects.equals(this.created, that.created)
                    && Objects.equals(this.category, that.category)
                    && Objects.equals(this.authorKey, that.authorKey)
                    && Objects.equals(this.objectItem, that.objectItem)
                    && Objects.equals(this.associatedItem, that.associatedItem)
                    && Objects.equals(this.changedValues, that.changedValues);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, summary, remoteAddress, created, category, authorKey, objectItem, associatedItem, changedValues);
    }

}
