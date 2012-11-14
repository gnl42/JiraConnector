/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.rest;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.joda.time.DateTime;

import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueLink;
import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.SecurityLevel;
import com.atlassian.connector.eclipse.internal.jira.core.model.Subtask;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Visibility;

public class JiraRestConverter {

	private static final String FIELD_ENVIRONMENT_ID = "environment"; //$NON-NLS-1$

	public static Project[] convertProjects(Iterable<BasicProject> allProjects) {
		List<Project> projects = new ArrayList<Project>();
		for (BasicProject basicProject : allProjects) {
			projects.add(convert(basicProject));
		}
		return projects.toArray(new Project[projects.size()]);
	}

	private static Project convert(BasicProject basicProject) {
		Project project = new Project();

		project.setName(basicProject.getName());
		project.setKey(basicProject.getKey());
		// TODO provide real project id
		project.setId(Integer.toString(basicProject.getSelf().toString().hashCode()));

		return project;
	}

	public static Resolution[] convertResolutions(
			Iterable<com.atlassian.jira.rest.client.domain.Resolution> allResolutions) {
		List<Resolution> resolutions = new ArrayList<Resolution>();

		for (com.atlassian.jira.rest.client.domain.Resolution resolution : allResolutions) {
			resolutions.add(convert(resolution));
		}

		return resolutions.toArray(new Resolution[resolutions.size()]);
	}

	private static Resolution convert(com.atlassian.jira.rest.client.domain.Resolution resolution) {
		Resolution outResolution = new Resolution();

		outResolution.setName(resolution.getName());
		outResolution.setDescription(resolution.getDescription());
		// TODO change to real id if available
		outResolution.setId(Integer.toString((resolution.getSelf().toString().hashCode())));

		return outResolution;
	}

	public static Priority[] convertPriorities(Iterable<com.atlassian.jira.rest.client.domain.Priority> allPriorities) {
		List<Priority> priorities = new ArrayList<Priority>();

		for (com.atlassian.jira.rest.client.domain.Priority priority : allPriorities) {
			priorities.add(convert(priority));
		}

		return priorities.toArray(new Priority[priorities.size()]);
	}

	private static Priority convert(com.atlassian.jira.rest.client.domain.Priority priority) {
		Priority outPriority = new Priority();

		outPriority.setName(priority.getName());
		outPriority.setDescription(priority.getDescription());
		outPriority.setColour(priority.getStatusColor());
		outPriority.setIcon(priority.getIconUri().toString());
		// TODO set real id if avaialble
		outPriority.setId(Integer.toString((priority.getSelf().toString().hashCode())));
		outPriority.setSelf(priority.getSelf());

		return outPriority;
	}

