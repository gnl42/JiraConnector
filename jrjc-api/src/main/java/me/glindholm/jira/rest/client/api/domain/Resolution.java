/*
 * Copyright (C) 2010 Atlassian
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
import java.util.Objects;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;

/**
 * Basic information (served together with the issue) about current resolution.
 *
 * @since v0.1
 */
public class Resolution extends AddressableNamedEntity implements Serializable, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String description;

    public Resolution(final URI self, final Long id, final String name, final String description) {
        super(self, name);
        this.id = id;
        this.description = description;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Resolution [id=" + id + ", description=" + description + ", " + super.toString() + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Resolution that) {
            return super.equals(obj) && Objects.equals(id, that.id) && Objects.equals(description, that.description);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, description);
    }
}
