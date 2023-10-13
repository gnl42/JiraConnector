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

import me.glindholm.jira.rest.client.api.IdentifiableEntity;

/**
 * A way to group users (@see RoleActors) with projects. An example would be a global role called "testers". If you
 * have a project X and a project Y, you would then be able to configure different RoleActors in the "testers" role
 * for project X than for project Y. You can use ProjectRole objects as the target of Notification and Permission
 * schemes.
 *
 * @see com.atlassian.jira.security.roles.ProjectRole
 */
@SuppressWarnings("JavadocReference")
public class ProjectRole extends BasicProjectRole implements Serializable, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "ProjectRole [description=" + description + ", actors=" + actors + ", id=" + id + "]";
    }

    private final String description;
    private final List<RoleActor> actors;
    private final long id;

    public ProjectRole(long id, URI self, String name, String description, List<RoleActor> actors) {
        super(self, name);
        this.id = id;
        this.description = description;
        this.actors = actors;
    }

    /**
     * @return description of this project role.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return actors associated with this role.
     */
    public List<RoleActor> getActors() {
        return actors;
    }

    /**
     * @return the unique id for this project role.
     */
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ProjectRole) {
            final ProjectRole that = (ProjectRole) o;
            return super.equals(o)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.actors, that.actors)
                    && Objects.equals(this.id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, actors);
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }
}
