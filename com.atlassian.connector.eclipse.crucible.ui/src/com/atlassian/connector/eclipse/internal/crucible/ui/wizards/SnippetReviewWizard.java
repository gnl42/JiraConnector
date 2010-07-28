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

import com.atlassian.connector.eclipse.internal.crucible.ui.operations.CreateSnippetReviewJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.OpenReviewAsTaskJob;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;

public class SnippetReviewWizard extends Wizard {

	private final ResourceEditorBean selection;

	private SnippetDetailsPage detailsPage;

	private final TaskRepository taskRepository;

	public SnippetReviewWizard(TaskRepository taskRepository, ResourceEditorBean selection) {
		setWindowTitle("New Crucible Snippet Review");
		setNeedsProgressMonitor(true);

		this.taskRepository = taskRepository;
		this.selection = selection;
	}

	@Override
	public void addPages() {
		super.addPages();

		detailsPage = new SnippetDetailsPage(getTaskRepository());
		addPage(detailsPage);
	}

	private TaskRepository getTaskRepository() {
		return taskRepository;
	}

	@Override
	public boolean performFinish() {
		final CreateSnippetReviewJob job = new CreateSnippetReviewJob(getTaskRepository(), detailsPage.getReview(),
				selection);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (job.getStatus().isOK()) {
					new OpenReviewAsTaskJob(getTaskRepository(), job.getCreatedReview()).schedule();
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(WorkbenchUtil.getShell(), "Failed to create review",
									job.getStatus().getMessage());
						}
					});
				}
			}
		});
		job.schedule();
		return true;
	}
}
