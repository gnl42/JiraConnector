package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class Permissions {
    /**
     * Permission key for ability to log work done against an issue. Only useful if Time Tracking is turned on.
     */
    public static final String WORK_ISSUE = "WORK_ISSUE";
    private final Map<String, Permission> permissionMap;

    public Permissions(@Nonnull Iterable<Permission> permissions) {
        this.permissionMap = Maps.uniqueIndex(permissions, Permission.TO_KEY);
    }

    @Nonnull
    public Map<String, Permission> getPermissionMap() {
        return permissionMap;
    }

    public boolean havePermission(String permissionKey) {
        Permission permission = getPermission(permissionKey);
        return (permission != null && permission.havePermission());
    }

    @Nullable
    public Permission getPermission(String permissionKey) {
        return permissionMap.get(permissionKey);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("permissionMap", permissionMap)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permissions that = (Permissions) o;

        if (!permissionMap.equals(that.permissionMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return permissionMap.hashCode();
    }
}
