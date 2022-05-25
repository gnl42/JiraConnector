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

    @Override
    public String toString() {
        return "BasicUser [displayName=" + displayName + ", accountId=" + accountId + "]";
    }

    /**
     * This value is used to mark incomplete user URI - when server response with user without selfUri set.
     * This may happen due to bug in JIRA REST API - for example in JRA-30263 bug, JIRA REST API will return
     * user without selfUri for deleted author of worklog entry.
     */
    public static URI INCOMPLETE_URI = URI.create("incomplete://user");

    private final String displayName;
    private final String accountId;

    public BasicUser(URI self, String name, String displayName, String accountId) {
        super(self, name);
        this.displayName = displayName;
        this.accountId = accountId;
    }

    public BasicUser(URI self, String name, String displayName) {
        this(self, name, displayName, null);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountId() {
        return accountId;
    }

    @Override
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicUser) {
            BasicUser that = (BasicUser) obj;
            return super.equals(that) && Objects.equals(this.displayName, that.displayName) && Objects.equals(this.accountId, that.accountId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), displayName, accountId);
    }

    /**
     * @return true when URI returned from server was incomplete. See {@link BasicUser#INCOMPLETE_URI} for more detail.
     */
    public boolean isSelfUriIncomplete() {
        return INCOMPLETE_URI.equals(self);
    }

}
