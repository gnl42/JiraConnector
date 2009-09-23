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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.AbstractTeamConnector;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
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

	private final Set<ITeamResourceConnector> teamConnectors;

	private ITeamResourceConnector selectedTeamConnector;

	private ComboViewer scmViewer;

	public WorkspacePatchSelectionPage(@NotNull TaskRepository taskRepository, @NotNull ReviewWizard wizard,
			@NotNull List<IResource> roots) {
		super("Add Workspace Changes to Review");
		setTitle("Add Workspace Changes to Review");
		setDescription("Attach local, uncommited changes from the workspace to the review.");

		this.roots.addAll(roots);
		this.teamConnectors = AtlassianUiPlugin.getDefault().getTeamResourceManager().getTeamConnectors();
		if (this.selectedTeamConnector == null && roots.size() > 0) {
			final ITeamResourceConnector teamConnector = AtlassianUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(roots.get(0));
			if (teamConnector != null) {
				this.selectedTeamConnector = teamConnector;
			}
		}
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
		scmViewer.setContentProvider(new ArrayContentProvider());
		scmViewer.setSorter(new ViewerSorter());
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
				if (selectedTeamConnector == null) {
					return;
				}

				CrucibleUiPlugin.getDefault().setPreferredTeamResourceConnectorName(selectedTeamConnector.getName());

				final List<IResource> modifiedResources = MiscUtil.buildArrayList();

				IRunnableWithProgress getModifiedResources = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Getting workspace changes", roots.size());
						try {
							for (IResource root : roots) {
								modifiedResources.addAll(selectedTeamConnector.getResourcesByFilterRecursive(
										new IResource[] { root }, ITeamResourceConnector.State.SF_ANY_CHANGE));
								monitor.worked(1);
							}
						} finally {
							monitor.done();
						}
					}
				};

				try {
					getContainer().run(false, false, getModifiedResources);
				} catch (InvocationTargetException e) {
					StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
							"Can't get list of modified resources", e));
				} catch (InterruptedException e) {
					StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
							"Can't get list of modified resources", e));
				}

				changeViewer.setInput(modifiedResources.toArray(new IResource[modifiedResources.size()]));

				changeViewer.expandAll();
				changeViewer.setAllChecked(true);
				realSelection = initialSelection = changeViewer.getCheckedElements();

				validatePage();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText("Include changes:");

		changeViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(2, 1).hint(SWT.DEFAULT, 220).grab(true, true).applyTo(
				changeViewer.getControl());
		changeViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IResource && ((IResource) element).getType() == IResource.FILE;
			}
		});
		changeViewer.setContentProvider(new WorkbenchContentProvider() {
			public Object getParent(Object element) {
				return ((IResource) element).getParent();
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IResource[]) {
					return (Object[]) parentElement;
				}
				return new Object[0];
			}
		});
		changeViewer.setLabelProvider(new WorkbenchLabelProvider() {
			@Override
			protected String decorateText(String input, Object element) {
				if (element instanceof IResource) {
					return AbstractTeamConnector.getResourcePathWithProjectName((IResource) element);
				}
				return super.decorateText(input, element);
			}
		});
		changeViewer.addCheckStateListener(new ICheckStateListener() {
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
		changeViewer.setUseHashlookup(true);

		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(changeViewer.getTree());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTreeMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		changeViewer.getTree().setMenu(menu);

		// update selection after all wiring has been done
		scmViewer.setInput(teamConnectors);
		scmViewer.setSelection(new StructuredSelection(selectedTeamConnector != null ? selectedTeamConnector
				: scmViewer.getElementAt(0)));
	}

	void setAllChecked(boolean state) {
		changeViewer.setAllChecked(state);
	}

	protected void fillTreeMenu(IMenuManager menuMgr) {
		Action selectAllAction = new Action("Select all") {
			public void run() {
				setAllChecked(true);
			}
		};
		menuMgr.add(selectAllAction);
		Action deselectAllAction = new Action("Deselect all") {
			public void run() {
				setAllChecked(false);
			}
		};
		menuMgr.add(deselectAllAction);
	}

	private void validatePage() {
		setErrorMessage(null);

		boolean allFine = true;
		String errorMessage = null;

		if (getSelectedTeamResourceConnector() == null) {
			errorMessage = "Please select SCM provider";
			allFine = false;
		}

		if (!allFine) {
			setPageComplete(false);
			if (errorMessage != null) {
				setErrorMessage(errorMessage);
			}
		} else {
			setPageComplete(true);
		}

		if (getContainer().getCurrentPage() != null) {
			getContainer().updateButtons();
		}
	}

	public ITeamResourceConnector getSelectedTeamResourceConnector() {
		return selectedTeamConnector;
	}

}