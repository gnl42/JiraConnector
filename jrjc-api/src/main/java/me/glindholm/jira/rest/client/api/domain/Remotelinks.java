package me.glindholm.jira.rest.client.api.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Remotelinks {
    private final Map<String, List<Remotelink>> remotelinks;

    public Remotelinks(final Map<String, List<Remotelink>> remotelinks) {
        this.remotelinks = remotelinks;
    }

    public Map<String, List<Remotelink>> getRemotelinks() {
        return remotelinks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remotelinks);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Remotelinks)) {
            return false;
        }
        final Remotelinks other = (Remotelinks) obj;
        return Objects.equals(remotelinks, other.remotelinks);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Remotelinks [remotelinks=").append(remotelinks).append("]");
        return builder.toString();
    }
}
