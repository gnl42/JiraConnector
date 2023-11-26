/*
 * Copyright (C) 2012 Atlassian
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

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps field id that may be used to refer to field in fields maps.
 */
public enum IssueFieldId {
    AFFECTS_VERSIONS_FIELD("versions"), ASSIGNEE_FIELD("assignee"), ATTACHMENT_FIELD("attachment"), COMMENT_FIELD("comment"), COMPONENTS_FIELD("components"),
    CREATED_FIELD("created"), DESCRIPTION_FIELD("description"), DUE_DATE_FIELD("duedate"), FIX_VERSIONS_FIELD("fixVersions"), ISSUE_TYPE_FIELD("issuetype"),
    LABELS_FIELD("labels"), LINKS_FIELD("issuelinks"), LINKS_PRE_5_0_FIELD("links"), PARENT("parent"), PRIORITY_FIELD("priority"), PROJECT_FIELD("project"),
    REPORTER_FIELD("reporter"), RESOLUTION_FIELD("resolution"), STATUS_FIELD("status"), SUBTASKS_FIELD("subtasks"), SUMMARY_FIELD("summary"),
    TIMETRACKING_FIELD("timetracking"), TRANSITIONS_FIELD("transitions"), UPDATED_FIELD("updated"), VOTES_FIELD("votes"), WATCHED_FIELD("watches"),
    WATCHER_PRE_5_0_FIELD("watcher"), WORKLOG_FIELD("worklog"), WORKLOGS_FIELD("worklogs");

    public final String id;

    private String getId() {
        return id;
    }

    IssueFieldId(final String id) {
        this.id = id;
    }

    /**
     * Returns all fields ids.
     *
     * @return List of string id of each field.
     */
    public static List<String> ids() {
        return EnumSet.allOf(IssueFieldId.class).stream().map(IssueFieldId::getId).collect(Collectors.toUnmodifiableList());
    }
}
