package com.atlassian.jira.rest.client.api;

import com.atlassian.jira.rest.client.api.domain.AddressableNamedEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * Basic information about a JIRA issue status category
 *
 */
public class StatusCategory extends AddressableNamedEntity implements IdentifiableEntity<Long> {

    private final Long id;
    private final String key;
    private final String colorName;

    public StatusCategory(URI self, String name, Long id, String key, String colorName) {
        super(self, name);
        this.id = id;
        this.key = key;
        this.colorName = colorName;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getColorName() {
        return colorName;
    }

    @Override
    public String toString() {
        return getToStringHelper().
                add("id", id).
                add("key", key).
                add("colorName", colorName).
                toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatusCategory) {
            StatusCategory that = (StatusCategory) obj;
            return super.equals(obj)
                    && Objects.equal(this.id, that.id)
                    && Objects.equal(this.key, that.key)
                    && Objects.equal(this.colorName, that.colorName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), id, key, colorName);
    }
}
