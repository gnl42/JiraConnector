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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import me.glindholm.jira.rest.client.api.IssueRestClient;

/**
 * Represents operations returned for expand {@link IssueRestClient.Expandos#OPERATIONS}
 *
 * @since 2.0
 */
public class Operations {
    private final List<OperationGroup> linkGroups;

    public Operations(final List<OperationGroup> linkGroups) {
        this.linkGroups = linkGroups;
    }

    public List<OperationGroup> getLinkGroups() {
        return linkGroups;
    }

    public <T> Optional<T> accept(final OperationVisitor<T> visitor) {
        return OperationGroup.accept(getLinkGroups(), visitor);
    }

    public Operation getOperationById(final String operationId) {
        return accept(operation -> operationId.equals(operation.getId()) ? Optional.of(operation) : Optional.<Operation>empty()).orElse(null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkGroups);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Operations other = (Operations) obj;
        return linkGroups.equals(other.linkGroups);
    }

    @Override
    public String toString() {
        return "Operations [linkGroups=" + linkGroups + "]";
    }
}
