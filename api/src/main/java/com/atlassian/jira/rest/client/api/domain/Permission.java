package com.atlassian.jira.rest.client.api.domain;

import com.atlassian.jira.rest.client.api.IdentifiableEntity;
import com.atlassian.jira.rest.client.api.NamedEntity;
import com.google.common.base.Function;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Permission implements NamedEntity, IdentifiableEntity<Integer> {
    @Nonnull private final Integer id;
    @Nonnull private final String key;
    @Nonnull private final String name;
    private final String description;
    private final boolean havePermission;

    public Permission(@Nonnull Integer id, @Nonnull String key, @Nonnull String name, String description, boolean havePermission) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.havePermission = havePermission;
    }

    @Nonnull
    public Integer getId() {
        return id;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean havePermission() {
        return havePermission;
    }

    protected Objects.ToStringHelper getToStringHelper() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .add("name", name)
                .add("description", description)
                .add("havePermission", havePermission);
    }

    @Override
    public String toString() {
        return getToStringHelper().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (havePermission != that.havePermission) return false;
        if (!id.equals(that.id)) return false;
        if (!key.equals(that.key)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (havePermission ? 1 : 0);
        return result;
    }

    public static final Function<Permission, String> TO_KEY = new Function<Permission, String>() {
        @Override
        public String apply(Permission input) {
            return input.getKey();
        }
    };
}
