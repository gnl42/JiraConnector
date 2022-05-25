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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import me.glindholm.jira.rest.client.api.ExpandableProperty;

/**
 * Complete information about a single JIRA user
 *
 * @since v0.1
 */
public class User extends BasicUser {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "User [emailAddress=" + emailAddress + ", active=" + active + ", groups=" + groups + ", avatarUris=" + avatarUris + ", timezone=" + timezone
                + ", toString()=" + super.toString() + "]";
    }

    public static final String S16_16 = "16x16";
    public static final String S48_48 = "48x48";

    private final String emailAddress;
    private final boolean active;

    private final ExpandableProperty<String> groups;

    private Map<String, URI> avatarUris;

    /**
     * @since me.glindholm.jira.rest.client.api 0.5, server: 4.4
     */
    @Nullable
    private String timezone;

    public User(URI self, String name, String displayName, String accountId, String emailAddress, boolean active,
            @Nullable ExpandableProperty<String> groups, Map<String, URI> avatarUris, @Nullable String timezone) {
        super(self, name, displayName, accountId);
        Preconditions.checkNotNull(avatarUris.get(S48_48), "At least one avatar URL is expected - for 48x48");
        this.timezone = timezone;
        this.emailAddress = emailAddress;
        this.active = active;
        this.avatarUris = new HashMap<>(avatarUris);
        this.groups = groups;
    }

    public User(URI self, String name, String displayName, String emailAddress, boolean active,
            @Nullable ExpandableProperty<String> groups, Map<String, URI> avatarUris, @Nullable String timezone) {
        this(self, name, displayName, null, emailAddress, true, groups, avatarUris, timezone);
    }

    /**
     * This constructor is used to create an active user per default.
     *
     * @deprecated since v5.1.0. Use {@link #User(URI,String,String,String,boolean,ExpandableProperty,Map,String)} instead.
     */
    @Deprecated
    public User(URI self, String name, String displayName, String emailAddress, @Nullable ExpandableProperty<String> groups,
            Map<String, URI> avatarUris, @Nullable String timezone) {
        this(self, name, displayName, emailAddress, true, groups, avatarUris, timezone);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isActive() {
        return active;
    }

    public URI getAvatarUri() {
        return avatarUris.get(S48_48);
    }

    /**
     * @return user avatar image URI for 16x16 pixels
     * @since 0.5 me.glindholm.jira.rest.client.api, 5.0 server
     */
    @Nullable
    public URI getSmallAvatarUri() {
        return avatarUris.get(S16_16);
    }

    /**
     * As of JIRA 5.0 there can be several different user avatar URIs - for different size.
     *
     * @param sizeDefinition size like "16x16" or "48x48". URI for 48x48 should be always defined.
     * @return URI for specified size or <code>null</code> when there is no avatar image with given dimensions specified for this user
     */
    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public URI getAvatarUri(String sizeDefinition) {
        return avatarUris.get(sizeDefinition);
    }

    /**
     * @return groups given user belongs to
     */
    @Nullable
    public ExpandableProperty<String> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User that = (User) obj;
            return super.equals(obj) && Objects.equals(this.emailAddress, that.emailAddress)
                    && Objects.equals(this.avatarUris, that.avatarUris);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), emailAddress, avatarUris, groups, timezone);
    }

    /**
     * @return user timezone, like "Europe/Berlin" or <code>null</code> if timezone info is not available
     * @since me.glindholm.jira.rest.client.api 0.5, server 4.4
     */
    @Nullable
    public String getTimezone() {
        return timezone;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

}
