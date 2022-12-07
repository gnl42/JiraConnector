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

package me.glindholm.jira.rest.client.api;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a resource which can be expandable - that is REST API is capable of sending just the number
 * of child resources or when the entity is expanded, also the child resources themselves
 *
 * @since v0.1
 */
public class ExpandableProperty<T> {
    private final int size;

    public ExpandableProperty(int size) {
        this.size = size;
        items = null;
    }

    public ExpandableProperty(int size, @Nullable List<T> items) {
        this.size = size;
        this.items = items;
    }

    public ExpandableProperty(List<T> items) {
        this.size = items.size();
        this.items = items;
    }

    public int getSize() {
        return size;
    }

    @Nullable
    final private List<T> items;

    @Nullable
    public List<T> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ExpandableProperty [size=" + size + ", items=" + items + "]";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpandableProperty) {
            ExpandableProperty<T> that = (ExpandableProperty<T>) obj;
            return Objects.equals(this.size, that.size)
                    && Objects.equals(this.items, that.items);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, items);
    }
}
