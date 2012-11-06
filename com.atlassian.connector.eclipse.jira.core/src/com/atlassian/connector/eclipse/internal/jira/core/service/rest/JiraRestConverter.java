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

import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.jira.rest.client.domain.BasicProject;

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
		outResolution.setId(Integer.toString((resolution.getSelf().toString().hashCode())));

		return outResolution;
	}

}
