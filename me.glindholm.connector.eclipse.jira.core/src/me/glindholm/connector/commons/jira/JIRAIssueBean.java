/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.connector.commons.jira;

import me.glindholmjira.rest.client.IssueRestClient;
import me.glindholmjira.rest.client.domain.BasicComponent;
import me.glindholmjira.rest.client.domain.BasicIssueType;
import me.glindholmjira.rest.client.domain.BasicPriority;
import me.glindholmjira.rest.client.domain.BasicResolution;
import me.glindholmjira.rest.client.domain.BasicStatus;
import me.glindholmjira.rest.client.domain.BasicUser;
import me.glindholmjira.rest.client.domain.Comment;
import me.glindholmjira.rest.client.domain.Field;
import me.glindholmjira.rest.client.domain.Issue;
import me.glindholmjira.rest.client.domain.IssueLink;
import me.glindholmjira.rest.client.domain.IssueType;
import me.glindholmjira.rest.client.domain.Priority;
import me.glindholmjira.rest.client.domain.Status;
import me.glindholmjira.rest.client.domain.Subtask;
import me.glindholmjira.rest.client.domain.Version;
import me.glindholmjira.rest.client.internal.json.JsonParseUtil;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.glindholm.connector.commons.jira.beans.JIRAComment;
import me.glindholm.connector.commons.jira.beans.JIRACommentBean;
import me.glindholm.connector.commons.jira.beans.JIRAComponentBean;
import me.glindholm.connector.commons.jira.beans.JIRAConstant;
import me.glindholm.connector.commons.jira.beans.JIRAPriorityBean;
import me.glindholm.connector.commons.jira.beans.JIRASecurityLevelBean;
import me.glindholm.connector.commons.jira.beans.JIRAVersionBean;
import me.glindholm.connector.commons.jira.soap.axis.RemoteIssue;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jdom2.Element;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JIRAIssueBean implements JIRAIssue {
	private Long id;
    private String serverUrl;
	private String key;
	private String summary;
	private String status;
	private String statusUrl;
	private String type;
	private String typeUrl;
	private String priority;
	private String priorityUrl;
	private String description;
    private String wikiDescription;
	private String projectKey;
	private JIRAConstant statusConstant;
	private JIRAConstant typeConstant;
	private JIRAPriorityBean priorityConstant;
	private String assignee;
	private String assigneeId;
	private String reporter;
	private String reporterId;
	private String resolution;
	private String created;
	private String updated;
	private long statusId;
	private long priorityId;
	private long typeId;
	private List<JIRAConstant> affectsVersions;
	private List<JIRAConstant> fixVersions;
	private List<JIRAConstant> components;

	private List<String> subTaskList;
	private boolean thisIsASubTask;
    private Map<String, Map<String, List<String>>> issueLinks;
	private String parentIssueKey;
	private String originalEstimate;
	private String remainingEstimate;
	private String timeSpent;
	private List<JIRAComment> commentsList;
	private Object apiIssueObject;
	private String originalEstimateInSeconds;
	private String remainingEstimateInSeconds;
	private String timeSpentInSeconds;
	private JIRASecurityLevelBean securityLevel;
    private String environment;
    private List<JiraCustomField> basicCustomFields = new ArrayList<JiraCustomField>();
    private Locale locale;

    public JIRAIssueBean() {
        locale = Locale.US;
	}

    public JIRAIssueBean(JIRAIssue issue) {
        this.locale = issue.getLocale();
        this.id = issue.getId();
        this.serverUrl = issue.getServerUrl();
        this.key = issue.getKey();
        this.summary = issue.getSummary();
        this.status = issue.getStatus();
        this.statusUrl = issue.getStatusTypeUrl();
        this.type = issue.getType();
        this.typeUrl = issue.getTypeIconUrl();
        this.priority = issue.getPriority();
        this.priorityUrl = issue.getPriorityIconUrl();
        this.description = issue.getDescription();
        this.wikiDescription = issue.getWikiDescription();
        this.projectKey = issue.getProjectKey();
        this.statusConstant = issue.getStatusConstant();
        this.typeConstant = issue.getTypeConstant();
        this.priorityConstant = issue.getPriorityConstant();
        this.assignee = issue.getAssignee();
        this.assigneeId = issue.getAssigneeId();
        this.reporter = issue.getReporter();
        this.reporterId = issue.getReporterId();
        this.resolution = issue.getResolution();
        this.created = issue.getCreated();
        this.updated = issue.getUpdated();
        this.statusId = issue.getStatusId();
        this.priorityId = issue.getPriorityId();
        this.typeId = issue.getTypeId();
        this.affectsVersions = issue.getAffectsVersions();
        this.fixVersions = issue.getFixVersions();
        this.components = issue.getComponents();
        this.subTaskList = issue.getSubTaskKeys();
        this.thisIsASubTask = issue.isSubTask();
        this.issueLinks = issue.getIssueLinks();
        this.parentIssueKey = issue.getParentIssueKey();
        this.originalEstimate = issue.getOriginalEstimate();
        this.remainingEstimate = issue.getRemainingEstimate();
        this.timeSpent = issue.getTimeSpent();
        this.commentsList = issue.getComments();
        this.apiIssueObject = issue.getApiIssueObject();
        this.originalEstimateInSeconds = issue.getOriginalEstimateInSeconds();
        this.remainingEstimateInSeconds = issue.getRemainingEstimateInSeconds();
        this.timeSpentInSeconds = issue.getTimeSpentInSeconds();
        this.securityLevel = issue.getSecurityLevel();
        this.environment = issue.getEnvironment();
        this.basicCustomFields = issue.getCustomFields();                
    }

	public JIRAIssueBean(String serverUrl, Element e, Locale locale) {
        this.locale = locale;
		this.summary = getTextSafely(e, "summary");
		this.key = getTextSafely(e, "key");
		this.id = new Long(getAttributeSafely(e, "key", "id"));
		updateProjectKey();
		this.status = getTextSafely(e, "status");
		this.statusUrl = getAttributeSafely(e, "status", "iconUrl");
		try {
			this.statusId = Long.parseLong(getAttributeSafely(e, "status", "id"));
		} catch (NumberFormatException ex) {
			this.statusId = 0;
		}
		this.priority = getTextSafely(e, "priority", "Unknown");
		this.priorityUrl = getAttributeSafely(e, "priority", "iconUrl");
		try {
			this.priorityId = Long.parseLong(getAttributeSafely(e, "priority", "id"));
		} catch (NumberFormatException ex) {
			this.priorityId = 0;
		}
		this.description = getTextSafely(e, "description");
        this.environment = getTextSafely(e, "environment");
        
		this.type = getTextSafely(e, "type");
		this.typeUrl = getAttributeSafely(e, "type", "iconUrl");
		try {
			this.typeId = Long.parseLong(getAttributeSafely(e, "type", "id"));
		} catch (NumberFormatException ex) {
			this.typeId = 0;
		}
		this.assignee = getTextSafely(e, "assignee");
		this.assigneeId = getAttributeSafely(e, "assignee", "username");
		this.reporter = getTextSafely(e, "reporter");
		this.reporterId = getAttributeSafely(e, "reporter", "username");
		this.created = getTextSafely(e, "created");
		this.updated = getTextSafely(e, "updated");
		this.resolution = getTextSafely(e, "resolution");

		this.parentIssueKey = getTextSafely(e, "parent");
		this.thisIsASubTask = parentIssueKey != null;
		subTaskList = new ArrayList<String>();
		Element subtasks = e.getChild("subtasks");
		if (subtasks != null) {
			for (Object subtask : subtasks.getChildren("subtask")) {
				String subTaskKey = ((Element) subtask).getText();
				if (subTaskKey != null) {
					subTaskList.add(subTaskKey);
				}
			}
		}

        Element issueLinksElement = e.getChild("issuelinks");
        if (issueLinksElement != null) {
            issueLinks = new HashMap<String, Map<String, List<String>>>();
            for (Object issueLinkTypeObj : issueLinksElement.getChildren("issuelinktype")) {
                Element issueLinkType = (Element) issueLinkTypeObj;
                String linkName = getTextSafely(issueLinkType, "name");
                Map<String, List<String>> map = new HashMap<String, List<String>>();
                for (String direction : new String[]{"outwardlinks", "inwardlinks"}) {
                    Element outwardLinks = issueLinkType.getChild(direction);
                    if (outwardLinks != null) {
                        String linkDescription = outwardLinks.getAttributeValue("description");
                        List<String> issueLinkList = new ArrayList<String>();
                        map.put(linkDescription, issueLinkList);
                        for (Object issueLinkObj : outwardLinks.getChildren("issuelink")) {
                            Element issueLink = (Element) issueLinkObj;
                            String issueKey = getTextSafely(issueLink, "issuekey");
                            if (issueKey != null) {
//                                System.out.println(linkName + ":" + description + ":" + issueKey);
                                issueLinkList.add(issueKey);
                            }
                        }
                        issueLinks.put(linkName, map);
                    }
                }
            }
        }
		this.originalEstimate = getTextSafely(e, "timeoriginalestimate");
		this.remainingEstimate = getTextSafely(e, "timeestimate");
		this.timeSpent = getTextSafely(e, "timespent");
		this.originalEstimateInSeconds = getAttributeSafely(e, "timeoriginalestimate", "seconds");
		this.remainingEstimateInSeconds = getAttributeSafely(e, "timeestimate", "seconds");
		this.timeSpentInSeconds = getAttributeSafely(e, "timespent", "seconds");
        this.serverUrl = serverUrl;

		Element comments = e.getChild("comments");
		if (comments != null) {
			commentsList = new ArrayList<JIRAComment>();
			for (Object comment : comments.getChildren("comment")) {
				Element el = (Element) comment;
				String commentId = el.getAttributeValue("id", "-1");
				String author = el.getAttributeValue("author", "Unknown");
				String text = el.getText();
				String creationDate = el.getAttributeValue("created", "Unknown");

				Calendar cal = Calendar.getInstance();
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", locale);
				try {
					cal.setTime(df.parse(creationDate));
				} catch (java.text.ParseException ex) {
                    //try another one
					df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
                    try {
                        cal.setTime(df.parse(creationDate));
                    } catch (ParseException e1) {
                        //not good date format is not handled
                    }
                }

				commentsList.add(new JIRACommentBean(commentId, author, text, cal));
			}
		}

        Element customfields = e.getChild("customfields");
        if (customfields != null && customfields.getChildren().size() > 0) {
              for (Object fieldElement : customfields.getChildren()) {
                  basicCustomFields.add(new JiraCustomFieldImpl.Builder((Element) fieldElement).build());
              }            
        }
	}

    public JIRAIssueBean(String url, RemoteIssue remoteIssue) {
        this.serverUrl = url;
        id = Long.valueOf(remoteIssue.getId());
		key = remoteIssue.getKey();
		summary = remoteIssue.getSummary();
		status = remoteIssue.getStatus();
        environment = remoteIssue.getEnvironment();
		//statusUrl
		type = remoteIssue.getType();
		//typeUrl = remoteIssue.getTypeIconUrl();
		priority = remoteIssue.getPriority();
		//priorityUrl = remoteIssue.getPriorityIconUrl();
		wikiDescription = remoteIssue.getDescription();
		projectKey = remoteIssue.getProject();
		//statusConstant
		//typeConstant
		//priorityConstant = remoteIssue.getPriorityConstant();
		assignee = remoteIssue.getAssignee();
		//assigneeId = remoteIssue.getAssigneeId();
		reporter = remoteIssue.getReporter();
		//reporterId = issue.getReporterId();
		resolution = remoteIssue.getResolution();
		//created = remoteIssue.getCreated();
		//updated = remoteIssue.getUpdated();
		//statusId = remoteIssue.getStatusId();
		//priorityId = issue.getPriorityId();
		//typeId = issue.getTypeId();
		//thisIsASubTask = remoteIssue.isSubTask();
		//subTaskList = remoteIssue.getSubTaskKeys();
		//parentIssueKey = remoteIssue.getParentIssueKey();
		//originalEstimate = remoteIssue.getOriginalEstimate();
		//originalEstimateInSeconds = remoteIssue.getOriginalEstimateInSeconds();
		//remainingEstimate = remoteIssue.getRemainingEstimate();
		//remainingEstimateInSeconds = remoteIssue.getRemainingEstimateInSeconds();
		//timeSpent = remoteIssue.getTimeSpent();
		//timeSpentInSeconds = issue.getTimeSpentInSeconds();

    }

    public JIRAIssueBean(String url, Issue issue) {
        locale = Locale.US;

        this.apiIssueObject = issue;
        this.serverUrl = url;
        this.id = issue.getId();
        this.key = issue.getKey();
        this.projectKey = issue.getProject().getKey();
        this.thisIsASubTask = issue.getIssueType().isSubtask();
        if (thisIsASubTask) {
            if (issue.getField("parent") != null) {
                Object parent = issue.getField("parent").getValue();
                if (parent instanceof JSONObject) {
                    this.parentIssueKey = JsonParseUtil.getOptionalString((JSONObject) parent, "key");
                }
            }
        }
        this.summary = issue.getSummary();
        this.description = getHtmlDescription(issue);
        this.wikiDescription = issue.getDescription();
        BasicIssueType issueType = issue.getIssueType();
        this.type = issueType.getName();
        Long issueTypeId = issueType.getId();
        if (issueType instanceof IssueType) {
            this.typeUrl = ((IssueType) issueType).getIconUri().toString();
        }
        this.typeId = issueTypeId != null ? issueTypeId : -1;
        BasicStatus s = issue.getStatus();
        this.statusId = s.getId();
        this.status = s.getName();
        if (s instanceof Status) {
            this.statusUrl = ((Status) s).getIconUrl().toString();
        }

        BasicResolution res = issue.getResolution();
        this.resolution = res != null ? res.getName() : "Unresolved";

        BasicUser ass = issue.getAssignee();
        if (ass != null) {
            this.assigneeId = ass.getName();
            this.assignee = ass.getDisplayName();
        }
        BasicUser rep = issue.getReporter();
        if (rep != null) {
            this.reporterId = rep.getName();
            this.reporter = rep.getDisplayName();
        }

        BasicPriority prio = issue.getPriority();
        if (prio != null) {
            Long prioId = prio.getId();
            this.priorityId = prioId != null ? prioId : -1;
            this.priority = prio.getName();
            if (prio instanceof Priority) {
                this.priorityUrl = ((Priority) prio).getIconUri().toString();
            }
        }
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        this.created = df.format(issue.getCreationDate().toDate());
        this.updated = df.format(issue.getUpdateDate().toDate());
        this.subTaskList = Lists.newArrayList();
        Iterable<Subtask> subtasks = issue.getSubtasks();
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                this.subTaskList.add(subtask.getIssueKey());
            }
        }
        this.components = Lists.newArrayList();
        Iterable<BasicComponent> components = issue.getComponents();
        if (components != null) {
            for (BasicComponent component : components) {
                this.components.add(new JIRAComponentBean(component.getId(), component.getName()));
            }
        }
        this.affectsVersions = Lists.newArrayList();
        Iterable<Version> aVersions = issue.getAffectedVersions();
        if (aVersions != null) {
            for (Version v : aVersions) {
                this.affectsVersions.add(new JIRAVersionBean(v.getId(), v.getName(), v.isReleased()));
            }
        }
        this.fixVersions = Lists.newArrayList();
        Iterable<Version> fVersions = issue.getFixVersions();
        if (fVersions != null) {
            for (Version v : fVersions) {
                this.fixVersions.add(new JIRAVersionBean(v.getId(), v.getName(), v.isReleased()));
            }
        }
        this.issueLinks = Maps.newHashMap();
        Iterable<IssueLink> links = issue.getIssueLinks();
        if (links != null) {
            for (IssueLink link : links) {
                String linkName = link.getIssueLinkType().getName();
                Map<String, List<String>> map = issueLinks.get(linkName);
                if (map == null) {
                    map = Maps.newHashMap();
                }
                String description = link.getIssueLinkType().getDescription();
                List<String> issueKeys = map.get(description);
                if (issueKeys == null) {
                    issueKeys = Lists.newArrayList();
                }
                issueKeys.add(link.getTargetIssueKey());
                map.put(description, issueKeys);

                issueLinks.put(linkName, map);
            }
        }

        Iterable<Comment> comments = issue.getComments();
        if (comments != null) {
            this.commentsList = Lists.newArrayList();
            for (Comment comment : comments) {
                Long cmtId = comment.getId();
                BasicUser commentAuthor = comment.getAuthor();
                Calendar created = comment.getCreationDate().toGregorianCalendar();
                created.setTimeZone(comment.getCreationDate().getZone().toTimeZone());
                this.commentsList.add(new JIRACommentBean(
                        cmtId != null ? cmtId.toString() : "",
                        commentAuthor != null ? commentAuthor.getName() : null,
                        getHtmlBodyForComment(issue, comment), created));
            }
        }

        JSONObject rfs = getRenderedFields(issue.getRawObject());
        if (rfs != null) {
            JSONObject timetracking = JsonParseUtil.getOptionalJsonObject(rfs, "timetracking");
            if (timetracking != null) {
                this.originalEstimate = JsonParseUtil.getOptionalString(timetracking, "originalEstimate");
                this.remainingEstimate = JsonParseUtil.getOptionalString(timetracking, "remainingEstimate");
                this.timeSpent = JsonParseUtil.getOptionalString(timetracking, "timeSpent");
                try {
                    this.originalEstimateInSeconds = Optional.fromNullable(JsonParseUtil.getOptionalLong(timetracking, "originalEstimateSeconds")).or(0L).toString();
                    this.remainingEstimateInSeconds = Optional.fromNullable(JsonParseUtil.getOptionalLong(timetracking, "remainingEstimateSeconds")).or(0L).toString();
                    this.timeSpentInSeconds = Optional.fromNullable(JsonParseUtil.getOptionalLong(timetracking, "timeSpentSeconds")).or(0L).toString();
                } catch (JSONException e) {
                    // buu
                }
            }
        }
        JSONObject editmeta = JsonParseUtil.getOptionalJsonObject(issue.getRawObject(), "editmeta");
        JSONObject fields = editmeta != null ? JsonParseUtil.getOptionalJsonObject(editmeta, "fields") : null;
        basicCustomFields = Lists.newArrayList();
        if (fields != null) {
            for (Field field : issue.getFields()) {
                if (!field.getId().startsWith("customfield_")) {
                    continue;
                }
                try {
                    basicCustomFields.add(new JiraCustomFieldImpl.Builder((JSONObject) fields.get(field.getId()), field).build());
                } catch (JSONException e) {
                    // just skip. Treat as missing meta
                }
            }
        }
    }

    private String getHtmlDescription(Issue issue) {
        JSONObject rf = getRenderedFields(issue.getRawObject());
        if (rf == null) {
            return issue.getDescription();
        }
        String result = JsonParseUtil.getOptionalString(rf, "description");
        return result != null ? result : issue.getDescription();
    }

    private String getHtmlBodyForComment(Issue issue, Comment comment) {
        JSONObject rf = getRenderedFields(issue.getRawObject());

        if (rf == null) {
            return comment.getBody();
        }

        try {
            JSONArray array = rf.getJSONObject("comment").getJSONArray("comments");
            for (int i = 0; i < array.length(); ++i) {
                JSONObject element = (JSONObject) array.get(i);
                if (Objects.equal(element.getLong("id"), comment.getId())) {
                    return element.getString("body");
                }
            }
        } catch (Exception e) {
            // well?
        }
        return comment.getBody();
    }

    private static JSONObject getRenderedFields(JSONObject issue) {
        if (issue == null) {
            return null;
        }
        return JsonParseUtil.getOptionalJsonObject(issue, IssueRestClient.Expandos.RENDERED_FIELDS.getFieldName());
    }

    public JIRAPriorityBean getPriorityConstant() {
		return priorityConstant;
	}

	public void setPriority(JIRAPriorityBean priority) {
		this.priority = priority.getName();
		this.priorityConstant = priority;
	}

	public JIRAIssueBean(Map params) {
		this.summary = (String) params.get("summary");
		this.status = (String) params.get("status");
		this.key = (String) params.get("key");
		this.id = new Long(params.get("key").toString());
		updateProjectKey();
		this.description = (String) params.get("description");
		this.type = (String) params.get("type");
		this.priority = (String) params.get("priority");
	}

	private void updateProjectKey() {
		if (key != null) {
			if (key.contains("-")) {
				projectKey = key.substring(0, key.indexOf("-"));
			} else {
				projectKey = key;
			}
		}
	}

    private String getTextSafely(Element e, String name, String defaultName) {
        String text = getTextSafely(e, name);

        return text != null ? text : defaultName;
    }

	private String getTextSafely(Element e, String name) {
		Element child = e.getChild(name);

		if (child == null) {
			return null;
		}

		return child.getText();
	}

	private String getAttributeSafely(Element e, String elementName, String attributeName) {
		Element child = e.getChild(elementName);

		if (child == null || child.getAttribute(attributeName) == null) {
			return null;
		}

		return child.getAttributeValue(attributeName);
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getProjectUrl() {
		return getServerUrl() + "/browse/" + getProjectKey();
	}

	public String getIssueUrl() {
		return getServerUrl() + "/browse/" + getKey();
	}


	public Long getId() {
		return id;
	}

	public boolean isSubTask() {
		return thisIsASubTask;
	}

    public Map<String, Map<String, List<String>>> getIssueLinks() {
        return issueLinks;
    }

	public String getParentIssueKey() {
		return parentIssueKey;
	}

	public List<String> getSubTaskKeys() {
		return subTaskList;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public String getStatus() {
		return status;
	}

	public String getStatusTypeUrl() {
		return statusUrl;
	}

	public String getPriority() {
		return priority;
	}

	public String getPriorityIconUrl() {
		return priorityUrl;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSummary() {
		return summary;
	}

    public String getEnvironment() {
        return environment;
    }

    public String getType() {
		return type;
	}

	public String getTypeIconUrl() {
		return typeUrl;
	}

	public void setTypeIconUrl(String newTypeUrl) {
		this.typeUrl = newTypeUrl;
	}

	public String getDescription() {
		return description;
	}

    public String getWikiDescription() {
        return wikiDescription;
    }

    public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JIRAConstant getTypeConstant() {
		return typeConstant;
	}

	public void setType(JIRAConstant type) {
		this.type = type.getName();
		this.typeConstant = type;
	}

	public JIRAConstant getStatusConstant() {
		return statusConstant;
	}

	public void setStatus(JIRAConstant status) {
		this.status = status.getName();
		this.statusConstant = status;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public long getPriorityId() {
		return priorityId;
	}

	public long getStatusId() {
		return statusId;
	}

	public long getTypeId() {
		return typeId;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JIRAIssueBean that = (JIRAIssueBean) o;

        return !(key != null ? !key.equals(that.key) : that.key != null);
    }

	private static final int ONE_EFF = 31;

	@Override
	public int hashCode() {
		int result = 0;

		result = ONE_EFF * result + (key != null ? key.hashCode() : 0);
		return result;
	}

	public String getAssigneeId() {
		return assigneeId;
	}

	public void setAssigneeId(String assigneeId) {
		this.assigneeId = assigneeId;
	}

	public String getReporterId() {
		return reporterId;
	}

	public void setReporterId(String reporterId) {
		this.reporterId = reporterId;
	}

	public List<JIRAConstant> getAffectsVersions() {
		return affectsVersions;
	}

	public void setAffectsVersions(List<JIRAConstant> affectsVersions) {
		this.affectsVersions = affectsVersions;
	}

	public List<JIRAConstant> getFixVersions() {
		return fixVersions;
	}

	public void setFixVersions(List<JIRAConstant> fixVersions) {
		this.fixVersions = fixVersions;
	}

	public List<JIRAConstant> getComponents() {
		return components;
	}

	public void setComponents(List<JIRAConstant> components) {
		this.components = components;
	}

	public String getOriginalEstimate() {
		return originalEstimate;
	}

	public void setOriginalEstimate(String originalEstimate) {
		this.originalEstimate = originalEstimate;
	}

	public String getOriginalEstimateInSeconds() {
		return originalEstimateInSeconds;
	}

	public String getRemainingEstimate() {
		return remainingEstimate;
	}

	public String getRemainingEstimateInSeconds() {
		return remainingEstimateInSeconds;
	}

	public void setRemainingEstimate(String remainingEstimate) {
		this.remainingEstimate = remainingEstimate;
	}

	public String getTimeSpent() {
		return timeSpent;
	}

	public String getTimeSpentInSeconds() {
		return timeSpentInSeconds;
	}

	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}

	public List<JIRAComment> getComments() {
		return commentsList;
	}

    public Object getApiIssueObject() {
		return apiIssueObject;
	}

	public void setApiIssueObject(Object o) {
		apiIssueObject = o;
	}

	public JIRASecurityLevelBean getSecurityLevel() {
		return securityLevel;
	}

    public List<JiraCustomField> getCustomFields() {
        return basicCustomFields;
    }

    public void setSecurityLevel(final JIRASecurityLevelBean securityLevelBean) {
		this.securityLevel = securityLevelBean;
	}

    public Locale getLocale() {
        return locale;
    }

    public boolean usesRest() {
        return apiIssueObject instanceof Issue;
    }

    public void setWikiDescription(String wikiDescription) {
        this.wikiDescription = wikiDescription;
    }
}
