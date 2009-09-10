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
/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesLabelProvider;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.IGenerateDiffOperation;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Page to select a patch file. Overriding validatePage was necessary to allow entering a file name that already exists.
 */
public class WorkspacePatchSelectionPage extends WizardPage {
	private CheckboxTreeViewer changeViewer;

	private Object[] initialSelection;

	private final List<IResource> roots = new ArrayList<IResource>();

	private Object[] realSelection;

	private String selectedRepository;

	private Set<Repository> cachedRepositories;

	private final Set<ITeamResourceConnector> teamConnectors;

	private ITeamResourceConnector selectedTeamConnector;

	private ComboViewer repositoryViewer;

	private final ReviewWizard wizard;

	private final TaskRepository taskRepository;

	private ComboViewer scmViewer;

	public WorkspacePatchSelectionPage(@NotNull TaskRepository taskRepository, @NotNull ReviewWizard wizard,
			@NotNull List<IResource> roots) {
		super("Add Workspace Patch to Review");
		setTitle("Add Workspace Patch to Review");
		setDescription("Attach a patch from the workspace to the review.");

		this.taskRepository = taskRepository;
		this.wizard = wizard;
		this.roots.addAll(roots);
		this.teamConnectors = AtlassianUiPlugin.getDefault().getTeamResourceManager().getTeamConnectors();
	}

	public IResource[] getSelection() {
		return Arrays.asList(this.realSelection).toArray(new IResource[this.realSelection.length]);
	}

