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
import java.util.Objects;

import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Type of a link between two JIRA issues
 *
 * @since v0.1
 */
public class IssueLinkType implements Serializable, NamedEntity {
    private static final long serialVersionUID = 1L;

    public enum Direction {
        OUTBOUND, INBOUND
    }

    private final String name;
    private final String description;
    private final Direction direction;

    public IssueLinkType(final String name, final String description, final Direction direction) {
        this.name = name;
        this.description = description;
        this.direction = direction;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "IssueLinkType [name=" + name + ", description=" + description + ", direction=" + direction + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IssueLinkType that) {
            return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(direction, that.direction);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, direction);
    }

}
