package me.glindholm.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

/**
 * @since v0.5
 */
public class IssuelinksType extends AddressableNamedEntity {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String inward;
    private final String outward;

    public IssuelinksType(final URI self, final String id, final String name, final String inward, final String outward) {
        super(self, name);
        this.id = id;
        this.inward = inward;
        this.outward = outward;
    }

    public String getId() {
        return id;
    }

    public String getInward() {
        return inward;
    }

    public String getOutward() {
        return outward;
    }

    @Override
    public String toString() {
        return "IssuelinksType [id=" + id + ", inward=" + inward + ", outward=" + outward + ", " + super.toString() + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IssuelinksType that) {
            return super.equals(obj) && Objects.equals(id, that.id) && Objects.equals(inward, that.inward) && Objects.equals(outward, that.outward);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, inward, outward);
    }

}
