/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.internal.jira.JiraFilter;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryConnector;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.tigris.jira.core.model.NamedFilter;

/**
 * Wizard that allows the user to select one of their named Jira filters on the
 * server
 * 
 * @author Mik Kersten
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
			JiraFilter filter = new JiraFilter(repository.getUrl().toExternalForm(), namedFilter);
			MylarTaskListPlugin.getTaskListManager().addQuery(filter);
			AbstractRepositoryConnector connector = MylarTaskListPlugin.getRepositoryManager().getRepositoryConnector(repository.getKind());
			if (connector != null) {
				connector.synchronize(filter, null);
			}
//			filter.refreshHits();
		} 

		return true;
	}

}
