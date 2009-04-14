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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.wizards.TaskRepositoryWizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Page for selecting a crucible repository
 * 
 * @author Thomas Ehrnhoefer
 */
public class SelectCrucibleRepositoryPage extends WizardSelectionPage {

	private TableViewer viewer;

	private List<TaskRepository> repositories = new ArrayList<TaskRepository>();

	private final SortedSet<ICustomChangesetLogEntry> logEntries;

	class RepositoryContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return repositories.toArray();
		}
	}

	public SelectCrucibleRepositoryPage(SortedSet<ICustomChangesetLogEntry> logEntries) {
		super("Create");
		setTitle("Select a Crucible Repository");
		setDescription("Choose one of the available Crucible repositories to create your review on.");
		this.repositories = getTaskRepositories();
		this.logEntries = logEntries;
	}

	public List<TaskRepository> getTaskRepositories() {
		List<TaskRepository> repos = new ArrayList<TaskRepository>();
		TaskRepositoryManager repositoryManager = TasksUiPlugin.getRepositoryManager();
		Set<TaskRepository> connectorRepositories = repositoryManager.getRepositories(CrucibleCorePlugin.CONNECTOR_KIND);
		for (TaskRepository repository : connectorRepositories) {
			repos.add(repository);
		}
		return repos;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);

		Table table = createTableViewer(container);

		GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		table.setLayoutData(gridData);

		final Action addRepositoryAction = new Action() {
			@SuppressWarnings("restriction")
			@Override
			public void run() {
				NewRepositoryWizard repositoryWizard = new NewRepositoryWizard(CrucibleCorePlugin.CONNECTOR_KIND);

				WizardDialog repositoryDialog = new TaskRepositoryWizardDialog(null, repositoryWizard);
				repositoryDialog.create();
				repositoryDialog.getShell().setText("Add New Crucible Repository Repository...");
				repositoryDialog.setBlockOnOpen(true);
				repositoryDialog.open();
			}
		};
		addRepositoryAction.setText("Add New Crucible Repository...");

		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		button.setEnabled(addRepositoryAction.isEnabled());
		button.setText("Add Task Repository...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRepositoryAction.run();
				repositories = getTaskRepositories();
				viewer.setInput(TasksUi.getRepositoryManager().getRepositoryConnectors());
			}
		});

		Dialog.applyDialogFont(container);
		setControl(container);
	}

	protected Table createTableViewer(Composite container) {
		viewer = new TableViewer(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RepositoryContentProvider());
		// viewer.setLabelProvider(new TaskRepositoryLabelProvider());
		viewer.setLabelProvider(new DecoratingLabelProvider(new TaskRepositoryLabelProvider(),
				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		viewer.setInput(TasksUi.getRepositoryManager().getRepositoryConnectors());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof TaskRepository) {
					setSelectedNode(new CustomWizardNode((TaskRepository) selection.getFirstElement()));
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});

		TaskRepository selectedRepository = TasksUiUtil.getSelectedRepository(null);
		if (selectedRepository != null) {
			viewer.setSelection(new StructuredSelection(selectedRepository));
		} else {
			TaskRepository localRepository = TasksUi.getRepositoryManager().getRepository(
					LocalRepositoryConnector.CONNECTOR_KIND, LocalRepositoryConnector.REPOSITORY_URL);
			viewer.setSelection(new StructuredSelection(localRepository));
		}

		viewer.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				if (canFlipToNextPage()) {
					getContainer().showPage(getNextPage());
				} else if (canFinish()) {
					if (getWizard().performFinish()) {
						((WizardDialog) getContainer()).close();
					}
				}
			}
		});

		viewer.getTable().showSelection();
		viewer.getTable().setFocus();
		return viewer.getTable();
	}

	protected IWizard createWizard(TaskRepository taskRepository) {
		return new NewCrucibleReviewWizard(taskRepository, logEntries);
	}

	@Override
	public boolean canFlipToNextPage() {
		return getSelectedNode() != null && getNextPage() != null;
	}

	public boolean canFinish() {
		return getSelectedNode() != null && getNextPage() == null;
	}

	public boolean performFinish() {
		if (getSelectedNode() == null || getNextPage() != null) {
			// finish event will get forwarded to nested wizard
			// by container
			return false;
		}

		return getSelectedNode().getWizard().performFinish();
	}

	private class CustomWizardNode implements IWizardNode {

		private final TaskRepository repository;

		private IWizard wizard;

		public CustomWizardNode(TaskRepository repository) {
			this.repository = repository;
		}

		public void dispose() {
			if (wizard != null) {
				wizard.dispose();
			}
		}

		public Point getExtent() {
			return new Point(-1, -1);
		}

		public IWizard getWizard() {
			if (wizard == null) {
				wizard = createWizard(repository);
				if (wizard != null) {
					wizard.setContainer(getContainer());
				}
			}
			return wizard;
		}

		public boolean isContentCreated() {
			return wizard != null;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CustomWizardNode)) {
				return false;
			}
			CustomWizardNode that = (CustomWizardNode) obj;
			if (this == that) {
				return true;
			}

			return this.repository.getConnectorKind().equals(that.repository.getConnectorKind())
					&& this.repository.getRepositoryUrl().equals(that.repository.getRepositoryUrl());
		}

		@Override
		public int hashCode() {
			return 31 * this.repository.getRepositoryUrl().hashCode() + this.repository.getConnectorKind().hashCode();
		}

	}

	/**
	 * Public for testing.
	 */
	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * Public for testing.
	 */
	public List<TaskRepository> getRepositories() {
		return repositories;
	}

}
