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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.ExpandableResource;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;

/**
 * Single JIRA issue
 *
 * @since v0.1
 */
public class Issue extends BasicIssue implements ExpandableResource {

    public Issue(String summary, URI self, String key, BasicProject project, BasicIssueType issueType, BasicStatus status,
			String description, @Nullable BasicPriority priority, @Nullable BasicResolution resolution, Collection<Attachment> attachments,
			@Nullable BasicUser reporter, @Nullable BasicUser assignee, DateTime creationDate, DateTime updateDate,
			Collection<Version> affectedVersions, Collection<Version> fixVersions, Collection<BasicComponent> components,
			@Nullable TimeTracking timeTracking, Collection<Field> fields, Collection<Comment> comments,
			@Nullable URI transitionsUri,
			@Nullable Collection<IssueLink> issueLinks,
			BasicVotes votes, Collection<Worklog> worklogs, BasicWatchers watchers, Iterable<String> expandos) {
		super(self, key);
        this.summary = summary;
		this.project = project;
		this.status = status;
		this.description = description;
		this.resolution = resolution;
		this.expandos = expandos;
        this.comments = comments;
        this.attachments = attachments;
		this.fields = fields;
		this.issueType = issueType;
		this.reporter = reporter;
		this.assignee = assignee;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.transitionsUri = transitionsUri;
		this.issueLinks = issueLinks;
		this.votes = votes;
		this.worklogs = worklogs;
		this.watchers = watchers;
		this.fixVersions = fixVersions;
		this.affectedVersions = affectedVersions;
		this.components = components;
		this.priority = priority;
		this.timeTracking = timeTracking;
	}

	private final BasicStatus status;
	private BasicIssueType issueType;
	private BasicProject project;
	private final URI transitionsUri;
	private final Iterable<String> expandos;
	private final Collection<BasicComponent> components;
    private final String summary;
	@Nullable
	private final String description;
	@Nullable
	private BasicUser reporter;
	private BasicUser assignee;
	@Nullable
	private final BasicResolution resolution;
	private Collection<Field> fields;
	private DateTime creationDate;
	private DateTime updateDate;
	private final BasicPriority priority;
	private final BasicVotes votes;
	@Nullable
	private final Collection<Version> fixVersions;
	@Nullable
	private final Collection<Version> affectedVersions;

	private final Collection<Comment> comments;

	@Nullable
	private final Collection<IssueLink> issueLinks;

	private final Collection<Attachment> attachments;

	private final Collection<Worklog> worklogs;
	private final BasicWatchers watchers;

	@Nullable
	private final TimeTracking timeTracking;

	public BasicStatus getStatus() {
		return status;
	}

	/**
	 * @return reporter of this issue or <code>null</code> if this issue has no reporter
	 */
	@Nullable
	public BasicUser getReporter() {
		return reporter;
	}

	/**
	 * @return assignee of this issue or <code>null</code> if this issue is unassigned.
	 */
	@Nullable
	public BasicUser getAssignee() {
		return assignee;
	}



	public String getSummary() {
		return summary;
	}

	/**
	 * @return priority of this issue
	 */
	@Nullable
	public BasicPriority getPriority() {
		return priority;
	}

	/**
	 *
	 * @return issue links for this issue (possibly nothing) or <code>null</code> when issue links are deactivated for this JIRA instance
	 */
	@Nullable
	public Iterable<IssueLink> getIssueLinks() {
		return issueLinks;
	}

	/**
	 * @return fields inaccessible by concrete getter methods (e.g. all custom fields)
	 */
	public Iterable<Field> getFields() {
		return fields;
	}

	/**
	 *
	 * @param id identifier of the field (inaccessible by concrete getter method)
	 * @return field with given id, or <code>null</code> when no field with given id exists for this issue
	 */
	@Nullable
	public Field getField(String id) {
		for (Field field : fields) {
			if (field.getId().equals(id)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * This method returns the first field with specified name.
	 * Names of fields in JIRA do not need to be unique. Therefore this method does not guarantee that you will get what you really want.
	 * It's added just for convenience. For identify fields you should use id rather than name.
	 *
	 * @param name name of the field.
	 * @return the first field matching selected name or <code>null</code> when no field with given name exists for this issue
	 */
	@Nullable
	public Field getFieldByName(String name) {
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public Iterable<String> getExpandos() {
		return expandos;
	}

	/**
	 * @return issue type
	 */
	public BasicIssueType getIssueType() {
		return issueType;
	}

	/**
	 * @return attachments of this issue
	 */
	public Iterable<Attachment> getAttachments() {
        return attachments;
    }

	public URI getAttachmentsUri() {
		return UriBuilder.fromUri(getSelf()).path("attachments").build();
	}

	/**
	 * @return comments for this issue
	 */
    public Iterable<Comment> getComments() {
        return comments;
    }

	/**
	 * @return project this issue belongs to
	 */
	public BasicProject getProject() {
		return project;
	}

	/**
	 * @return <code>null</code when voting is disabled in JIRA
	 */
	@Nullable
	public BasicVotes getVotes() {
		return votes;
	}

	public Iterable<Worklog> getWorklogs() {
		return worklogs;
	}

	/**
	 * @return <code>null</code when watching is disabled in JIRA 
	 */
	@Nullable
	public BasicWatchers getWatchers() {
		return watchers;
	}

	@Nullable
	public Iterable<Version> getFixVersions() {
		return fixVersions;
	}

	@Nullable
    public URI getTransitionsUri() {
        return transitionsUri;
    }

	@Nullable
    public Iterable<Version> getAffectedVersions() {
		return affectedVersions;
	}

	public Iterable<BasicComponent> getComponents() {
		return components;
	}

	public URI getVotesUri() {
		return UriBuilder.fromUri(getSelf()).path("votes").build();
	}



	@Nullable
	public BasicResolution getResolution() {
		return resolution;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public DateTime getUpdateDate() {
		return updateDate;
	}

	@Nullable
	public TimeTracking getTimeTracking() {
		return timeTracking;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("project", project).
				add("status", status).
				add("description", description).
				add("expandos", expandos).
				add("resolution", resolution).
				add("reporter", reporter).
				add("assignee", assignee).addValue("\n").
				add("fields", fields).addValue("\n").
				add("affectedVersions", affectedVersions).addValue("\n").
				add("fixVersions", fixVersions).addValue("\n").
				add("components", components).addValue("\n").
				add("issueType", issueType).
				add("creationDate", creationDate).
				add("updateDate", updateDate).addValue("\n").
				add("attachments", attachments).addValue("\n").
				add("comments", comments).addValue("\n").
				add("transitionsUri", transitionsUri).
				add("issueLinks", issueLinks).addValue("\n").
				add("votes", votes).addValue("\n").
				add("worklogs", worklogs).addValue("\n").
				add("watchers", watchers).
				add("timeTracking", timeTracking).
				toString();
	}
}