	public static JiraIssue convertIssue(Issue issue, JiraClientCache cache, String url, IProgressMonitor monitor)
			throws JiraException {
		JiraIssue jiraIssue = new JiraIssue();

		// TODO rest: set real id if avaialble
		jiraIssue.setId(generateIssueId(issue.getSelf().toString(), issue.getKey()));
		jiraIssue.setSelf(issue.getSelf());
		jiraIssue.setKey(issue.getKey());
		jiraIssue.setSummary(issue.getSummary());
		// TODO rest: description html vs text vs wiki markap
		jiraIssue.setDescription(issue.getDescription());
//		jiraIssue.setParentId();
//		jiraIssue.setParentKey();
		// TODO rest: do we need to use cache here? can't we create priority and other objects from issue?
		jiraIssue.setPriority(cache.getPriorityByName(issue.getPriority().getName()));
		jiraIssue.setStatus(cache.getStatusByName(issue.getStatus().getName()));
		jiraIssue.setAssignee(issue.getAssignee().getName());
		jiraIssue.setAssigneeName(issue.getAssignee().getName());
		jiraIssue.setReporter(issue.getReporter().getName());
		jiraIssue.setReporterName(issue.getReporter().getName());
		jiraIssue.setResolution(issue.getResolution() == null ? null : cache.getResolutionByName(issue.getResolution()
				.getName()));
		if (issue.getTimeTracking().getOriginalEstimateMinutes() != null) {
			jiraIssue.setInitialEstimate(issue.getTimeTracking().getOriginalEstimateMinutes() * 60);
		}
		if (issue.getTimeTracking().getRemainingEstimateMinutes() != null) {
			jiraIssue.setEstimate(issue.getTimeTracking().getRemainingEstimateMinutes() * 60);
		}
		if (issue.getTimeTracking().getTimeSpentMinutes() != null) {
			jiraIssue.setActual(issue.getTimeTracking().getTimeSpentMinutes() * 60);
		}

		Object security = issue.getField("security").getValue();
		if (security != null && security instanceof JSONObject) {
			JSONObject json = (JSONObject) security;

			try {
				String id = json.getString("id");
				String name = json.getString("name");

				SecurityLevel securityLevel = new SecurityLevel(id);
				securityLevel.setName(name);

				jiraIssue.setSecurityLevel(securityLevel);
			} catch (JSONException e) {
				// TODO rest handle exception (log)
				e.printStackTrace();
			}
		}

		Project project = cache.getProjectByKey(issue.getProject().getKey());
		jiraIssue.setProject(project);
		if (project != null && !project.hasDetails()) {
			cache.refreshProjectDetails(project, monitor);
		}

		jiraIssue.setCreated(issue.getCreationDate().toDate());
		jiraIssue.setUpdated(issue.getUpdateDate().toDate());

		if (project != null && project.getIssueTypeById(issue.getIssueType().getId().toString()) != null) {
			jiraIssue.setType(project.getIssueTypeById(issue.getIssueType().getId().toString()));
		} else {
			jiraIssue.setType(cache.getIssueTypeById(issue.getIssueType().getId().toString()));
		}

		jiraIssue.setSubtasks(convert(issue.getSubtasks()));
		jiraIssue.setType(convert(issue.getIssueType()));
		jiraIssue.setUrl(url + "/browse/" + issue.getKey()); //$NON-NLS-1$
		jiraIssue.setComponents(convertComponents(issue.getComponents()));

		// TODO rest use getter once available
		Object env = issue.getField(FIELD_ENVIRONMENT_ID).getValue();
		if (env != null) {
			jiraIssue.setEnvironment(env.toString());
		}

		jiraIssue.setReportedVersions(convertVersions(issue.getAffectedVersions()));
		jiraIssue.setFixVersions(convertVersions(issue.getFixVersions()));

		DateTime dueDate = issue.getDueDate();
		if (dueDate != null) {
			jiraIssue.setDue(dueDate.toDate());
		}

		jiraIssue.setIssueLinks(convertIssueLinks(issue.getIssueLinks()));
		jiraIssue.setComments(convertComments(issue.getComments()));

		jiraIssue.setAttachments(convertAttachments(issue.getAttachments()));

		return jiraIssue;
	}

	private static Attachment[] convertAttachments(
			Iterable<com.atlassian.jira.rest.client.domain.Attachment> attachments) {

		List<Attachment> outAttachments = new ArrayList<Attachment>();

		for (com.atlassian.jira.rest.client.domain.Attachment attachment : attachments) {
			outAttachments.add(convert(attachment));
		}

		return outAttachments.toArray(new Attachment[outAttachments.size()]);
	}

	private static Attachment convert(com.atlassian.jira.rest.client.domain.Attachment attachment) {
		Attachment outAttachment = new Attachment();

		// TODO rest change to real id 
		outAttachment.setId(attachment.getSelf().toString());
		outAttachment.setAuthor(attachment.getAuthor().getDisplayName());
		outAttachment.setCreated(attachment.getCreationDate().toDate());
		outAttachment.setName(attachment.getFilename());
		outAttachment.setSize(attachment.getSize());
		outAttachment.setContent(attachment.getContentUri());

		return outAttachment;
	}

	private static Comment[] convertComments(Iterable<com.atlassian.jira.rest.client.domain.Comment> comments) {
		List<Comment> outComments = new ArrayList<Comment>();

		for (com.atlassian.jira.rest.client.domain.Comment comment : comments) {
			outComments.add(convert(comment));
		}

		return outComments.toArray(new Comment[outComments.size()]);
	}

	private static Comment convert(com.atlassian.jira.rest.client.domain.Comment comment) {
		Comment outComment = new Comment();

		outComment.setAuthor(comment.getAuthor().getDisplayName());
		outComment.setComment(comment.getBody());
		outComment.setCreated(comment.getCreationDate().toDate());
		outComment.setMarkupDetected(true);

		Visibility visibility = comment.getVisibility();
		if (visibility != null) {
			outComment.setRoleLevel(visibility.getValue());
		}

		return outComment;
	}

