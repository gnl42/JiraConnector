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
        return "StatusCategory [id=" + id + ", key=" + key + ", colorName=" + colorName + ", " + super.toString() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatusCategory) {
            StatusCategory that = (StatusCategory) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, that.id)
                    && Objects.equals(this.key, that.key)
                    && Objects.equals(this.colorName, that.colorName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, key, colorName);
    }
}
