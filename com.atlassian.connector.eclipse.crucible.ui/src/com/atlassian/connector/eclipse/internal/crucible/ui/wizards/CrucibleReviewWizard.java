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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for creating a new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewWizard extends Wizard implements INewWizard {
	private final TaskRepository taskRepository;

	public CrucibleReviewWizard(TaskRepository taskRepository) {
		setWindowTitle("New Crucible Review");
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		this.taskRepository = taskRepository;
	}

	@Override
	public void addPages() {
		addPage(new ReviewTypeSelectionPage(getTaskRepository()));
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}
}
