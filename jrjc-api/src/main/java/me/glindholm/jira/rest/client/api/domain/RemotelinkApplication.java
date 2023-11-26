package me.glindholm.jira.rest.client.api.domain;

import java.util.Objects;

public class RemotelinkApplication {
    private final String type;
    private final String name;

    public RemotelinkApplication(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final RemotelinkApplication other)) {
            return false;
        }
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemotelinkApplication [type=").append(type).append(", name=").append(name).append("]");
        return builder.toString();
    }

}
