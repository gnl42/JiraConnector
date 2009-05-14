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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.Formatter;

/**
 * Create a task from failed build.
 * 
 * @author Pawel Niewiadomski
 */
public class NewTaskFromFailedBuildAction extends BaseSelectionListenerAction {
	public NewTaskFromFailedBuildAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setText("New Task From Failed Build...");
		setToolTipText("New Task From Failed Build...");
		setImageDescriptor(TasksUiImages.TASK_NEW);
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
					// TODO NLS externalize strings
					final StringBuilder sb = new StringBuilder();
					Formatter fmt = new Formatter(sb);
					fmt.format("\n-- Build %s-%d failed, please investigate and fix...\n", build.getPlanKey(),
							build.getNumber());
					sb.append("\nBuild Result: "); //$NON-NLS-1$
					sb.append(build.getResultUrl());

					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					TaskMapping taskMapping = new TaskMapping() {
						@Override
						public String getDescription() {
							return sb.toString();
						}
					};
					TasksUiUtil.openNewTaskEditor(shell, taskMapping, null);
				}
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			try {
				BambooBuild build = ((BambooBuild) selection.getFirstElement());
				build.getNumber(); // check if this is a valid build, it'll throw exc otherwise
				return build.getStatus().equals(BuildStatus.FAILURE);
			} catch (UnsupportedOperationException e) {
				// ignore
			}
		}
		return false;
	}
}