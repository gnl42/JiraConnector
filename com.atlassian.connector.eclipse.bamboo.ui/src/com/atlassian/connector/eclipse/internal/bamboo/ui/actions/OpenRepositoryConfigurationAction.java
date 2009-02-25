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
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class OpenRepositoryConfigurationAction extends BaseSelectionListenerAction {
	private TaskRepository repository;

	private boolean linkedAction = false;

	private final TreeViewer buildViewer;

	public OpenRepositoryConfigurationAction(TreeViewer buildViewer) {
		this(null, buildViewer);
	}

	public OpenRepositoryConfigurationAction(TaskRepository repository, TreeViewer buildViewer) {
		super(null);
		this.repository = repository;
		this.buildViewer = buildViewer;
		linkedAction = true;
	}

	@Override
	public void run() {
		if (repository != null && linkedAction) {
			openConfiguration();
		} else {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					repository = TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
							build.getServerUrl());
					openConfiguration();
				}
			}
		}
	}

	private void openConfiguration() {
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
