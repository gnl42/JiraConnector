/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.Issue;
import me.glindholm.jira.rest.client.api.domain.Remotelink;
import me.glindholm.jira.rest.client.api.domain.Watchers;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Jacek Jaroczynski
 */
public class JiraIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String key;

    private String parentId;

    private String parentKey;

    private String summary;

    private String environment;

    private String description;

    private JiraProject project;

    private JiraIssueType type;

    private JiraPriority priority;

    private JiraStatus status;

    private JiraResolution resolution;

    private BasicUser assignee;

    private BasicUser reporter;

    private Instant created;

    private Instant updated;

    private JiraVersion[] reportedVersions = null;

    private JiraVersion[] fixVersions = null;

    private JiraComponent[] components = null;

    private Instant due;

    private boolean hasDueDate;

    private int votes;

    private JiraComment[] comments = {};

    private Long initialEstimate;

    private Long estimate;

    private long actual;

    private boolean isWatched;

    private boolean hasVote;

    private String url;

    private JiraAttachment[] attachments = {};

    private JiraCustomField[] customFields = {};

    private JiraIssueField[] editableFields = {};

    private JiraSubtask[] subtasks = {};

    private JiraIssueLink[] issueLinks = {};

    private JiraWorkLog[] worklogs = {};

    private JiraSecurityLevel securityLevel;

    private boolean markupDetected;

    private URI self;

    private Long rank = null;

    private String[] labels = {};

    private Issue rawIssue;

    private Watchers watchers;

    private Map<String, List<Remotelink>> remotelinks;

    public String getId() {
        return id;
    }

    public URI getSelf() {
        return self;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(final String parentKey) {
        this.parentKey = parentKey;
    }

    public BasicUser getAssignee() {
        return assignee;
    }

    public void setAssignee(final BasicUser asignee) {
        assignee = asignee;
    }

    public JiraComponent[] getComponents() {
        return components;
    }

    public void setComponents(final JiraComponent[] components) {
        this.components = components;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(final Instant created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Instant getDue() {
        return due;
    }

    public void setDue(final Instant due) {
        this.due = due;
        hasDueDate = true;
    }

    public boolean hasDueDate() {
        return hasDueDate;
    }

    public JiraVersion[] getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(final JiraVersion[] fixVersions) {
        this.fixVersions = fixVersions;
    }

    public JiraPriority getPriority() {
        return priority;
    }

    public void setPriority(final JiraPriority priority) {
        this.priority = priority;
    }

    public BasicUser getReporter() {
        return reporter;
    }

    public void setReporter(final BasicUser reporter) {
        this.reporter = reporter;
    }

    public JiraResolution getResolution() {
        return resolution;
    }

    public void setResolution(final JiraResolution resolution) {
        this.resolution = resolution;
    }

    public JiraStatus getStatus() {
        return status;
    }

    public void setStatus(final JiraStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public JiraIssueType getType() {
        return type;
    }

    public void setType(final JiraIssueType type) {
        this.type = type;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(final Instant updated) {
        this.updated = updated;
    }

    public JiraVersion[] getReportedVersions() {
        return reportedVersions;
    }

    public void setReportedVersions(final JiraVersion[] reportedVersions) {
        this.reportedVersions = reportedVersions;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(final int votes) {
        this.votes = votes;
    }

    public JiraComment[] getComments() {
        return comments;
    }

    public void setComments(final JiraComment[] comments) {
        this.comments = comments;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(final String environment) {
        this.environment = environment;
    }

    public JiraProject getProject() {
        return project;
    }

    public void setProject(final JiraProject project) {
        this.project = project;
    }

    public long getActual() {
        return actual;
    }

    public void setActual(final long actual) {
        this.actual = actual;
    }

    public Long getInitialEstimate() {
        return initialEstimate;
    }

    public void setInitialEstimate(final long initialEstimate) {
        this.initialEstimate = initialEstimate;
    }

    public Long getEstimate() {
        return estimate;
    }

    public void setEstimate(final long estimate) {
        this.estimate = estimate;
    }

    public void setWatched(final boolean isWatched) {
        this.isWatched = isWatched;
    }

    public boolean isWatched() {
        // XXX Requires new API to work
        return isWatched;
    }

    /**
     * Determines if it is ok for the supplied user to vote on this issue. Users can
     * not vote on an issue if the issue is resolved or the user is the reporter.
     *
     * @return <code>true</code> if it is valid for <code>user</code> to vote for
     *         the issue
     */
    public boolean canUserVote(final String user) {
        return (getResolution() == null || getResolution().getId() == null || "".equals(getResolution() //$NON-NLS-1$
                .getId())) && !user.equals(getReporter());
    }

    public void setHasVote(final boolean hasVote) {
        this.hasVote = hasVote;
    }

    /**
     * Determines if this issue has been voted on by the current user
     *
     * @return <code>true</code> if the current user is voting for this issue.
     *         <code>false</code> otherwise.
     */
    public boolean getHasVote() {
        // XXX Required new API to work
        return hasVote;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return key + " " + summary; //$NON-NLS-1$
    }

    public JiraAttachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(final JiraAttachment[] attachments) {
        this.attachments = attachments;
    }

    public JiraAttachment getAttachmentById(final String id) {
        for (final JiraAttachment attachment : attachments) {
            if (attachment.getId().equals(id)) {
                return attachment;
            }
        }
        return null;
    }

    public void setCustomFields(final JiraCustomField[] customFields) {
        this.customFields = customFields;
    }

    public JiraCustomField[] getCustomFields() {
        return customFields;
    }

    public JiraCustomField getCustomFieldById(final String fieldId) {
        for (final JiraCustomField field : getCustomFields()) {
            if (fieldId.equals(field.getId())) {
                return field;
            }
        }
        return null;
    }

    public JiraSubtask[] getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(final JiraSubtask[] subtasks) {
        this.subtasks = subtasks;
    }

    public JiraIssueLink[] getIssueLinks() {
        return issueLinks;
    }

    public void setIssueLinks(final JiraIssueLink[] issueLinks) {
        this.issueLinks = issueLinks;
    }

    /**
     * @param field
     * @return list of field actual values without faked "none" value for combo and
     *         radio buttons
     */
    public String[] getFieldValues(final String field) {
        if ("summary".equals(field)) { //$NON-NLS-1$
            return new String[] { getSummary() };
        } else if ("description".equals(field)) { //$NON-NLS-1$
            return new String[] { getDescription() };
        } else if ("resolution".equals(field)) { //$NON-NLS-1$
            if (resolution != null) {
                return new String[] { resolution.getId() };
            }
        } else if ("assignee".equals(field)) { //$NON-NLS-1$
            return new String[] { assignee.getId() };
        } else if ("reporter".equals(field)) { //$NON-NLS-1$
            return new String[] { reporter.getId() };
        } else if ("issuetype".equals(field)) { //$NON-NLS-1$
            if (type != null) {
                return new String[] { type.getId() };
            }
        } else if ("priority".equals(field)) { //$NON-NLS-1$
            if (priority != null) {
                return new String[] { getPriority().getId() };
            }
        } else if ("components".equals(field)) { //$NON-NLS-1$
            if (components != null) {
                final String[] res = new String[components.length];
                for (int i = 0; i < components.length; i++) {
                    res[i] = components[i].getId();
                }
                return res;
            }
        } else if ("versions".equals(field)) { //$NON-NLS-1$
            if (reportedVersions != null) {
                final String[] res = new String[reportedVersions.length];
                for (int i = 0; i < reportedVersions.length; i++) {
                    res[i] = reportedVersions[i].getId();
                }
                return res;
            }
        } else if ("fixVersions".equals(field)) { //$NON-NLS-1$
            if (fixVersions != null) {
                final String[] res = new String[fixVersions.length];
                for (int i = 0; i < fixVersions.length; i++) {
                    res[i] = fixVersions[i].getId();
                }
                return res;
            }
        } else if ("environment".equals(field)) { //$NON-NLS-1$
            if (environment != null) {
                return new String[] { environment };
            }
        } else if ("duedate".equals(field)) { //$NON-NLS-1$
            if (due != null) {
                return new String[] { new SimpleDateFormat("dd/MMM/yy").format(due) }; //$NON-NLS-1$
            }
        } else if ("timetracking".equals(field)) { //$NON-NLS-1$
            return new String[] { Long.toString(getEstimate() / 60) + "m" }; //$NON-NLS-1$
        } else if ("labels".equals(field)) { //$NON-NLS-1$
            return getLabels();
        } else if ("security".equals(field)) { //$NON-NLS-1$
            return new String[] { getSecurityLevel().getId() };
        }

        // TODO add other fields

        if (field.startsWith("customfield_")) { //$NON-NLS-1$
            for (final JiraCustomField customField : customFields) {
                if (customField.getId().equals(field)) {
                    final List<String> values = customField.getValues();
                    values.remove(JiraCustomField.NONE_ALLOWED_VALUE);
                    return values.toArray(new String[values.size()]);
                }
            }
        }

        return null;
    }

    // TODO refactor RSS parser to use this call
    // public void setValue(String field, String value) {
    // if ("resolution".equals(field)) { //$NON-NLS-1$
    // if (value != null) {
    // resolution = new Resolution(value, value);
    // }
    // } else if ("assignee".equals(field)) { //$NON-NLS-1$
    // assignee = value;
    //
    // // TODO add other fields
    // } else if (field.startsWith("customfield_")) { //$NON-NLS-1$
    // boolean found = false;
    //
    // for (int i = 0; i < customFields.length; i++) {
    // CustomField customField = customFields[i];
    // if (customField.getId().equals(field)) {
    // customFields[i] = new CustomField(customField.getId(), customField.getKey(),
    // customField.getKey(),
    // Collections.singletonList(value));
    // found = true;
    // break;
    // }
    //
    // }
    //
    // if (!found) {
    // List<CustomField> list = Arrays.asList(customFields);
    // list.add(new CustomField(field, "", "", Collections.singletonList(value)));
    // //$NON-NLS-1$ //$NON-NLS-2$
    // customFields = list.toArray(new CustomField[list.size()]);
    // }
    // }
    // }

    public JiraSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(final JiraSecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public boolean isMarkupDetected() {
        return markupDetected;
    }

    public void setMarkupDetected(final boolean markupDetected) {
        this.markupDetected = markupDetected;
    }

    public void setSelf(final URI self) {
        this.self = self;
    }

    public JiraWorkLog[] getWorklogs() {
        return worklogs;
    }

    public void setWorklogs(final JiraWorkLog[] worklogs) {
        this.worklogs = worklogs;
    }

    public void setRank(final Long rank) {
        this.rank = rank;
    }

    public Long getRank() {
        return rank;
    }

    public JiraIssueField[] getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(final JiraIssueField[] editableFields) {
        this.editableFields = editableFields;
    }

    public void setLabels(final String[] strings) {
        labels = strings;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setRawIssue(final Issue rawIssue) {
        this.rawIssue = rawIssue;
    }

    public Issue getRawIssue() {
        return rawIssue;
    }

    public void setWatchers(final Watchers watchers) {
        this.watchers = watchers;
    }

    public Watchers getWatchers() {
        return watchers;
    }

    public void setRemotelinks(final Map<String, List<Remotelink>> remotelinks) {
        this.remotelinks = remotelinks;
    }

    public Map<String, List<Remotelink>> getRemotelinks() {
        return remotelinks;
    }
}
