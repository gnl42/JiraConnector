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

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 */
public class CrucibleClient {

	private final CrucibleClientData clientData;

	private final AbstractWebLocation location;

	private final CrucibleServerCfg serverCfg;

	private final CrucibleServerFacade crucibleServer;

	public CrucibleClient(AbstractWebLocation location, CrucibleServerCfg serverCfg,
			CrucibleServerFacade crucibleServer, CrucibleClientData data) {
		this.location = location;
		this.clientData = data;
		this.serverCfg = serverCfg;
		this.crucibleServer = crucibleServer;
	}

	public void validate(IProgressMonitor monitor) throws CoreException {
		try {
			crucibleServer.testServerConnection(serverCfg);
		} catch (CrucibleLoginException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e));
		} catch (RemoteApiException e) {
			throw new CoreException(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}
}
