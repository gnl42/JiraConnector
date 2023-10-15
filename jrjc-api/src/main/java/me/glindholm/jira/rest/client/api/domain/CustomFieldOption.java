/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.api.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents Custom Field Option
 *
 * @since v1.0
 */
public class CustomFieldOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final Long id;
    private final String value;
    private final List<CustomFieldOption> children;
    @Nullable
    private final CustomFieldOption child;

    public CustomFieldOption(final Long id, final URI self, final String value, final List<CustomFieldOption> children,
            @Nullable final CustomFieldOption child) {
        this.value = value;
        this.id = id;
        this.self = self;
        this.children = children;
        this.child = child;
    }

    public URI getSelf() {
        return self;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public List<CustomFieldOption> getChildren() {
        return children;
    }

    @Nullable
    public CustomFieldOption getChild() {
        return child;
    }

    /**
     * Returns ToStringHelper with all fields inserted. Override this method to insert additional
     * fields.
     *
     * @return ToStringHelper
     */
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "CustomFieldOption [self=" + self + ", id=" + id + ", value=" + value + ", children=" + children + ", child=" + child + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, id, value, children, child);
    }

    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final CustomFieldOption other = (CustomFieldOption) obj;
        return Objects.equals(self, other.self) && Objects.equals(id, other.id) && Objects.equals(value, other.value)
                && Objects.equals(children, other.children) && Objects.equals(child, other.child);
    }
}
