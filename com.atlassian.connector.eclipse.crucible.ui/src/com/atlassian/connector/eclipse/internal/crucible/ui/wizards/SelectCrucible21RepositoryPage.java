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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public abstract class SelectCrucible21RepositoryPage extends SelectCrucibleRepositoryPage {

	public static final String IS_VERSION_2_1 = "com.atlassian.connector.eclipse.crucible.core.isVersion2.1";

	private CrucibleRepositorySelectionWizard crucibleRepositoryWizard;

	public SelectCrucible21RepositoryPage() {
		super(SelectCrucibleRepositoryPage.ENABLED_CRUCIBLE_REPOSITORY_FILTER);
		setDescription("Add new repositories using the Task Repositories view.\nOnly Crucible 2.1 or newer supports current operation.");
		getMessage();
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		if (crucibleRepositoryWizard == null && getWizard() != null
				&& getWizard() instanceof CrucibleRepositorySelectionWizard) {
			crucibleRepositoryWizard = (CrucibleRepositorySelectionWizard) getWizard();
		}

		if (crucibleRepositoryWizard == null) {
			return;
		}

		if (visible) {
			// retrieve Crucible repos version and filter list of available repos in the wizard
			final WizardPage page = this;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					final Collection<TaskRepository> crucible21Repos = getCrucible21Repos(page);

					// we need our own content provider to inject filtered list of repos
					getViewer().setContentProvider(new LocalRepositoryProvider(crucible21Repos));
					getViewer().setInput(crucible21Repos);
				}
			});
		}
	}

	private Collection<TaskRepository> getCrucible21Repos(final WizardPage page) {
		final Collection<TaskRepository> crucible21Repos = new ArrayList<TaskRepository>();

		if (crucibleRepositoryWizard != null) {
			for (final TaskRepository repo : getRepositories()) {
				if (repo.getProperty(IS_VERSION_2_1) == null) {
					crucibleRepositoryWizard.updateRepoVersion(page, repo);
				}
			}

			for (final TaskRepository repo : getRepositories()) {
				if (Boolean.valueOf(repo.getProperty(IS_VERSION_2_1))) {
					crucible21Repos.add(repo);
				}
			}
		}
		return crucible21Repos;
	}

	public void setWizard(CrucibleRepositorySelectionWizard wizard) {
		this.crucibleRepositoryWizard = wizard;
		super.setWizard(wizard);
	}

	private final class LocalRepositoryProvider implements IStructuredContentProvider {
		private Collection<TaskRepository> crucible21Repos;

		private boolean newRepositoryAdded = false;

		private LocalRepositoryProvider(Collection<TaskRepository> crucible21Repos) {
			this.crucible21Repos = crucible21Repos;
		}

		public Object[] getElements(Object inputElement) {
			if (newRepositoryAdded) {
				// filter list once again as it comes from Mylyn
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						crucible21Repos = getCrucible21Repos(SelectCrucible21RepositoryPage.this);
						getViewer().setInput(crucible21Repos);
					}
				});
				return new Object[0];
			}
			return crucible21Repos.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput != crucible21Repos && oldInput == crucible21Repos) {
				// new repository added using the button in the wizard 
				// (input comes from the base Mylyn class which means it is not filtered)
				newRepositoryAdded = true;
			} else {
				newRepositoryAdded = false;
			}
		}
	}
}
