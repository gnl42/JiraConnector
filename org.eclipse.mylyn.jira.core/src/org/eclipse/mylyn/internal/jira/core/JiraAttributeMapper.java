/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.web.rss.JiraRssHandler;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

/**
 * @author Steffen Pingel
 */
public class JiraAttributeMapper extends TaskAttributeMapper implements ITaskAttributeMapper2 {

	private final JiraClient client;

	public JiraAttributeMapper(TaskRepository taskRepository, JiraClient client) {
		super(taskRepository);
		this.client = client;
	}

	@Override
	public Date getDateValue(TaskAttribute attribute) {
		if (JiraUtil.isCustomDateTimeAttribute(attribute)) {
			try {
				return JiraRssHandler.getDateTimeFormat().parse(attribute.getValue());
			} catch (ParseException e) {
				return null;
			}
		} else {
			return super.getDateValue(attribute);
		}
	}

	@Override
	public void setDateValue(TaskAttribute attribute, Date date) {
		if (JiraUtil.isCustomDateTimeAttribute(attribute)) {
			attribute.setValue(JiraRssHandler.getDateTimeFormat().format(date));
		} else {
			super.setDateValue(attribute, date);
		}
	}

	@Override
	public String getValueLabel(TaskAttribute taskAttribute) {
		if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
			return JiraUtil.getTimeFormat(getTaskRepository()).format(getLongValue(taskAttribute));
		}
		return super.getValueLabel(taskAttribute);
	}

	@Override
	public List<String> getValueLabels(TaskAttribute taskAttribute) {
		if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
			return Collections.singletonList(JiraUtil.getTimeFormat(getTaskRepository()).format(
					getLongValue(taskAttribute)));
		}
		return super.getValueLabels(taskAttribute);
	}

	@Override
	public String mapToRepositoryKey(TaskAttribute parent, String key) {
		if (TaskAttribute.COMPONENT.equals(key)) {
			return JiraAttribute.COMPONENTS.id();
		} else if (TaskAttribute.TASK_KIND.equals(key)) {
			return JiraAttribute.TYPE.id();
		} else if (TaskAttribute.DATE_DUE.equals(key)) {
			return JiraAttribute.DUE_DATE.id();
		}
		return super.mapToRepositoryKey(parent, key);
	}

	@Override
	public Map<String, String> getOptions(TaskAttribute attribute) {
		Map<String, String> options = getRepositoryOptions(attribute);
		return (options != null) ? options : super.getOptions(attribute);
	}

	public Map<String, String> getRepositoryOptions(TaskAttribute attribute) {
		if (client.getCache().hasDetails()) {
			Map<String, String> options = new LinkedHashMap<String, String>();
			if (JiraAttribute.PROJECT.id().equals(attribute.getId())) {
				Project[] jiraProjects = client.getCache().getProjects();
				for (Project jiraProject : jiraProjects) {
					options.put(jiraProject.getId(), jiraProject.getName());
				}
				return options;
			} else if (JiraAttribute.RESOLUTION.id().equals(attribute.getId())) {
				Resolution[] jiraResolutions = client.getCache().getResolutions();
				for (Resolution resolution : jiraResolutions) {
					options.put(resolution.getId(), resolution.getName());
				}
				return options;
			} else if (JiraAttribute.PRIORITY.id().equals(attribute.getId())) {
				Priority[] jiraPriorities = client.getCache().getPriorities();
				for (Priority priority : jiraPriorities) {
					options.put(priority.getId(), priority.getName());
				}
				return options;
			} else if (JiraAttribute.TYPE.id().equals(attribute.getId())) {
				boolean isSubTask = JiraTaskDataHandler.hasSubTaskType(attribute);
				IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
				for (IssueType issueType : jiraIssueTypes) {
					if (!isSubTask || issueType.isSubTaskType()) {
						options.put(issueType.getId(), issueType.getName());
					}
				}
				return options;
			} else {
				TaskAttribute projectAttribute = attribute.getTaskData().getRoot().getMappedAttribute(
						JiraAttribute.PROJECT.id());
				if (projectAttribute != null) {
					Project project = client.getCache().getProjectById(projectAttribute.getValue());
					if (project != null) {
						if (JiraAttribute.COMPONENTS.id().equals(attribute.getId())) {
							for (Component component : project.getComponents()) {
								options.put(component.getId(), component.getName());
							}
							return options;
						} else if (JiraAttribute.AFFECTSVERSIONS.id().equals(attribute.getId())) {
							for (Version version : project.getVersions()) {
								options.put(version.getId(), version.getName());
							}
							return options;
						} else if (JiraAttribute.FIXVERSIONS.id().equals(attribute.getId())) {
							for (Version version : project.getVersions()) {
								options.put(version.getId(), version.getName());
							}
							return options;
						}
					}
				}
			}
		}
		return null;
	}

}