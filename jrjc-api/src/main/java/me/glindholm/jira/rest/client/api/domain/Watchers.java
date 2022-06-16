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

import java.util.List;
import java.util.Objects;

/**
 * Complete information about the watchers of given issue
 *
 * @since v0.1
 */
public class Watchers extends BasicWatchers {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Watchers [watchers=" + users + ", " + super.toString() + "]";
    }

    private final List<BasicUser> users;

    public Watchers(BasicWatchers basicWatchers, List<BasicUser> users) {
        super(basicWatchers.getSelf(), basicWatchers.isWatching(), basicWatchers.getNumWatchers());
        this.users = users;
    }

    public List<BasicUser> getUsers() {
        return users;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), users);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Watchers) {
            final Watchers that = (Watchers) obj;
            return super.equals(that) && Objects.equals(this.users, that.users);
        }
        return false;
    }
}