	/**
	 * Allow the user to chose to save the patch to the workspace or outside of the workspace.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(5, 5).create());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Dialog.applyDialogFont(composite);
		initializeDialogUnits(composite);
		setControl(composite);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select SCM provider:");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		scmViewer = new ComboViewer(composite);
		scmViewer.getCombo().setText("Select SCM provider");
		scmViewer.setContentProvider(ArrayContentProvider.getInstance());
		scmViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ITeamResourceConnector) {
					return ((ITeamResourceConnector) element).getName();
				}
				return super.getText(element);
			}
		});
		scmViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) scmViewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}

				selectedTeamConnector = (ITeamResourceConnector) selection.getFirstElement();

				changeViewer.resetFilters();
				changeViewer.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						if (element instanceof IResource && selectedTeamConnector != null) {
							IResource resource = (IResource) element;
							IPath resourcePath = resource.getFullPath();
							for (IResource root : WorkspacePatchSelectionPage.this.roots) {
								IPath rootPath = root.getFullPath();
								if (rootPath.isPrefixOf(resourcePath) || resourcePath.isPrefixOf(rootPath)) {
									if (selectedTeamConnector.checkForResourcesPresenceRecursive(
											new IResource[] { resource }, ITeamResourceConnector.State.SF_ANY_CHANGE)) {
										return true;
									}
								}

							}
						}
						return false;
					}
				});

				changeViewer.expandAll();
				changeViewer.setAllChecked(true);
				realSelection = initialSelection = changeViewer.getCheckedElements();

				validatePage();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText("Include changes:");

		this.changeViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).hint(SWT.DEFAULT, 220).grab(true, true).applyTo(
				changeViewer.getControl());
		this.changeViewer.setContentProvider(new WorkbenchContentProvider() {
			/*public Object[] getChildren(Object element) {
				if (element instanceof IProject || element instanceof IFolder) {
					try {
						return ((IContainer) element).members();
					} catch (Exception e) {
						// do nothing
					}
				}
				return super.getChildren(element);
			}*/
		});
		this.changeViewer.setLabelProvider(new WorkbenchLabelProvider());
		this.changeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					IResource resource = (IResource) event.getElement();
					if (resource.getType() != IResource.FILE) {
						IPath path = resource.getFullPath();
						for (Object current : WorkspacePatchSelectionPage.this.initialSelection) {
							if (path.isPrefixOf(((IResource) current).getFullPath())) {
								WorkspacePatchSelectionPage.this.changeViewer.setChecked(current, true);
								WorkspacePatchSelectionPage.this.changeViewer.setGrayed(current, false);
							}
						}
					}
					while ((resource = resource.getParent()).getType() != IResource.ROOT) {
						boolean hasUnchecked = false;
						IPath path = resource.getFullPath();
						for (Object element : WorkspacePatchSelectionPage.this.initialSelection) {
							IResource current = (IResource) element;
							if (path.isPrefixOf(current.getFullPath()) && current != resource) {
								hasUnchecked |= !WorkspacePatchSelectionPage.this.changeViewer.getChecked(current);
							}
						}
						if (!hasUnchecked) {
							WorkspacePatchSelectionPage.this.changeViewer.setGrayed(resource, false);
							WorkspacePatchSelectionPage.this.changeViewer.setChecked(resource, true);
						}
					}
				} else {
					IResource resource = (IResource) event.getElement();
					if (resource.getType() != IResource.FILE) {
						IPath path = resource.getFullPath();
						for (Object element : WorkspacePatchSelectionPage.this.initialSelection) {
							IResource current = (IResource) element;
							if (path.isPrefixOf(current.getFullPath())) {
								WorkspacePatchSelectionPage.this.changeViewer.setChecked(current, false);
							}
						}
					}
					while ((resource = resource.getParent()).getType() != IResource.ROOT) {
						WorkspacePatchSelectionPage.this.changeViewer.setGrayed(resource, true);
					}
				}
				WorkspacePatchSelectionPage.this.realSelection = WorkspacePatchSelectionPage.this.changeViewer.getCheckedElements();
			}
		});
		this.changeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

		label = new Label(composite, SWT.NONE);
		label.setText("Select the repository on Crucible:");
		GridDataFactory.fillDefaults().grab(false, false).applyTo(label);
		repositoryViewer = new ComboViewer(composite);
		repositoryViewer.getCombo().setText("Select Repository");
		repositoryViewer.setContentProvider(ArrayContentProvider.getInstance());
		repositoryViewer.setSorter(new ViewerSorter());
		repositoryViewer.setLabelProvider(new CrucibleRepositoriesLabelProvider());
		repositoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (repositoryViewer.getSelection() instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) repositoryViewer.getSelection()).getFirstElement();
					if (cachedRepositories.contains(selected)) {
						selectedRepository = ((Repository) selected).getName();
					}
				}
				validatePage();
			}
		});
		GridDataFactory.fillDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING)
				.grab(true, false)
				.hint(100, SWT.DEFAULT)
				.applyTo(repositoryViewer.getCombo());

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wizard.updateCache(WorkspacePatchSelectionPage.this);
			}
		});

		// update selection after all wiring has been done
		scmViewer.setInput(teamConnectors);
		scmViewer.setSelection(new StructuredSelection(scmViewer.getElementAt(0)));
	}

	private void validatePage() {
		setErrorMessage(null);

		boolean allFine = true;
		String errorMessage = null;

		/*FIXME: if (patchText.getText().length() < 1) {
			errorMessage = "In order to create a review from a patch,"
					+ " copy the patch to the clipboard before opening this Wizard.";
			allFine = false;
		} else if (selectedRepository == null) {
			errorMessage = "Choose a repository on Crucible this patch relates to.";
			allFine = false;
		}
		if (includePatch) {
			setPageComplete(allFine);
			if (errorMessage != null) {
				setErrorMessage(errorMessage);
			}
		} else {
			setPageComplete(true);
		}*/

		getContainer().updateButtons();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
						wizard.updateCache(WorkspacePatchSelectionPage.this);
					}
					if (cachedRepositories == null) {
						cachedRepositories = CrucibleUiUtil.getCachedRepositories(taskRepository);
					}
					repositoryViewer.setInput(cachedRepositories);
					validatePage();
				}
			});
		} else {
			setErrorMessage(null);
			setPageComplete(true);
			getContainer().updateButtons();
		}
	}

	public String getSelectedRepository() {
		return selectedRepository;
	}

	public boolean hasPatch() {
		return realSelection != null && realSelection.length > 0;
	}

	public String getPatch() {
		try {
			IGenerateDiffOperation runnable = selectedTeamConnector.getGenerateDiffOperationInstance(getSelection(),
					true, false, true);
			getContainer().run(false, false, runnable);

			if (runnable.getStatus().isOK()) {
				return runnable.getPatch();
			}
		} catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		} catch (InvocationTargetException e) {
			// FIXME: ignoring exception
		} catch (InterruptedException e) {
			// FIXME: ignoring exception
		}
		return "";
	}
}