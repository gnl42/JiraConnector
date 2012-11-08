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

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClientCache;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;

public class JiraRestConverter {

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

	public static JiraIssue convertIssue(Issue issue, JiraClientCache cache, IProgressMonitor monitor)
			throws JiraException {
		JiraIssue jiraIssue = new JiraIssue();

		// TODO rest: set real id if avaialble
		jiraIssue.setId(Integer.toString(issue.getSelf().toString().hashCode()));
		jiraIssue.setSelf(issue.getSelf());
		jiraIssue.setKey(issue.getKey());
		jiraIssue.setSummary(issue.getSummary());
		// TODO rest: description html vs text vs wiki markap
		jiraIssue.setDescription(issue.getDescription());
//		jiraIssue.setParentId();
//		jiraIssue.setParentKey();
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

//		jiraIssue.setSecurityLevel(issue.get)

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

		return jiraIssue;
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
}
