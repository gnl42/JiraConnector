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
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog.Type;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Action to add a label to a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 */
public class AddLabelToBuildAction extends BaseSelectionListenerAction {

	public AddLabelToBuildAction() {
		super(null);
		inititalize();
	}

	private void inititalize() {
		setText("Add Label to Build...");
		setToolTipText("Add Label to Build");
		setImageDescriptor(BambooImages.LABEL);
	}

	@Override
	public void run() {
		ISelection s = super.getStructuredSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) s;
			Object selected = selection.iterator().next();
			if (selected instanceof BambooBuild) {
				final BambooBuild build = (BambooBuild) selected;
				if (build != null) {
					AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(null, build,
							TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
									build.getServerUrl()), Type.LABEL);
					dialog.open();
				}
			}
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
