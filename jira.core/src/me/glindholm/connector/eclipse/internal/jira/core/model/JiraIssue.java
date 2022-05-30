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

import me.glindholm.jira.rest.client.api.domain.Issue;
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

    private String assignee;

    private String reporter;

    private String assigneeName;

    private String reporterName;

    private Instant created;

    private Instant updated;

    private JiraVersion[] reportedVersions = null;

    private JiraVersion[] fixVersions = null;

    private JiraComponent[] components = null;

    private Instant due;

    private boolean hasDueDate;

    private int votes;

    private JiraComment[] comments = new JiraComment[0];

    private Long initialEstimate;

    private Long estimate;

    private long actual;

    private boolean isWatched;

    private boolean hasVote;

    private String url;

    private JiraAttachment[] attachments = new JiraAttachment[0];

    private JiraCustomField[] customFields = new JiraCustomField[0];

    private JiraIssueField[] editableFields = new JiraIssueField[0];

    private JiraSubtask[] subtasks = new JiraSubtask[0];

    private JiraIssueLink[] issueLinks = new JiraIssueLink[0];

    private JiraWorkLog[] worklogs = new JiraWorkLog[0];

    private JiraSecurityLevel securityLevel;

    private boolean markupDetected;

    private URI self;

    private Long rank = null;

    private String[] labels = new String[0];

    private Issue rawIssue;

    private Watchers watchers;

    public String getId() {
        return id;
    }

    public URI getSelf() {
        return self;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public String getAssignee() {
        return this.assignee;
    }

    public void setAssignee(String asignee) {
        this.assignee = asignee;
    }

    public JiraComponent[] getComponents() {
        return this.components;
    }

    public void setComponents(JiraComponent[] components) {
        this.components = components;
    }

    public Instant getCreated() {
        return this.created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getDue() {
        return this.due;
    }

    public void setDue(Instant due) {
        this.due = due;
        this.hasDueDate = true;
    }

    public boolean hasDueDate() {
        return hasDueDate;
    }

    public JiraVersion[] getFixVersions() {
        return this.fixVersions;
    }

    public void setFixVersions(JiraVersion[] fixVersions) {
        this.fixVersions = fixVersions;
    }

    public JiraPriority getPriority() {
        return this.priority;
    }

    public void setPriority(JiraPriority priority) {
        this.priority = priority;
    }

    public String getReporter() {
        return this.reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public JiraResolution getResolution() {
        return this.resolution;
    }

    public void setResolution(JiraResolution resolution) {
        this.resolution = resolution;
    }

    public JiraStatus getStatus() {
        return this.status;
    }

    public void setStatus(JiraStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public JiraIssueType getType() {
        return this.type;
    }

    public void setType(JiraIssueType type) {
        this.type = type;
    }

    public Instant getUpdated() {
        return this.updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public JiraVersion[] getReportedVersions() {
        return this.reportedVersions;
    }

    public void setReportedVersions(JiraVersion[] reportedVersions) {
        this.reportedVersions = reportedVersions;
    }

    public int getVotes() {
        return this.votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public JiraComment[] getComments() {
        return this.comments;
    }

    public void setComments(JiraComment[] comments) {
        this.comments = comments;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public JiraProject getProject() {
        return this.project;
    }

    public void setProject(JiraProject project) {
        this.project = project;
    }

    public long getActual() {
        return this.actual;
    }

    public void setActual(long actual) {
        this.actual = actual;
    }

    public Long getInitialEstimate() {
        return this.initialEstimate;
    }

    public void setInitialEstimate(long initialEstimate) {
        this.initialEstimate = initialEstimate;
    }

    public Long getEstimate() {
        return this.estimate;
    }

    public void setEstimate(long estimate) {
        this.estimate = estimate;
    }

    public void setWatched(boolean isWatched) {
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
    public boolean canUserVote(String user) {
        return (this.getResolution() == null || this.getResolution().getId() == null || "".equals(this.getResolution() //$NON-NLS-1$
                .getId())) && !user.equals(this.getReporter());
    }

    public void setHasVote(boolean hasVote) {
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
        return this.hasVote;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return this.key + " " + this.summary; //$NON-NLS-1$
    }

    public JiraAttachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(JiraAttachment[] attachments) {
        this.attachments = attachments;
    }

    public JiraAttachment getAttachmentById(String id) {
        for (JiraAttachment attachment : this.attachments) {
            if (attachment.getId().equals(id)) {
                return attachment;
            }
        }
        return null;
    }

    public void setCustomFields(JiraCustomField[] customFields) {
        this.customFields = customFields;
    }

    public JiraCustomField[] getCustomFields() {
        return customFields;
    }

    public JiraCustomField getCustomFieldById(String fieldId) {
        for (JiraCustomField field : getCustomFields()) {
            if (fieldId.equals(field.getId())) {
                return field;
            }
        }
        return null;
    }

    public JiraSubtask[] getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(JiraSubtask[] subtasks) {
        this.subtasks = subtasks;
    }

    public JiraIssueLink[] getIssueLinks() {
        return issueLinks;
    }

    public void setIssueLinks(JiraIssueLink[] issueLinks) {
        this.issueLinks = issueLinks;
    }

    /**
     * @param field
     * @return list of field actual values without faked "none" value for combo and
     *         radio buttons
     */
    public String[] getFieldValues(String field) {
        if ("summary".equals(field)) { //$NON-NLS-1$
            return new String[] { getSummary() };
        } else if ("description".equals(field)) { //$NON-NLS-1$
            return new String[] { getDescription() };
        } else if ("resolution".equals(field)) { //$NON-NLS-1$
            if (resolution != null) {
                return new String[] { resolution.getId() };
            }
        } else if ("assignee".equals(field)) { //$NON-NLS-1$
            return new String[] { assignee };
        } else if ("reporter".equals(field)) { //$NON-NLS-1$
            return new String[] { reporter };
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
                String[] res = new String[components.length];
                for (int i = 0; i < components.length; i++) {
                    res[i] = components[i].getId();
                }
                return res;
            }
        } else if ("versions".equals(field)) { //$NON-NLS-1$
            if (reportedVersions != null) {
                String[] res = new String[reportedVersions.length];
                for (int i = 0; i < reportedVersions.length; i++) {
                    res[i] = reportedVersions[i].getId();
                }
                return res;
            }
        } else if ("fixVersions".equals(field)) { //$NON-NLS-1$
            if (fixVersions != null) {
                String[] res = new String[fixVersions.length];
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
            for (JiraCustomField customField : customFields) {
                if (customField.getId().equals(field)) {
                    List<String> values = customField.getValues();
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

    public void setSecurityLevel(JiraSecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public boolean isMarkupDetected() {
        return markupDetected;
    }

    public void setMarkupDetected(boolean markupDetected) {
        this.markupDetected = markupDetected;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getAssigneeDisplayName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public void setSelf(URI self) {
        this.self = self;
    }

    public JiraWorkLog[] getWorklogs() {
        return worklogs;
    }

    public void setWorklogs(JiraWorkLog[] worklogs) {
        this.worklogs = worklogs;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public Long getRank() {
        return this.rank;
    }

    public JiraIssueField[] getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(JiraIssueField[] editableFields) {
        this.editableFields = editableFields;
    }

    public void setLabels(String[] strings) {
        this.labels = strings;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setRawIssue(Issue rawIssue) {
        this.rawIssue = rawIssue;
    }

    public Issue getRawIssue() {
        return rawIssue;
    }

    public void setWatchers(Watchers watchers) {
        this.watchers = watchers;
    }

    public Watchers getWatchers() {
        return watchers;
    }
}
