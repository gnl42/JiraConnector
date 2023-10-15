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

import java.time.OffsetDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Statistics about logins (successful and failed logins number and last date) for the current user
 *
 * @since v0.1
 */
public class LoginInfo {
    private final int failedLoginCount;
    private final int loginCount;
    @Nullable
    private final OffsetDateTime lastFailedLoginDate;
    @Nullable
    private final OffsetDateTime previousLoginDate;

    public LoginInfo(final int failedLoginCount, final int loginCount, @Nullable final OffsetDateTime lastFailedLoginDate,
            @Nullable final OffsetDateTime previousLoginDate) {
        this.failedLoginCount = failedLoginCount;
        this.loginCount = loginCount;
        this.lastFailedLoginDate = lastFailedLoginDate;
        this.previousLoginDate = previousLoginDate;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public int getLoginCount() {
        return loginCount;
    }

    @Nullable
    public OffsetDateTime getLastFailedLoginDate() {
        return lastFailedLoginDate;
    }

    @Nullable
    public OffsetDateTime getPreviousLoginDate() {
        return previousLoginDate;
    }

    @Override
    public String toString() {
        return "LoginInfo [failedLoginCount=" + failedLoginCount + ", loginCount=" + loginCount + ", lastFailedLoginDate=" + lastFailedLoginDate
                + ", previousLoginDate=" + previousLoginDate + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final LoginInfo that) {
            return Objects.equals(failedLoginCount, that.failedLoginCount) && Objects.equals(loginCount, that.loginCount)
                    && Objects.equals(lastFailedLoginDate, that.lastFailedLoginDate) && Objects.equals(previousLoginDate, that.previousLoginDate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failedLoginCount, loginCount, lastFailedLoginDate, previousLoginDate);
    }

}