	private static IssueLink[] convertIssueLinks(Iterable<com.atlassian.jira.rest.client.domain.IssueLink> issueLinks) {

		List<IssueLink> outIssueLinks = new ArrayList<IssueLink>();

		for (com.atlassian.jira.rest.client.domain.IssueLink issueLink : issueLinks) {
			outIssueLinks.add(convert(issueLink));
		}

		return outIssueLinks.toArray(new IssueLink[outIssueLinks.size()]);
	}

	private static IssueLink convert(com.atlassian.jira.rest.client.domain.IssueLink issueLink) {
		IssueLink outIssueLink = new IssueLink(generateIssueId(issueLink.getTargetIssueUri().toString(),
				issueLink.getTargetIssueKey()), issueLink.getTargetIssueKey(), issueLink.getIssueLinkType().getName(),
				issueLink.getIssueLinkType().getName(), issueLink.getIssueLinkType().getDescription(), "");

		return outIssueLink;

	}

	private static Version[] convertVersions(Iterable<com.atlassian.jira.rest.client.domain.Version> versions) {
		List<Version> outVersions = new ArrayList<Version>();

		for (com.atlassian.jira.rest.client.domain.Version version : versions) {
			outVersions.add(convert(version));
		}

		return outVersions.toArray(new Version[outVersions.size()]);
	}

	private static Version convert(com.atlassian.jira.rest.client.domain.Version version) {
		Version outVersion = new Version(version.getId().toString());

		outVersion.setName(version.getName());
		outVersion.setReleaseDate(version.getReleaseDate().toDate());
		outVersion.setArchived(version.isArchived());
		outVersion.setReleased(version.isReleased());

		return outVersion;
	}

	private static Component[] convertComponents(Iterable<BasicComponent> components) {

		List<Component> outComponents = new ArrayList<Component>();

		for (BasicComponent component : components) {
			outComponents.add(convert(component));
		}

		return outComponents.toArray(new Component[outComponents.size()]);
	}

	private static Component convert(BasicComponent component) {
		Component outComponent = new Component(component.getId().toString());

		outComponent.setName(component.getName());

		return outComponent;
	}

	private static IssueType convert(BasicIssueType issueType) {
		IssueType outIssueType = new IssueType(issueType.getId().toString(), issueType.isSubtask());

		outIssueType.setName(issueType.getName());
		return outIssueType;
	}

	private static Subtask[] convert(Iterable<com.atlassian.jira.rest.client.domain.Subtask> allSubtasks) {
		List<Subtask> subtasks = new ArrayList<Subtask>();

		for (com.atlassian.jira.rest.client.domain.Subtask subtask : allSubtasks) {
			subtasks.add(convert(subtask));
		}

		return subtasks.toArray(new Subtask[subtasks.size()]);
	}

	private static Subtask convert(com.atlassian.jira.rest.client.domain.Subtask subtask) {
		// TODO rest use real id once available 
		return new Subtask(generateIssueId(subtask.getIssueUri().toString(), subtask.getIssueKey()),
				subtask.getIssueKey());
	}

	private static String generateIssueId(String uri, String issueKey) {
		return uri + "_" + issueKey.replace('-', '*');
	}

	public static IssueType[] convertIssueTypes(Iterable<com.atlassian.jira.rest.client.domain.IssueType> allIssueTypes) {
		List<IssueType> issueTypes = new ArrayList<IssueType>();

		for (com.atlassian.jira.rest.client.domain.IssueType issueType : allIssueTypes) {
			issueTypes.add(convert(issueType));
		}

		return issueTypes.toArray(new IssueType[issueTypes.size()]);
	}

	private static IssueType convert(com.atlassian.jira.rest.client.domain.IssueType issueType) {
		IssueType outIssueType = new IssueType();

		outIssueType.setId(issueType.getId().toString());
		outIssueType.setName(issueType.getName());
		outIssueType.setDescription(issueType.getDescription());
		outIssueType.setIcon(issueType.getIconUri().toString());
		outIssueType.setSubTaskType(issueType.isSubtask());

		return outIssueType;
	}

	public static List<JiraIssue> convertIssues(Iterable<BasicIssue> issues) {
		List<JiraIssue> outIssues = new ArrayList<JiraIssue>();

		for (BasicIssue issue : issues) {
			outIssues.add(convert(issue));
		}

		return outIssues;
	}

	private static JiraIssue convert(BasicIssue issue) {
		JiraIssue outIssue = new JiraIssue();

		// TODO rest set real id
		outIssue.setId(generateIssueId(issue.getSelf().toString(), issue.getKey()));
		outIssue.setKey(issue.getKey());
		outIssue.setSelf(issue.getSelf());

		return outIssue;
	}
}
