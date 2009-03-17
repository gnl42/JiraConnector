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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog.Type;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * Action to add a label to a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 */
public class AddLabelToBuildAction extends AbstractBambooAction {
	public AddLabelToBuildAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
	}

	public AddLabelToBuildAction(BambooBuild build) {
		super(build);
	}

	@Override
	public void run() {
		final BambooBuild build = getBuild();
		if (build != null) {
			AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(null, build, TasksUi.getRepositoryManager()
					.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()), Type.LABEL);
			dialog.open();
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			try {
				((BambooBuild) selection.getFirstElement()).getNumber();
				return true;
			} catch (UnsupportedOperationException e) {
				// ignore
			}
		}
		return false;
	}
}
