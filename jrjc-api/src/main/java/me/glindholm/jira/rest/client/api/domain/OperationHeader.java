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
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Represents operations header
 *
 * @since 2.0
 */
public class OperationHeader implements Serializable, Operation {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final String id;
    private final String label;
    @Nullable
    private final String title;
    @Nullable
    private final String iconClass;

    public OperationHeader(@Nullable final String id, final String label, @Nullable final String title, @Nullable final String iconClass) {
        this.id = id;
        this.label = label;
        this.title = title;
        this.iconClass = iconClass;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public <T> Optional<T> accept(final OperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getLabel() {
        return label;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getIconClass() {
        return iconClass;
    }

    @Override
    public String toString() {
        return "OperationHeader [id=" + id + ", label=" + label + ", title=" + title + ", iconClass=" + iconClass + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OperationHeader) {
            OperationHeader that = (OperationHeader) o;
            return Objects.equals(id, that.id)
                    && Objects.equals(label, that.label)
                    && Objects.equals(title, that.title)
                    && Objects.equals(iconClass, that.iconClass);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, title, iconClass);
    }
}
