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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.ITaskRepositoryFilter;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public abstract class SelectCrucibleVersionOrNewerRepositoryPage extends SelectCrucibleRepositoryPage {

	private CrucibleRepositorySelectionWizard crucibleRepositoryWizard;

	protected TaskRepository selectedRepository;

	private final CrucibleVersionInfo version;

	public SelectCrucibleVersionOrNewerRepositoryPage() {
		this(new CrucibleVersionInfo("2.1", null));
	}

	public SelectCrucibleVersionOrNewerRepositoryPage(CrucibleVersionInfo version) {
		super(ENABLED_CRUCIBLE_REPOSITORY_FILTER);
		setDescription(NLS.bind(
				"Add new repositories using the Task Repositories view.\nOnly Crucible {0} or newer supports current operation.",
				version.toString()));
		this.version = version;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		// we need our own label provider to mark not matching repos as not clickable instead of hide them
		getViewer().setLabelProvider(new DelegatingStyledCellLabelProvider(new LocalRepositoryLabelProvider()));

		getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof TaskRepository) {
					selectedRepository = (TaskRepository) selection.getFirstElement();
					if (isSelectedRepoInVersionOrNewer()) {
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}
				} else {
					setPageComplete(false);
				}
			}
		});
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
			getViewer().setInput(getCrucibeTaskRepositories());
			// using async exec here, as otherwise run() would be run _before_ the dialog is actually shown
			// so the user would not see anything before the long running operation is completed
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (getControl() != null && !getControl().isDisposed()) {
						// retrieve Crucible repos version and filter list of available repos in the wizard
						fillCrucibleVersionInfo(SelectCrucibleVersionOrNewerRepositoryPage.this);
						getWizard().getContainer().updateButtons();
					}
				}
			});
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return isSelectedRepoInVersionOrNewer() && getSelectedNode() != null && getNextPage() != null;
	}

	private boolean isSelectedRepoInVersionOrNewer() {
		if (selectedRepository == null) {
			return false;
		}
		final CrucibleVersionInfo crucibleVersionInfo = CrucibleUiUtil.getCrucibleVersionInfo(selectedRepository);
		return crucibleVersionInfo != null && crucibleVersionInfo.compareTo(version) >= 0;
	}

	public boolean canFinish() {
		return isSelectedRepoInVersionOrNewer() && getSelectedNode() != null && getNextPage() == null;
	}

	private void fillCrucibleVersionInfo(final WizardPage page) {

		if (crucibleRepositoryWizard != null) {
			for (final TaskRepository repo : getCrucibeTaskRepositories()) {
				final CrucibleVersionInfo versionInfo = CrucibleUiUtil.getCrucibleVersionInfo(repo);
				if (versionInfo == null) {
					crucibleRepositoryWizard.updateRepoVersion(page, repo);
				}
				getViewer().refresh();
			}
		}
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

		private final Styler GRAY_COLOR_STYLER = new Styler() {

			private static final String COLOR_GREY = "acfeColorGrey";
			{
				JFaceResources.getColorRegistry().put(COLOR_GREY, new RGB(100, 100, 100));
			}

			public void applyStyles(org.eclipse.swt.graphics.TextStyle textStyle) {
				textStyle.foreground = JFaceResources.getColorRegistry().get(COLOR_GREY);

			};
		};

		public StyledString getStyledText(Object element) {

			StyledString styledString = new StyledString();

			if (element instanceof TaskRepository) {
				TaskRepository repository = (TaskRepository) element;
				final CrucibleVersionInfo crucibleVersionInfo = CrucibleUiUtil.getCrucibleVersionInfo(repository);
				if (crucibleVersionInfo == null) {
					styledString.append(super.getText(element), GRAY_COLOR_STYLER);
					styledString.append(" (unknown Crucible version)", StyledString.DECORATIONS_STYLER);
				} else {
					if (crucibleVersionInfo.compareTo(version) >= 0) {
						styledString.append(super.getText(element));
					} else {
						styledString.append(super.getText(element), GRAY_COLOR_STYLER);
						styledString.append(NLS.bind(" (this repository version is below {0})", version.toString()),
								StyledString.DECORATIONS_STYLER);
					}
				}

			} else if (element instanceof AbstractRepositoryConnector) {
				styledString.append(((AbstractRepositoryConnector) element).getLabel());
			}

			return styledString;
		}

	}
}
