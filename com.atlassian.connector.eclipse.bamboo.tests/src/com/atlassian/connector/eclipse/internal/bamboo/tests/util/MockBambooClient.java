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

package com.atlassian.connector.eclipse.internal.bamboo.tests.util;

import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

/**
 * Mock implementation for testing
 * 
 * @author Thomas Ehrnhoefer
 */
public class MockBambooClient extends BambooClient {

	private final Map<TaskRepository, Object> responses = new HashMap<TaskRepository, Object>();

	public MockBambooClient() {
		super(null, null, null, null);
	}

	@Override
	public void addCommentToBuild(IProgressMonitor monitor, TaskRepository repository, BambooBuild build, String comment)
			throws CoreException {
		// TODO
	}

	@Override
	public void addLabelToBuild(IProgressMonitor monitor, TaskRepository repository, BambooBuild build, String label)
			throws CoreException {
		// TODO
	}

	@Override
	public BuildDetails getBuildDetails(IProgressMonitor monitor, TaskRepository taskRepository, BambooBuild build)
			throws CoreException {
		// TODO
		return null;
	}

	@Override
	public String getBuildLogs(IProgressMonitor monitor, TaskRepository taskRepository, BambooBuild build)
			throws CoreException {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<BambooBuild> getBuilds(IProgressMonitor monitor, TaskRepository taskRepository,
			boolean promptForCredentials) throws CoreException {
		Assert.assertNotNull(responses.get(taskRepository));
		Assert.assertTrue(responses.get(taskRepository) instanceof Collection);
		return (Collection<BambooBuild>) responses.get(taskRepository);
	}

	@Override
	public BambooClientData getClientData() {
		// TODO
		return null;
	}

	@Override
	public boolean hasRepositoryData() {
		// TODO
		return false;
	}

	@Override
	public void runBuild(IProgressMonitor monitor, TaskRepository repository, BambooBuild build) throws CoreException {
		// TODO
	}

	@Override
	public void validate(IProgressMonitor monitor, TaskRepository taskRepository) throws CoreException {
		// TODO
	}

	public void setResponse(Object response, TaskRepository repository) {
		this.responses.put(repository, response);
	}

}
