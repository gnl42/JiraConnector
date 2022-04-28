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
package com.atlassian.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

import com.atlassian.jira.rest.client.api.IdentifiableEntity;

/**
 * Represents Filter
 *
 * @since 2.0
 */
public class Filter extends AddressableNamedEntity implements IdentifiableEntity<Long> {
    @Override
    public String toString() {
        return "Filter [id=" + id + ", description=" + description + ", jql=" + jql + ", viewUrl=" + viewUrl + ", searchUrl=" + searchUrl + ", owner=" + owner
                + ", favourite=" + favourite + "]";
    }

    private final Long id;
    private final String description;
    private final String jql;
    private final URI viewUrl;
    private final URI searchUrl;
    private final BasicUser owner;
    private final boolean favourite;

    public Filter(URI self, Long id, String name, String description, String jql, URI viewUrl, URI searchUrl, BasicUser owner, boolean favourite) {
        super(self, name);
        this.id = id;
        this.description = description;
        this.jql = jql;
        this.viewUrl = viewUrl;
        this.searchUrl = searchUrl;
        this.owner = owner;
        this.favourite = favourite;
    }

    @Override
    public Long getId() {
        return id;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getJql() {
        return jql;
    }

    @SuppressWarnings("UnusedDeclaration")
    public URI getViewUrl() {
        return viewUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public URI getSearchUrl() {
        return searchUrl;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BasicUser getOwner() {
        return owner;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isFavourite() {
        return favourite;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Filter) {
            Filter that = (Filter) obj;
            return super.equals(that)
                    && Objects.equals(this.id, that.id)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.jql, that.jql)
                    && Objects.equals(this.viewUrl, that.viewUrl)
                    && Objects.equals(this.searchUrl, that.searchUrl)
                    && Objects.equals(this.owner, that.owner)
                    && Objects.equals(this.favourite, that.favourite);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, description, jql, searchUrl, viewUrl, owner, favourite);
    }
}
