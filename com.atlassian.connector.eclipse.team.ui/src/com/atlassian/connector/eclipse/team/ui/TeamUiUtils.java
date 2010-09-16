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

package com.atlassian.connector.eclipse.team.ui;

import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.widgets.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
@SuppressWarnings("restriction")
public final class TeamUiUtils {
	public static final String TEAM_PROV_ID_SVN_SUBVERSIVE = "org.eclipse.team.svn.core.svnnature";

	public static final String TEAM_PROVIDER_ID_CVS_ECLIPSE = "org.eclipse.team.cvs.core.cvsnature";

	public static final String TEAM_PROV_ID_SVN_SUBCLIPSE = "org.tigris.subversion.subclipse.core.svnnature";

	private static DefaultTeamUiResourceConnector defaultConnector = new DefaultTeamUiResourceConnector();

	private TeamUiUtils() {
	}

	public static boolean hasNoTeamConnectors() {
		return AtlassianTeamUiPlugin.getDefault().getTeamResourceManager().getTeamConnectors().size() == 0;
	}

	public static boolean checkTeamConnectors() {
		if (hasNoTeamConnectors()) {
			handleMissingTeamConnectors();
			return false;
		}
		return true;
	}

	@Nullable
	public static ScmRepository getApplicableRepository(@NotNull IResource resource) {
		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamUiResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				try {
					ScmRepository res = connector.getApplicableRepository(resource);
					if (res != null) {
						return res;
					}
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianTeamUiPlugin.PLUGIN_ID, e.getMessage(), e));
					// and try the next connector
				}
			}
		}
		return null;

	}

	public static LocalStatus getLocalRevision(@NotNull IResource resource) throws CoreException {
		ITeamUiResourceConnector connector = AtlassianTeamUiPlugin.getDefault()
				.getTeamResourceManager()
				.getTeamConnector(resource);

		if (connector != null && connector.isEnabled()) {
			LocalStatus res = connector.getLocalRevision(resource);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	public static void handleMissingTeamConnectors() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				new MessageDialog(
						WorkbenchUtil.getShell(),
						"No Atlassian SCM Integration installed",
						null,
						"In order to access this functionality you need to install an Atlassian SCM Integration feature.\n\n"
								+ "You may install them by opening: Help | Install New Software, selecting 'Atlassian Connector for Eclipse' Update Site "
								+ "and chosing one or more integation features in 'Atlassian Integrations' category.",
						MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0).open();
			}
		});
	}

	/**
	 * @param monitor
	 *            progress monitor
	 * @return all supported repositories configured in current workspace
	 */
	@NotNull
	public static Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		TeamUiResourceManager teamResourceManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();
		Collection<ScmRepository> res = MiscUtil.buildArrayList();

		for (ITeamUiResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled()) {
				res.addAll(connector.getRepositories(monitor));
			}
		}
		res.addAll(defaultConnector.getRepositories(monitor));
		return res;

	}

}
