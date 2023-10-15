package me.glindholm.jira.rest.client.api.domain.input;

import java.io.Serializable;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;

public class PropertyInput implements Serializable, IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String key;
    private final String value;

    public PropertyInput(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getId() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PropertyInput that = (PropertyInput) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
