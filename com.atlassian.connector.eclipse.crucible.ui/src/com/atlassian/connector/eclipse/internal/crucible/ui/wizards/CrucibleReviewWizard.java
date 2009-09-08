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

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.ui.INewWizard;

/**
 * Wizard for creating a new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewWizard extends NewTaskWizard implements INewWizard {
	public CrucibleReviewWizard(TaskRepository taskRepository) {
		super(taskRepository, null);
		setWindowTitle("New Crucible Review");
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(new ReviewTypeSelectionPage(getTaskRepository()));
	}
}
