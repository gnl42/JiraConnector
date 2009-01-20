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

package com.atlassian.connector.eclipse.ui.team;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * A utility class for doing UI related operations for team items
 * 
 * @author Shawn Minto
 */
public final class TeamUiUtils {

	private static DefaultTeamResourceConnector defaultConnector = new DefaultTeamResourceConnector();

	private TeamUiUtils() {
	}

	public static void openFile(String repoUrl, String filePath, String revisionString, IProgressMonitor monitor) {
		// TODO if the repo url is null, we should probably use the task repo host and look at all repos

		assert (filePath != null);
		assert (revisionString != null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		TeamResourceManager teamResourceManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();

		for (ITeamResourceConnector connector : teamResourceManager.getTeamConnectors()) {
			if (connector.isEnabled() && connector.canHandleFile(repoUrl, filePath, revisionString, monitor)) {
				if (connector.openFile(repoUrl, filePath, revisionString, monitor)) {
					return;
				}
			}
		}

		// try a backup solution
		defaultConnector.openFile(repoUrl, filePath, revisionString, monitor);
	}

	public static void openLocalResource(final IResource resource) {
		if (Display.getCurrent() != null) {
			openLocalResourceInternal(resource);
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					openLocalResourceInternal(resource);
				}
			});
		}
	}

	private static void openLocalResourceInternal(IResource resource) {
		// the local revision matches the revision we care about and the file is in sync
		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) resource, true);
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, AtlassianUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	public static void openFileDeletedErrorMessage() {
		MessageDialog.openInformation(null, "Unable to find file", "May have been deleted");
	}

	public static void openFileDoesntExistErrorMessage() {
		MessageDialog.openInformation(null, "Unable to find file", "This file does not exist in your local workspace");

	}
}
