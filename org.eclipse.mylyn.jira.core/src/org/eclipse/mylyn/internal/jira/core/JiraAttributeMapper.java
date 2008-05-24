/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeProperties;

/**
 * @author Steffen Pingel
 */
public class JiraAttributeMapper extends TaskAttributeMapper {

	private final JiraClient client;

	public JiraAttributeMapper(TaskRepository taskRepository, JiraClient client) {
		super(taskRepository);
		this.client = client;
	}

	@Override
	public String mapToRepositoryKey(TaskAttribute parent, String key) {
		if (TaskAttribute.COMPONENT.equals(key)) {
			return JiraAttribute.COMPONENTS.getId();
		}
		return super.mapToRepositoryKey(parent, key);
	}

	@Override
	public String getType(TaskAttribute taskAttribute) {
		TaskAttributeProperties properties = TaskAttributeProperties.from(taskAttribute);
		if (properties.getType() != null) {
			return properties.getType();
		}

		JiraFieldType type = JiraFieldType.fromKey(taskAttribute.getMetaData(IJiraConstants.META_TYPE));
		if (type.getTaskType() != null) {
			return type.getTaskType();
		}

		return TaskAttribute.TYPE_SHORT_TEXT;
	}

	@Override
	public Map<String, String> getOptions(TaskAttribute attribute) {
		if (client.getCache().hasDetails()) {
			Map<String, String> options = new HashMap<String, String>();
			if (JiraAttribute.PROJECT.getId().equals(attribute.getId())) {
				Project[] jiraProjects = client.getCache().getProjects();
				for (Project jiraProject : jiraProjects) {
					options.put(jiraProject.getId(), jiraProject.getName());
				}
				return options;
			} else if (JiraAttribute.RESOLUTION.getId().equals(attribute.getId())) {
				Resolution[] jiraResolutions = client.getCache().getResolutions();
				for (Resolution resolution : jiraResolutions) {
					options.put(resolution.getId(), resolution.getName());
				}
				return options;
			} else if (JiraAttribute.PRIORITY.getId().equals(attribute.getId())) {
				Priority[] jiraPriorities = client.getCache().getPriorities();
				for (Priority priority : jiraPriorities) {
					options.put(priority.getId(), priority.getName());
				}
				return options;
			} else if (JiraAttribute.TYPE.getId().equals(attribute.getId())) {
				IssueType[] jiraIssueTypes = client.getCache().getIssueTypes();
				for (IssueType issueType : jiraIssueTypes) {
					options.put(issueType.getId(), issueType.getName());
				}
				return options;
			} else {
				TaskAttribute projectAttribute = attribute.getTaskData().getMappedAttribute(
						JiraAttribute.PROJECT.getId());
				if (projectAttribute != null) {
					Project project = client.getCache().getProjectById(projectAttribute.getValue());
					if (project != null) {
						if (JiraAttribute.COMPONENTS.getId().equals(attribute.getId())) {
							for (Component component : project.getComponents()) {
								options.put(component.getId(), component.getName());
							}
							return options;
						} else if (JiraAttribute.AFFECTSVERSIONS.getId().equals(attribute.getId())) {
							for (Version version : project.getVersions()) {
								options.put(version.getId(), version.getName());
							}
							return options;
						} else if (JiraAttribute.FIXVERSIONS.getId().equals(attribute.getId())) {
							for (Version version : project.getVersions()) {
								options.put(version.getId(), version.getName());
							}
							return options;
						}
					}
				}
			}
		}
		return super.getOptions(attribute);
	}

}