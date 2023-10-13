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
import java.util.Objects;

import me.glindholm.jira.rest.client.api.AddressableEntity;

/**
 * Basic information about watchers of a JIRA issue
 *
 * @since v0.1
 */
public class BasicWatchers implements Serializable, AddressableEntity {
    private static final long serialVersionUID = 1L;

    private final URI self;
    private final boolean isWatching;
    private final int numWatchers;

    public BasicWatchers(URI self, boolean watching, int numWatchers) {
        this.self = self;
        isWatching = watching;
        this.numWatchers = numWatchers;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    public boolean isWatching() {
        return isWatching;
    }

    public int getNumWatchers() {
        return numWatchers;
    }

    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "BasicWatchers [self=" + self + ", isWatching=" + isWatching + ", numWatchers=" + numWatchers + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicWatchers) {
            final BasicWatchers that = (BasicWatchers) obj;
            return Objects.equals(this.self, that.self)
                    && Objects.equals(this.isWatching, that.isWatching)
                    && Objects.equals(this.numWatchers, that.numWatchers);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, isWatching, numWatchers);
    }
}
