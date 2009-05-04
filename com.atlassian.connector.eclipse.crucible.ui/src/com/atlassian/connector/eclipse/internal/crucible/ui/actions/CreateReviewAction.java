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
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

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
		SortedSet<ICustomChangesetLogEntry> logEntries = new TreeSet<ICustomChangesetLogEntry>();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ICustomChangesetLogEntry) {
				logEntries.add((ICustomChangesetLogEntry) obj);
			}
		}
		RepositorySelectionWizard wizard = new RepositorySelectionWizard(logEntries);

		WizardDialog wd = new WizardDialog(TasksUiInternal.getShell(), wizard);
		wd.setBlockOnOpen(true);
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selected) {
		this.selection = selected;
	}

}
