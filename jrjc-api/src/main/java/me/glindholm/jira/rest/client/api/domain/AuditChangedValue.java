package me.glindholm.jira.rest.client.api.domain;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a value that has changed in object related to Audit Record.
 *
 * @since v2.0
 */
public class AuditChangedValue {

    @Override
    public String toString() {
        return "AuditChangedValue [fieldName=" + fieldName + ", changedTo=" + changedTo + ", changedFrom=" + changedFrom + "]";
    }

    private final String fieldName;

    @Nullable
    private final String changedTo;

    @Nullable
    private final String changedFrom;

    public AuditChangedValue(final String fieldName, @Nullable final String changedTo, @Nullable final String changedFrom) {
        this.fieldName = fieldName;
        this.changedTo = changedTo;
        this.changedFrom = changedFrom;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Nullable
    public String getChangedTo() {
        return changedTo;
    }

    @Nullable
    public String getChangedFrom() {
        return changedFrom;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final AuditChangedValue that) {
            return Objects.equals(fieldName, that.fieldName) && Objects.equals(changedFrom, that.changedFrom) && Objects.equals(changedTo, that.changedTo);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, changedFrom, changedTo);
    }

}
