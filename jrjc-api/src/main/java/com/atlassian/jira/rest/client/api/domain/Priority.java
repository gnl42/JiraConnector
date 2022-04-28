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

package com.atlassian.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Complete information about a JIRA issue priority
 *
 * @since v0.1
 */
public class Priority extends BasicPriority {
    @Override
    public String toString() {
        return "Priority [statusColor=" + statusColor + ", description=" + description + ", iconUrl=" + iconUrl + ", " + super.toString() + "]";
    }

    private final String statusColor;
    private final String description;
    private final URI iconUrl;

    public Priority(URI self, @Nullable Long id, String name, String statusColor, String description, URI iconUri) {
        super(self, id, name);
        this.statusColor = statusColor;
        this.description = description;
        this.iconUrl = iconUri;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public String getDescription() {
        return description;
    }

    public URI getIconUri() {
        return iconUrl;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Priority) {
            Priority that = (Priority) obj;
            return super.equals(obj) && Objects.equals(this.description, that.description)
                    && Objects.equals(this.statusColor, that.statusColor)
                    && Objects.equals(this.iconUrl, that.iconUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, statusColor, iconUrl);
    }

}
