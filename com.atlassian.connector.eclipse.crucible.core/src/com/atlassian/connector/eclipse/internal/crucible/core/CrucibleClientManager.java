/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core;

import java.io.File;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.provisional.tasks.core.RepositoryClientManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 */
public class CrucibleClientManager extends RepositoryClientManager<CrucibleClient, CrucibleClientData> {

	public CrucibleClientManager(File cacheFile) {
		super(cacheFile);
	}

	@Override
	protected CrucibleClient createClient(TaskRepository taskRepository, CrucibleClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		return new CrucibleClient(location, data);
	}

	@Override
	protected CrucibleClientData createRepositoryConfiguration() {
		return new CrucibleClientData();
	}

}
