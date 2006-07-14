/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import org.eclipse.mylar.internal.tasks.ui.wizards.AbstractEditQueryWizard;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;

/**
 * @author Rob Elves
 * @author Mik Kersten
 */
public class EditJiraQueryWizard extends AbstractEditQueryWizard {

	private JiraQueryWizardPage queryPage;

	public EditJiraQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
		super(repository, query);
		setForcePreviousAndNextButtons(true);
//		this.repository = repository;
//		this.query = query;
//		setNeedsProgressMonitor(true);
//		setWindowTitle(TITLE);
	}

	@Override
	public void addPages() {
		queryPage = new JiraQueryWizardPage(repository, query);
		queryPage.setWizard(this);
		addPage(queryPage);
	}

	@Override
	public boolean performFinish() {

		AbstractRepositoryQuery q = queryPage.getQuery();
		if (q != null) {
			
//			String name;
//			if (q instanceof JiraRepositoryQuery) {
//				name = ((JiraRepositoryQuery) q).getNamedFilter().getName();
//			} else {
//				name = ((JiraCustomQuery) query).getFilterDefinition().getName();
//			}
			
			TasksUiPlugin.getTaskListManager().getTaskList().deleteQuery(query);
			TasksUiPlugin.getTaskListManager().getTaskList().addQuery(q);
			
			AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(repository.getKind());
			if (connector != null) {
				connector.synchronize(q, null);
			}
//			filter.refreshHits();
		} 

		return true;
	}

	@Override
	public boolean canFinish() {
		if(queryPage.getNextPage() == null) {
			return queryPage.isPageComplete();
		}
		return queryPage.getNextPage().isPageComplete();
	}
}
