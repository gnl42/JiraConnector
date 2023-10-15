package me.glindholm.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

public class Remotelink {
    private final String globalId;
    private final String relationship;

    private final Long id;
    private final URI self;

    private final RemotelinkApplication application;

    private final RemotelinkObject object;

    public Remotelink(final String globalId, final String relationship, final Long id, final URI self, final RemotelinkApplication application,
            final RemotelinkObject object) {
        this.globalId = globalId;
        this.relationship = relationship;
        this.id = id;
        this.self = self;
        this.application = application;
        this.object = object;
    }

    public String getGlobalId() {
        return globalId;
    }

    public String getRelationship() {
        return relationship;
    }

    public Long getId() {
        return id;
    }

    public URI getSelf() {
        return self;
    }

    public RemotelinkApplication getApplication() {
        return application;
    }

    public RemotelinkObject getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, globalId, id, object, relationship, self);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final Remotelink other)) {
            return false;
        }
        return Objects.equals(application, other.application) && Objects.equals(globalId, other.globalId) && Objects.equals(id, other.id)
                && Objects.equals(object, other.object) && Objects.equals(relationship, other.relationship) && Objects.equals(self, other.self);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Remotelink [globalId=").append(globalId).append(", relationship=").append(relationship).append(", id=").append(id).append(", self=")
                .append(self).append(", application=").append(application).append(", object=").append(object).append("]");
        return builder.toString();
    }

}
