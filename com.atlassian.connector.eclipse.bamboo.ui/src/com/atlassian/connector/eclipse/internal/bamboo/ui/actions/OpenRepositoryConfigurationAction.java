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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * 
 * @author Wojciech Seliga
 */
public class OpenRepositoryConfigurationAction extends BaseSelectionListenerAction {

	public OpenRepositoryConfigurationAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setActionDefinitionId(IWorkbenchActionDefinitionIds.PROPERTIES);
		setText(BambooConstants.OPEN_REPOSITORY_PROPERTIES_ACTION_LABEL);
		setToolTipText("Open the repository configuration");
	}

	@Override
	public void run() {
		ISelection s = super.getStructuredSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) s;
			Object selected = selection.iterator().next();
			if (selected instanceof EclipseBambooBuild) {
				final EclipseBambooBuild eclipseBambooBuild = (EclipseBambooBuild) selected;
				openConfiguration(eclipseBambooBuild.getTaskRepository());
			} else if (selected instanceof TaskRepository) {
				TaskRepository repository = (TaskRepository) selected;
				openConfiguration(repository);
			}
		}
	}

	private void openConfiguration(final TaskRepository repository) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				TasksUiUtil.openEditRepositoryWizard(repository);
			}
		});
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1;
	}
}
