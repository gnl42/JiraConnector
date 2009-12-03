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

package com.atlassian.connector.eclipse.team.core;

import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
public final class TeamUtils {

	public static final String TEAM_PROVIDER_ID_CVS_ECLIPSE = "org.eclipse.team.cvs.core.cvsnature";

	public static final String TEAM_PROV_ID_SVN_SUBCLIPSE = "org.tigris.subversion.subclipse.core.svnnature";

	public static final String TEAM_PROV_ID_SVN_SUBVERSIVE = "org.eclipse.team.svn.core.svnnature";

	public static DefaultTeamResourceConnector defaultConnector = new DefaultTeamResourceConnector();

	private TeamUtils() {
	}

	public static DefaultTeamResourceConnector getDefaultConnector() {
		return defaultConnector;
	}

	public static Collection<String> getSupportedTeamConnectors() {
		Collection<String> res = MiscUtil.buildArrayList();
		TeamResourceManager teamResourceManager = AtlassianTeamCorePlugin.getDefault().getTeamResourceManager();
		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				res.add(connector.getName());
			}
		}
		res.add(defaultConnector.getName());
		return res;
	}

	/**
	 * @param monitor
	 *            progress monitor
	 * @return all supported repositories configured in current workspace
	 */
	@NotNull
	public static Collection<RepositoryInfo> getRepositories(IProgressMonitor monitor) {
		TeamResourceManager teamResourceManager = AtlassianTeamCorePlugin.getDefault().getTeamResourceManager();
		Collection<RepositoryInfo> res = MiscUtil.buildArrayList();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
//				try {
				res.addAll(connector.getRepositories(monitor));
//				} catch (CoreException e) {
//					StatusHandler.log(new Status(IStatus.WARNING, AtlassianUiPlugin.PLUGIN_ID,
//							"Cannot get repositories for a connector"));
//					// ignore and try other connector(s)
//				}
			}
		}
		res.addAll(defaultConnector.getRepositories(monitor));
		return res;

	}

	@Nullable
	public static RepositoryInfo getApplicableRepository(@NotNull IResource resource) {
		TeamResourceManager teamResourceManager = AtlassianTeamCorePlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					RepositoryInfo res = connector.getApplicableRepository(resource);
					if (res != null) {
						return res;
					}
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamCorePlugin.PLUGIN_ID, e.getMessage(), e));
					// and try the next connector
				}
			}
		}
		return null;

	}

	public static RevisionInfo getLocalRevision(@NotNull IResource resource) throws CoreException {
		ITeamResourceConnector connector = AtlassianTeamCorePlugin.getDefault()
				.getTeamResourceManager()
				.getTeamConnector(resource);

		if (connector != null && connector.isEnabled()) {
			RevisionInfo res = connector.getLocalRevision(resource);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

}
