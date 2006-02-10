/*******************************************************************************
 * Copyright (c) 2006 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.internal.jira.JiraFilter;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.tigris.jira.core.model.NamedFilter;

/**
 * Wizard that allows the user to select one of their named Jira filters on the
 * server
 * 
 * @author Wesley Coelho (initial integration patch)
 */
public class NewJiraQueryWizard extends Wizard {

	private final TaskRepository repository;

	private JiraQueryWizardPage queryPage;

	public NewJiraQueryWizard(TaskRepository repository) {
		this.repository = repository;
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		queryPage = new JiraQueryWizardPage(repository);
		addPage(queryPage);
	}

	@Override
	public boolean performFinish() {

		NamedFilter namedFilter = queryPage.getSelectedFilter();

		if (namedFilter != null) {
			JiraFilter filter = new JiraFilter(namedFilter, true);
			MylarTaskListPlugin.getTaskListManager().addQuery(filter);
		}
		// if (TaskListView.getDefault() != null) {
		// TaskListView.getDefault().getViewer().refresh();
		// }

		return true;
	}

}
