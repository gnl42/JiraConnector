/*
 * Copyright (C) 2018 Atlassian
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

package me.glindholm.jira.rest.client.api.domain.input;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Details about user to be created or updated;
 *
 * @since v5.1.0
 */
public class UserInput {
    @Nullable
    private final String key;
    @Nullable
    private final String name;
    @Nullable
    private final String password;
    @Nullable
    private final String emailAddress;
    @Nullable
    private final String displayName;
    @Nullable
    private final String notification;
    @Nullable
    private final List<String> applicationKeys;

    public UserInput(@Nullable final String key, @Nullable final String name, @Nullable final String password, @Nullable final String emailAddress,
            @Nullable final String displayName, @Nullable final String notification, final List<String> applicationKeys) {
        this.key = key;
        this.name = name;
        this.password = password;
        this.emailAddress = emailAddress;
        this.displayName = displayName;
        this.notification = notification;
        this.applicationKeys = applicationKeys;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Nullable
    public String getEmailAddress() {
        return emailAddress;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public String getNotification() {
        return notification;
    }

    @Nullable
    public List<String> getApplicationKeys() {
        return applicationKeys;
    }
}
