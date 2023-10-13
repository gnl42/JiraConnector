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
import java.util.Objects;

import me.glindholm.jira.rest.client.api.AddressableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Basic information about a JIRA project's role.
 */
public class BasicProjectRole implements Serializable, AddressableEntity, NamedEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final String name;

    public BasicProjectRole(URI self, String name) {
        this.self = self;
        this.name = name;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    /**
     * @return the name of this project role.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BasicProjectRole) {
            final BasicProjectRole that = (BasicProjectRole) o;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.name, that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), self, name);
    }

    @Override
    public String toString() {
        return "BasicProjectRole [self=" + self + ", name=" + name + "]";
    }

    protected String getToStringHelper() {
        return toString();
    }
}
