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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.graphics.RGB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public abstract class SelectCrucible21RepositoryPage extends SelectCrucibleRepositoryPage {

	public static final String IS_VERSION_2_1 = "com.atlassian.connector.eclipse.crucible.core.isVersion2.1";

	private CrucibleRepositorySelectionWizard crucibleRepositoryWizard;

	protected TaskRepository selectedRepository;

	private Collection<TaskRepository> crucible21Repos;

	public SelectCrucible21RepositoryPage() {
		super(ENABLED_CRUCIBLE_REPOSITORY_FILTER);
		setDescription("Add new repositories using the Task Repositories view.\nOnly Crucible 2.1 or newer supports current operation.");
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

			crucible21Repos = getCrucible21Repos(page);

			// we need our own label provider to mark not matching repos as not clickable instead of hide them
			getViewer().setLabelProvider(
					new DelegatingStyledCellLabelProvider(new LocalRepositoryLabelProvider(crucible21Repos)));

			getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() instanceof TaskRepository) {
						selectedRepository = (TaskRepository) selection.getFirstElement();
						if (crucible21Repos.contains(selectedRepository)) {
							setPageComplete(true);
						} else {
							setPageComplete(false);
						}
					} else {
						setPageComplete(false);
					}
				}
			});

			getViewer().setInput(getCrucibeTaskRepositories());
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return crucible21Repos.contains(selectedRepository) && getSelectedNode() != null && getNextPage() != null;
	}

	public boolean canFinish() {
		return crucible21Repos.contains(selectedRepository) && getSelectedNode() != null && getNextPage() == null;
	}

	private Collection<TaskRepository> getCrucible21Repos(final WizardPage page) {
		final Collection<TaskRepository> crucible21Repos = new ArrayList<TaskRepository>();

		if (crucibleRepositoryWizard != null) {
			for (final TaskRepository repo : getCrucibeTaskRepositories()) {
				if (repo.getProperty(IS_VERSION_2_1) == null) {
					crucibleRepositoryWizard.updateRepoVersion(page, repo);
				}
			}

			for (final TaskRepository repo : getCrucibeTaskRepositories()) {
				if (Boolean.valueOf(repo.getProperty(IS_VERSION_2_1))) {
					crucible21Repos.add(repo);
				}
			}
		}
		return crucible21Repos;
	}

	private List<TaskRepository> getCrucibeTaskRepositories() {
		return getTaskRepositories(ENABLED_CRUCIBLE_REPOSITORY_FILTER);
	}

	private List<TaskRepository> getTaskRepositories(ITaskRepositoryFilter filter) {
		List<TaskRepository> repositories = new ArrayList<TaskRepository>();
		TaskRepositoryManager repositoryManager = TasksUiPlugin.getRepositoryManager();
		for (AbstractRepositoryConnector connector : repositoryManager.getRepositoryConnectors()) {
			Set<TaskRepository> connectorRepositories = repositoryManager.getRepositories(connector.getConnectorKind());
			for (TaskRepository repository : connectorRepositories) {
				if (filter.accept(repository, connector)) {
					repositories.add(repository);
				}
			}
		}
		return repositories;
	}

	public void setWizard(CrucibleRepositorySelectionWizard wizard) {
		this.crucibleRepositoryWizard = wizard;
		super.setWizard(wizard);
	}

	public final class LocalRepositoryLabelProvider extends TaskRepositoryLabelProvider implements IStyledLabelProvider {

		private final Collection<TaskRepository> crucible21Repos;

		public LocalRepositoryLabelProvider(Collection<TaskRepository> crucible21Repos) {
			this.crucible21Repos = crucible21Repos;
			JFaceResources.getColorRegistry().put("colorGrey", new RGB(100, 100, 100));
		}

		public StyledString getStyledText(Object element) {

			StyledString styledString = new StyledString();

			if (element instanceof TaskRepository) {
				TaskRepository repository = (TaskRepository) element;

				if (crucible21Repos.contains(repository)) {
					styledString.append(super.getText(element));
				} else {
					styledString.append(super.getText(element), StyledString.createColorRegistryStyler("colorGrey",
							null));
					styledString.append(" (this repository version is below 2.1)", StyledString.DECORATIONS_STYLER);
				}

			} else if (element instanceof AbstractRepositoryConnector) {
				styledString.append(((AbstractRepositoryConnector) element).getLabel());
			}

			return styledString;
		}

	}
}
