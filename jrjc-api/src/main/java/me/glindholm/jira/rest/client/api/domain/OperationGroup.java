/*
 * Copyright (C) 2014 Atlassian
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
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

/**
 * Represents operations group
 *
 * @since 2.0
 */
public class OperationGroup implements Serializable, Operation {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final String id;
    @Nullable
    private final OperationHeader header;
    private final Iterable<OperationLink> links;
    private final Iterable<OperationGroup> groups;
    @Nullable
    private final Integer weight;

    public OperationGroup(@Nullable final String id, final Iterable<OperationLink> links,
            final Iterable<OperationGroup> groups, @Nullable final OperationHeader header,
            @Nullable final Integer weight) {
        this.id = id;
        this.header = header;
        this.links = links;
        this.groups = groups;
        this.weight = weight;
    }

    @Override
    @Nullable
    public String getId() {
        return id;
    }

    @Override
    public <T> Optional<T> accept(final OperationVisitor<T> visitor) {
        final Optional<T> result = visitor.visit(this);
        if (result.isPresent()) {
            return result;
        } else {
            final Iterable<Operation> operations = Iterables.concat(
                    header != null ? Collections.singleton(header) : Collections.<Operation>emptyList(),
                            links, groups);
            return accept(operations, visitor);
        }
    }

    static <T> Optional<T> accept(final Iterable<? extends Operation> operations, final OperationVisitor<T> visitor) {
        for (Operation operation : operations) {
            Optional<T> result = operation.accept(visitor);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Nullable
    public OperationHeader getHeader() {
        return header;
    }

    public Iterable<OperationLink> getLinks() {
        return links;
    }

    public Iterable<OperationGroup> getGroups() {
        return groups;
    }

    @Nullable
    public Integer getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, header, links, groups, weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final OperationGroup other = (OperationGroup) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.header, other.header)
                && Iterables.elementsEqual(this.links, other.links)
                && Iterables.elementsEqual(this.groups, other.groups)
                && Objects.equals(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return "OperationGroup [id=" + id + ", header=" + header + ", links=" + links + ", groups=" + groups + ", weight=" + weight + "]";
    }
}
