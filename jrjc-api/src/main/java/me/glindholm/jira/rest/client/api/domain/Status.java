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
import me.glindholm.jira.rest.client.api.StatusCategory;

/**
 * Basic information about a JIRA issue status
 *
 * @since v0.1
 */
public class Status extends AddressableNamedEntity implements Serializable, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String description;
    private final URI iconUrl;
    private final StatusCategory statusCategory;

    public Status(URI self, final Long id, final String name, final String description, final URI iconUrl, final StatusCategory statusCategory) {
        super(self, name);
        this.id = id;
        this.description = description;
        this.iconUrl = iconUrl;
        this.statusCategory = statusCategory;
    }

    @Override
    public String toString() {
        return "Status [id=" + id + ", description=" + description + ", iconUrl=" + iconUrl + ", statusCategory=" + statusCategory + ", =" + super.toString()
        + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Status) {
            Status that = (Status) obj;
            return super.equals(obj)
                    && Objects.equals(this.id, that.id)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.iconUrl, that.iconUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, description, iconUrl);
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public URI getIconUrl() {
        return iconUrl;
    }

    public StatusCategory getStatusCategory() {
        return statusCategory;
    }
}
