/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylar.internal.jira.ui.JiraRepositorySettingsPage;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryClient;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.internal.tasklist.ITask;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.eclipse.mylar.internal.tasklist.ui.wizards.AbstractRepositorySettingsPage;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryClient extends AbstractRepositoryClient {

	private static final String CLIENT_LABEL = "JIRA";
	
	public String getLabel() {
		return CLIENT_LABEL;
	}

	public String getKind() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	public ITask createTaskFromExistingId(TaskRepository repository, String id) {
		// ignore
		return null;
	}

	public void synchronize() {
		// ignore
	}

	public Job synchronize(ITask task, boolean forceUpdate, IJobChangeListener listener) {
		// ignore
		return null;
	}

	public void synchronize(AbstractRepositoryQuery repositoryQuery) {
		// ignore
	}

	public AbstractRepositorySettingsPage getSettingsPage() {
		return new JiraRepositorySettingsPage();
	}

	public IWizard getQueryWizard(TaskRepository repository) {
		return null;
	}

	public void openEditQueryDialog(AbstractRepositoryQuery query) {
		// ignore
	}

	public IWizard getAddExistingTaskWizard(TaskRepository repository) {
		return null;
	}

}
