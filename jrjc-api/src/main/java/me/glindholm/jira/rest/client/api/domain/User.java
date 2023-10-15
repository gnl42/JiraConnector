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

import org.eclipse.jdt.annotation.Nullable;

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
        return "User [groups=" + groups + ", avatarUris=" + avatarUris + ", timezone=" + timezone + ", toString()=" + super.toString() + "]";
    }

    public static final String S16_16 = "16x16";
    public static final String S48_48 = "48x48";

    private final ExpandableProperty<String> groups;

    private final Map<String, URI> avatarUris;

    /**
     * @since me.glindholm.jira.rest.client.api 0.5, server: 4.4
     */
    @Nullable
    private final String timezone;

    public User(final BasicUser user, @Nullable final ExpandableProperty<String> groups, final Map<String, URI> avatarUris, @Nullable final String timezone) {
        super(user);
        Objects.requireNonNull(avatarUris.get(S48_48), "At least one avatar URL is expected - for 48x48");
        this.timezone = timezone;
        this.avatarUris = new HashMap<>(avatarUris);
        this.groups = groups;
    }

    /**
     * This constructor is used to create an active user per default.
     *
     * @deprecated since v5.1.0. Use
     *             {@link #User(URI,String,String,String,boolean,ExpandableProperty,Map,String)}
     *             instead.
     */
    @Deprecated
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
     * @return URI for specified size or <code>null</code> when there is no avatar image with given
     *         dimensions specified for this user
     */
    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public URI getAvatarUri(final String sizeDefinition) {
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
    public boolean equals(final Object obj) {
        if (obj instanceof final User that) {
            return super.equals(obj) && Objects.equals(avatarUris, that.avatarUris);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), avatarUris, groups, timezone);
    }

    /**
     * @return user timezone, like "Europe/Berlin" or <code>null</code> if timezone info is not
     *         available
     * @since me.glindholm.jira.rest.client.api 0.5, server 4.4
     */
    @Nullable
    public String getTimezone() {
        return timezone;
    }

}
