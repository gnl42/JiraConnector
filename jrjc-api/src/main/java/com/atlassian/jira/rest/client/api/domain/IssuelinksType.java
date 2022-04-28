package com.atlassian.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;


/**
 * @since v0.5
 */
public class IssuelinksType extends AddressableNamedEntity {
    private final String id;
    private final String inward;
    private final String outward;

    public IssuelinksType(URI self, String id, String name, String inward, String outward) {
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
    public boolean equals(Object obj) {
        if (obj instanceof IssuelinksType) {
            IssuelinksType that = (IssuelinksType) obj;
            return super.equals(obj) && Objects.equals(this.id, that.id)
                    && Objects.equals(this.inward, that.inward)
                    && Objects.equals(this.outward, that.outward);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, inward, outward);
    }

}
