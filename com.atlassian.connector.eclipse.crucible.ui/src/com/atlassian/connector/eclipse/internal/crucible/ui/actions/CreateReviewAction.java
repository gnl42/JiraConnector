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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleRepositoryPage;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Action for creating a review from a revision
 * 
 * @author Thomas Ehrnhoefer
 */
public class CreateReviewAction extends BaseSelectionListenerAction implements IActionDelegate {

	private ISelection selection;

	public CreateReviewAction() {
		super("New Crucible Review...");
	}

	public void run(IAction action) {
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return;
		}
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		final SortedSet<ICustomChangesetLogEntry> logEntries = new TreeSet<ICustomChangesetLogEntry>();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ICustomChangesetLogEntry) {
				logEntries.add((ICustomChangesetLogEntry) obj);
			}
		}
		RepositorySelectionWizard wizard = new RepositorySelectionWizard(new SelectCrucibleRepositoryPage(
				SelectCrucibleRepositoryPage.CRUCIBLE_REPOSITORY_FILTER) {
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository, new HashSet<ReviewWizard.Type>(
						Arrays.asList(ReviewWizard.Type.ADD_CHANGESET)));
				wizard.setLogEntries(logEntries);
				return wizard;
			}
		});

		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
		wd.setBlockOnOpen(true);
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selected) {
		this.selection = selected;
	}
}
