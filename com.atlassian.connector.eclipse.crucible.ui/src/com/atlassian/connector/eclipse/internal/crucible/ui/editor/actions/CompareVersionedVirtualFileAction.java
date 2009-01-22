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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

/**
 * Action to open the compare editor given a crucible file with 2 revisions
 * 
 * @author Shawn Minto
 */
public class CompareVersionedVirtualFileAction extends Action {

	private final CrucibleFileInfo crucibleFile;

	public CompareVersionedVirtualFileAction(CrucibleFileInfo crucibleFile) {
		this.crucibleFile = crucibleFile;
	}

	@Override
	public void run() {
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					VersionedVirtualFile newVirtualFile = crucibleFile.getFileDescriptor();

					VersionedVirtualFile oldVirtualFile = crucibleFile.getOldFileDescriptor();

					TeamUiUtils.openCompareEditor(newVirtualFile.getRepoUrl(), newVirtualFile.getUrl(),
							oldVirtualFile.getRevision(), newVirtualFile.getRevision(), monitor);
				}
			});
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch (OperationCanceledException e) {
			// ignore since the user requested a cancel
		}
	}

}
