/*
 * Copyright (C) 2011 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import me.glindholm.jira.rest.client.api.IdentifiableEntity;

/**
 * Very basic (key and link only) representation of a JIRA issue.
 *
 * @since v0.2
 */
public class BasicIssue implements Serializable, AddressableEntity, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final URI self;

    private final String key;
    private final Long id;

    public BasicIssue(final URI self, final String key, final Long id) {
        this.self = self;
        this.key = key;
        this.id = id;
    }

    /**
     * @return URI of this issue
     */
    @Override
    public URI getSelf() {
        return self;
    }

    /**
     * @return issue key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return issue id
     */
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BasicIssue [self=" + self + ", key=" + key + ", id=" + id + "]";
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final BasicIssue that) {
            return Objects.equals(self, that.self) && Objects.equals(key, that.key) && Objects.equals(id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, key, id);
    }

}
