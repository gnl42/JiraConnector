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

import javax.annotation.Nullable;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;

/**
 * Basic information about selected priority
 *
 * @since v0.1
 */
public class BasicPriority extends AddressableNamedEntity implements Serializable, IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Long id;

    public BasicPriority(URI self, @Nullable Long id, String name) {
        super(self, name);
        this.id = id;
    }

    /**
     * Getter for id
     *
     * @return the id
     */
    @Override
    @Nullable
    public Long getId() {
        return id;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "BasicPriority [id=" + id + ", " + super.toString() + "]";
    }
}
