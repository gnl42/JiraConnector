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

import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesLabelProvider;
import com.atlassian.connector.eclipse.ui.dialogs.ComboSelectionDialog;
import com.atlassian.connector.eclipse.ui.team.ScmRepository;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class CrucibleRepositoryMappingPage extends WizardPage {

	private final Map<String, String> repositoryMappings;

	private final TaskRepository taskRepository;

	private TableViewer repositoriesMappingViewer;

	private Set<Repository> cachedRepositories;

	protected CrucibleRepositoryMappingPage(String pageName, TaskRepository repository) {
		super(pageName);

		this.repositoryMappings = TaskRepositoryUtil.getScmRepositoryMappings(repository);
		this.taskRepository = repository;

	}

	public Composite createRepositoryMappingComposite(Composite composite, int hSizeHint) {
		final Composite mappingComposite = new Composite(composite, SWT.NONE);

		final Table table = new Table(mappingComposite, SWT.BORDER);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().hint(hSizeHint, 100).grab(true, true).applyTo(table);
		repositoriesMappingViewer = new TableViewer(table);
		repositoriesMappingViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection<?>) {
					return ((Collection<?>) inputElement).toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		final TableViewerColumn column1 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column1.getColumn().setText("SCM Path");
		column1.getColumn().setWidth(500);
		column1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ScmRepository) {
					return ((ScmRepository) element).getScmPath();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ScmRepository) {
					return CommonImages.getImage(CrucibleImages.REPOSITORY);
				}
				return null;
			}
		});

		final TableViewerColumn column2 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column2.getColumn().setText("Crucible Repository");
		column2.getColumn().setWidth(200);
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ScmRepository) {
					String mapping = repositoryMappings.get(((ScmRepository) element).getScmPath());
					if (mapping != null) {
						return mapping;
					}
				}
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});

		final Button editButton = new Button(mappingComposite, SWT.PUSH);
		editButton.setText("Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editRepositoryMapping();
			}
		});
		editButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(editButton);

		table.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				editRepositoryMapping();
			}

			public void widgetSelected(SelectionEvent e) {
				ISelection selection = repositoriesMappingViewer.getSelection();
				if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
					editButton.setEnabled(true);
				} else {
					editButton.setEnabled(false);
				}
			}

		});

		return mappingComposite;
	}

	private void editRepositoryMapping() {
		ISelection selection = repositoriesMappingViewer.getSelection();
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
			String scmPath = ((ScmRepository) ((IStructuredSelection) selection).getFirstElement()).getScmPath();
			if (cachedRepositories == null) {
				cachedRepositories = CrucibleUiUtil.getCachedRepositories(taskRepository);
			}

			Repository preselectRepository = null;
			String repositoryName = repositoryMappings.get(scmPath);
			if (repositoryName != null) {
				for (Repository repo : cachedRepositories) {
					if (repositoryName.equals(repo.getName())) {
						preselectRepository = repo;
						break;
					}
				}
			}

			ComboSelectionDialog<Repository> dialog = new ComboSelectionDialog<Repository>(
					repositoriesMappingViewer.getTable().getShell(), "Map Local to Crucible Repository", String.format(
							"Map \"%s\" to: ", scmPath), new CrucibleRepositoriesLabelProvider(), cachedRepositories,
					preselectRepository);
			int returnCode = dialog.open();
			if (returnCode == IDialogConstants.OK_ID) {
				Repository crucibleRepository = dialog.getSelection();
				repositoryMappings.put(scmPath, crucibleRepository.getName());
				repositoriesMappingViewer.setInput(getMappingViewerInput());

				final Map<String, String> repositoryMappingsCopy = new HashMap<String, String>(
						repositoryMappings.size());
				for (Map.Entry<String, String> entry : repositoryMappings.entrySet()) {
					if (entry.getValue() != null) {
						repositoryMappingsCopy.put(entry.getKey(), entry.getValue());
					}
				}
				TaskRepositoryUtil.setScmRepositoryMappings(taskRepository, repositoryMappingsCopy);
			}
		}
		validatePage();
	}

	protected Control createUpdateRepositoryDataButton(Composite composite) {
		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		final WizardPage mainPage = this;
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), mainPage);
				clearCachedCrucibleRepositories();
			}
		});

		return updateData;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible && !CrucibleUiUtil.hasCachedData(getTaskRepository())) {
			final WizardPage page = this;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (!CrucibleUiUtil.hasCachedData(getTaskRepository())) {
						CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), page);
					}
				}
			});
		}
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public Map<String, String> getRepositoryMappings() {
		return repositoryMappings;
	}

	public TableViewer getRepositoriesMappingViewer() {
		return repositoriesMappingViewer;
	}

	protected void clearCachedCrucibleRepositories() {
		cachedRepositories = null;
	}

	protected abstract void validatePage();

	protected abstract Collection<ScmRepository> getMappingViewerInput();

}
