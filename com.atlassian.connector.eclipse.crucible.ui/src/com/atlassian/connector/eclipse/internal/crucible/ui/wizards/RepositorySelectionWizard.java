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

import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Wizard for selecting a ewpository and opening the newCrucibleReviewWizard afterwards
 * 
 * @author thomas
 */
public class RepositorySelectionWizard extends Wizard implements INewWizard {

	private final SelectCrucibleRepositoryPage selectRepositoryPage;

	public RepositorySelectionWizard(SortedSet<ICustomChangesetLogEntry> logEntries) {
		this.selectRepositoryPage = new SelectCrucibleRepositoryPage(logEntries);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWindowTitle("Create");
		setDefaultPageImageDescriptor(TasksUiImages.BANNER_REPOSITORY);
	}

	public RepositorySelectionWizard() {
		this(new TreeSet<ICustomChangesetLogEntry>());
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore
	}

	@Override
	public void addPages() {
		addPage(selectRepositoryPage);
	}

	@Override
	public boolean canFinish() {
		return selectRepositoryPage.canFinish();
	}

	@Override
	public boolean performFinish() {
		return selectRepositoryPage.performFinish();
	}

}
