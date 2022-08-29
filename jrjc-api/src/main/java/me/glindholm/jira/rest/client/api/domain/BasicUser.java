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
import java.util.Objects;

/**
 * Basic information about a JIRA user
 *
 * @since v0.1
 */
public class BasicUser extends AddressableNamedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * This value is used to mark incomplete user URI - when server response with
     * user without selfUri set. This may happen due to bug in JIRA REST API - for
     * example in JRA-30263 bug, JIRA REST API will return user without selfUri for
     * deleted author of worklog entry.
     */
    public static final URI INCOMPLETE_URI = URI.create("incomplete://user");

    public static final String UNASSIGNED = "-1";

    private final String displayName;
    private final String accountId;
    private final String emailAddress;
    private final boolean active;

    public BasicUser(final URI self, final String name, final String displayName, final String accountId, final String emailAddress, final boolean active) {
        super(self, name);
        this.displayName = displayName;
        this.accountId = accountId;
        this.emailAddress = emailAddress;
        this.active = active;
    }

    public BasicUser(final URI self, final String name, final String displayName) {
        this(self, name, displayName, null, null, true);
    }

    public BasicUser(final BasicUser user) {
        this(user.getSelf(), user.getName(), user.getDisplayName(), user.getAccountId(), user.getEmailAddress(), user.isActive());
    }

    public String getId() {
        if (super.getName() == null) {
            return accountId;
        } else if (super.getName() != null) {
            return super.getName();
        } else {
            return UNASSIGNED;
        }
    }

    public boolean isAssigned() {
        return getId() != UNASSIGNED;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof BasicUser)) {
            return false;
        }
        final BasicUser other = (BasicUser) obj;
        return Objects.equals(accountId, other.accountId) && active == other.active && Objects.equals(displayName, other.displayName)
                && Objects.equals(emailAddress, other.emailAddress);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(accountId, active, displayName, emailAddress);
        return result;
    }

    /**
     * @return true when URI returned from server was incomplete. See
     *         {@link BasicUser#INCOMPLETE_URI} for more detail.
     */
    public boolean isSelfUriIncomplete() {
        return INCOMPLETE_URI.equals(self);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BasicUser [self=").append(self).append(", name=").append(name).append(", displayName=").append(displayName).append(", accountId=")
                .append(accountId).append(", emailAddress=").append(emailAddress).append(", active=").append(active).append("]");
        return builder.toString();
    }

}
