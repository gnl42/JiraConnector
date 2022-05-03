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

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;

import me.glindholm.jira.rest.client.api.ExpandableResource;
import me.glindholm.jira.rest.client.api.OptionalIterable;

/**
 * Complete information about single JIRA project.
 * Many REST resources instead include just @{}BasicProject
 *
 * @since v0.1
 */
public class Project extends BasicProject implements ExpandableResource {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Iterable<String> expandos;
    @Nullable
    private final String description;
    private final BasicUser lead;
    @Nullable
    private final URI uri;
    private final Collection<Version> versions;

    @Override
    public String toString() {
        return "Project [expandos=" + expandos + ", description=" + description + ", lead=" + lead + ", uri=" + uri + ", versions=" + versions + ", components="
                + components + ", issueTypes=" + issueTypes + ", projectRoles=" + projectRoles + ", " + super.toString() + "]";
    }

    private final Collection<BasicComponent> components;
    private final OptionalIterable<IssueType> issueTypes;
    private final Collection<BasicProjectRole> projectRoles;

    public Project(final Iterable<String> expandos, URI self, String key, Long id, String name, String description, BasicUser lead, URI uri,
            Collection<Version> versions, Collection<BasicComponent> components,
            OptionalIterable<IssueType> issueTypes, Collection<BasicProjectRole> projectRoles) {
        super(self, key, id, name);
        this.expandos = expandos;
        this.description = description;
        this.lead = lead;
        this.uri = uri;
        this.versions = versions;
        this.components = components;
        this.issueTypes = issueTypes;
        this.projectRoles = projectRoles;
    }

    /**
     * @return description provided for this project or null if there is no description specific for this project.
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * @return the person who leads this project
     */
    public BasicUser getLead() {
        return lead;
    }

    /**
     * @return user-defined URI to a web page for this project, or <code>null</code> if not defined.
     */
    @Nullable
    public URI getUri() {
        return uri;
    }

    /**
     * @return versions defined for this project
     */
    public Iterable<Version> getVersions() {
        return versions;
    }

    /**
     * @return components defined for this project
     */
    public Iterable<BasicComponent> getComponents() {
        return components;
    }

    /**
     * Getter for issueTypes
     *
     * @return the issueTypes defined for this project
     */
    public OptionalIterable<IssueType> getIssueTypes() {
        return issueTypes;
    }

    /**
     * @return basic definition of this project's roles.
     */
    public Iterable<BasicProjectRole> getProjectRoles() {
        return projectRoles;
    }

    @Override
    public Iterable<String> getExpandos() {
        return expandos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Project) {
            Project that = (Project) o;
            return super.equals(that)
                    && Objects.equals(this.lead, that.lead)
                    && Objects.equals(this.uri, that.uri)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.components, that.components)
                    && Objects.equals(this.issueTypes, that.issueTypes)
                    && Objects.equals(this.versions, that.versions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, lead, uri);
    }
}
