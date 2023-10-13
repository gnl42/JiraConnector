package me.glindholm.jira.rest.client.api.domain;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Item that can be associated with Audit Record. Represents additional
 * information about item related to record like user, group or schema.
 *
 * @since v2.0
 */
public class AuditAssociatedItem {

    @Nullable
    private final String id;

    @Override
    public String toString() {
        return "AuditAssociatedItem [id=" + id + ", name=" + name + ", typeName=" + typeName + ", parentId=" + parentId + ", parentName=" + parentName + "]";
    }

    private final String name;

    private final String typeName;

    @Nullable
    private final String parentId;

    @Nullable
    private final String parentName;

    public AuditAssociatedItem(final String id, final String name, final String typeName, final String parentId, final String parentName) {
        this.id = id;
        this.name = name;
        this.typeName = typeName;
        this.parentId = parentId;
        this.parentName = parentName;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getTypeName() {
        return typeName;
    }

    @Nullable
    public String getParentId() {
        return parentId;
    }

    @Nullable
    public String getParentName() {
        return parentName;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AuditAssociatedItem) {
            final AuditAssociatedItem that = (AuditAssociatedItem) o;
            return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(parentId, that.parentId)
                    && Objects.equals(parentName, that.parentName) && Objects.equals(typeName, that.typeName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, typeName, typeName, parentId, parentName);
    }

}
