package me.glindholm.jira.rest.client.api.domain;

import java.util.Objects;

public class RemotelinkStatus {
    private final Boolean resolved;
    private final RemotelinkIcon icon;

    public RemotelinkStatus(final Boolean resolved, final RemotelinkIcon icon) {
        this.resolved = resolved;
        this.icon = icon;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public RemotelinkIcon getIcon() {
        return icon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, resolved);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RemotelinkStatus)) {
            return false;
        }
        final RemotelinkStatus other = (RemotelinkStatus) obj;
        return Objects.equals(icon, other.icon) && Objects.equals(resolved, other.resolved);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemotelinkStatus [resolved=").append(resolved).append(", icon=").append(icon).append("]");
        return builder.toString();
    }
}
