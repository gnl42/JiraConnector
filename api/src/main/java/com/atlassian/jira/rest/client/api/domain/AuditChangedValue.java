package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO: Document this class / interface here
 *
 * @since v2.0
 */
public class AuditChangedValue {

    @Nonnull
    private final String fieldName;

    @Nullable
    private final String changedTo;

    @Nullable
    private final String changedFrom;

    public AuditChangedValue(String fieldName, String changedTo, String changedFrom) {
        this.fieldName = fieldName;
        this.changedTo = changedTo;
        this.changedFrom = changedFrom;
    }

    @Nonnull
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

    protected Objects.ToStringHelper getToStringHelper() {
        return Objects.toStringHelper(this).
                add("fieldName", fieldName).
                add("changedFrom", changedFrom).
                add("changedTo", changedTo);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AuditChangedValue) {
            AuditChangedValue that = (AuditChangedValue) o;
            return  Objects.equal(this.fieldName, that.fieldName)
                    && Objects.equal(this.changedFrom, that.changedFrom)
                    && Objects.equal(this.changedTo, that.changedTo);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldName, changedFrom, changedTo);
    }

}
