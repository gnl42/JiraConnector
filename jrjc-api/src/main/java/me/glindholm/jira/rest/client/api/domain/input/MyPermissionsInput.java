/*
 * Copyright (C) 2014 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.api.domain.input;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.MyPermissionsRestClient;

/**
 * Permissions context for {@link MyPermissionsRestClient}
 */
public class MyPermissionsInput {
    @Nullable
    private final String projectKey;
    @Nullable
    private final Integer projectId;
    @Nullable
    private final String issueKey;
    @Nullable
    private final Integer issueId;

    /**
     * Creates permissions context
     *
     * @param projectKey key of project to scope returned permissions for
     * @param projectId  id of project to scope returned permissions for
     * @param issueKey   key of the issue to scope returned permissions for
     * @param issueId    id of the issue to scope returned permissions for
     */
    public MyPermissionsInput(@Nullable final String projectKey, @Nullable final Integer projectId,
            @Nullable final String issueKey, @Nullable final Integer issueId) {
        this.projectKey = projectKey;
        this.projectId = projectId;
        this.issueKey = issueKey;
        this.issueId = issueId;
    }

    @Nullable
    public String getProjectKey() {
        return projectKey;
    }

    @Nullable
    public Integer getProjectId() {
        return projectId;
    }

    @Nullable
    public String getIssueKey() {
        return issueKey;
    }

    @Nullable
    public Integer getIssueId() {
        return issueId;
    }

    @Override
    public String toString() {
        return "MyPermissionsInput [projectKey=" + projectKey + ", projectId=" + projectId + ", issueKey=" + issueKey + ", issueId=" + issueId + "]";
    }

    /**
     * Creates permissions context with project defined by key
     */
    public static MyPermissionsInput withProject(final String projectKey) {
        return new MyPermissionsInput(projectKey, null, null, null);
    }

    /**
     * Creates permissions context with project defined by id
     */
    public static MyPermissionsInput withProject(final int projectId) {
        return new MyPermissionsInput(null, projectId, null, null);
    }

    /**
     * Creates permissions context with issue defined by key
     */
    public static MyPermissionsInput withIssue(final String issueKey) {
        return new MyPermissionsInput(null, null, issueKey, null);
    }

    /**
     * Creates permissions context with issue defined by id
     */
    public static MyPermissionsInput withIssue(final int issueId) {
        return new MyPermissionsInput(null, null, null, issueId);
    }

    /**
     * Creates permissions context for any project or issue
     */
    public static MyPermissionsInput withAny() {
        return null;
    }
}
