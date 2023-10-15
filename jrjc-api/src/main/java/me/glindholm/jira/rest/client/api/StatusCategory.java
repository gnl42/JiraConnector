package me.glindholm.jira.rest.client.api;

import java.net.URI;
import java.util.Objects;

import me.glindholm.jira.rest.client.api.domain.AddressableNamedEntity;

/**
 * Basic information about a JIRA issue status category
 *
 */
public class StatusCategory extends AddressableNamedEntity implements IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String key;
    private final String colorName;

    public StatusCategory(final URI self, final String name, final Long id, final String key, final String colorName) {
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
        return "StatusCategory [id=" + id + ", key=" + key + ", colorName=" + colorName + ", " + super.toString() + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final StatusCategory that) {
            return super.equals(obj) && Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(colorName, that.colorName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, key, colorName);
    }
}
