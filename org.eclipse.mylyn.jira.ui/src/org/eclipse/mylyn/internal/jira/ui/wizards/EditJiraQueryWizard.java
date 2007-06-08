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

package org.eclipse.mylyn.internal.jira.ui.wizards;

import org.eclipse.mylyn.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractEditQueryWizard;

/**
 * @author Rob Elves
 * @author Mik Kersten
 */
public class EditJiraQueryWizard extends AbstractEditQueryWizard {

//	private JiraQueryWizardPage queryPage;

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
		if(query instanceof JiraCustomQuery) {
			page = new JiraQueryPage(repository, (JiraCustomQuery) query);
		} else {
			page = new JiraQueryWizardPage(repository, query);
		}

		page.setWizard(this);
		addPage(page);
	}


	@Override
	public boolean canFinish() {
		if(page.getNextPage() == null) {
			return page.isPageComplete();
		}
		return page.getNextPage().isPageComplete();
	}
}
