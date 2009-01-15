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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions;

import com.atlassian.theplugin.commons.VersionedVirtualFile;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * Action to open a version file
 * 
 * @author Shawn Minto
 */
public class OpenVersionedVirtualFileAction extends Action {
	private final VersionedVirtualFile virtualFile;

	public OpenVersionedVirtualFileAction(VersionedVirtualFile fileDescriptor) {
		this.virtualFile = fileDescriptor;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return TasksUiImages.REPOSITORY;
	}

	@Override
	public String getText() {
		return "Open File";
	}

	@Override
	public void run() {
		MessageDialog.openInformation(null, "Unsupported Operation", "This operation is currently unsupported");
//		TeamUiUtils.openFile(virtualFile.getRepoUrl(), virtualFile.getUrl(), virtualFile.getRevision(),
//				new NullProgressMonitor());
	}
}