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
 * Cookie used for maintaining the session for this user
 *
 * @since v0.1
 */
public class SessionCookie implements Serializable, NamedEntity {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    public SessionCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SessionCookie [name=" + name + ", value=" + value + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SessionCookie) {
            SessionCookie that = (SessionCookie) obj;
            return Objects.equals(this.name, that.name)
                    && Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

}
