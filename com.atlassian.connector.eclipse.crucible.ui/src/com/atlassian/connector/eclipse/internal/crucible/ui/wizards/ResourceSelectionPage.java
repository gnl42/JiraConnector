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

import com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferenceContextData;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepositoryMappingPreferencePage;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ResourceSelectionPage extends WizardPage {

	public class ResourceEntry {
		private final IResource resource;

		private final String state;

		private final boolean updated;

		public ResourceEntry(IResource resource, boolean updated, String state) {
			this.resource = resource;
			this.updated = updated;
			this.state = state;
		}

		public IResource getResource() {
			return resource;
		}

		public String getState() {
			return state;
		}

		public boolean isUpdated() {
			return updated;
		}
	}

	private CheckboxTreeViewer changeViewer;

//	private Object[] initialSelection;

	private final List<IResource> roots = new ArrayList<IResource>();

	private Object[] realSelection;

	private ITeamUiResourceConnector teamConnector;

	private final TaskRepository taskRepository;

	public ResourceSelectionPage(@NotNull TaskRepository taskRepository, @NotNull List<IResource> roots) {
		super("Add Resources to Review");
		this.taskRepository = taskRepository;
		setTitle("Add Resources to Review");
		setDescription("Add Resources to Review");

		this.roots.addAll(roots);

		if (roots.size() > 0) {
			// we support only single SCM integration selection in the wizard
			// other resources will be ignored
			final ITeamUiResourceConnector teamConnector = AtlassianTeamUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(roots.get(0));
			if (teamConnector != null) {
				this.teamConnector = teamConnector;
			}

			for (IResource resource : roots) {

			}
		}
	}

	public ResourceEntry[] getSelection() {

//		Collection<IResource> resources = new ArrayList<IResource>();
//
//		for (Object selected : changeViewer.getCheckedElements()) {
//			resources.add(((ResourceEntry) selected).getResource());
//		}
//
//		return resources.toArray(new IResource[changeViewer.getCheckedElements().length]);

		return Arrays.asList(changeViewer.getCheckedElements()).toArray(
				new ResourceEntry[changeViewer.getCheckedElements().length]);
	}

	/**
	 * Allow the user to chose to save the patch to the workspace or outside of the workspace.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).margins(5, 5).create());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Dialog.applyDialogFont(composite);
		initializeDialogUnits(composite);
		setControl(composite);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Include changes:");

		changeViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
		Tree tree = changeViewer.getTree();
		tree.setHeaderVisible(true);

		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setWidth(500);
		column1.setText("Resource");

		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setWidth(90);
		column2.setText("SCM State");

		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setWidth(90);
		column3.setText("Review Type");

		GridDataFactory.fillDefaults().span(2, 1).hint(SWT.DEFAULT, 220).grab(true, true).applyTo(
				changeViewer.getControl());

		changeViewer.setContentProvider(new WorkbenchContentProvider() {

			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		changeViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof ResourceEntry) {
					ResourceEntry resourceEntry = (ResourceEntry) element;
					if (columnIndex == 0) {
						return AbstractTeamUiConnector.getResourcePathWithProjectName(resourceEntry.getResource());
					} else if (columnIndex == 1) {
						return resourceEntry.getState();
					} else if (columnIndex == 2) {
						return (resourceEntry.isUpdated() ? "post-commit" : "pre-commit");
					}
				}

				return "";
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
		// TODO jj add support for tree view (with directories)
		changeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
//				if (event.getChecked()) {
//					ResourceEntry resourceEntry = (ResourceEntry) event.getElement();
//					IResource resource = resourceEntry.getResource();
//					if (resource.getType() != IResource.FILE) {
//						IPath path = resource.getFullPath();
//						for (Object current : ResourceSelectionPage.this.initialSelection) {
//							if (path.isPrefixOf(((IResource) current).getFullPath())) {
//								ResourceSelectionPage.this.changeViewer.setChecked(current, true);
//								ResourceSelectionPage.this.changeViewer.setGrayed(current, false);
//							}
//						}
//					}
//					while ((resource = resource.getParent()).getType() != IResource.ROOT) {
//						boolean hasUnchecked = false;
//						IPath path = resource.getFullPath();
//						for (Object element : ResourceSelectionPage.this.initialSelection) {
//							IResource current = (IResource) element;
//							if (path.isPrefixOf(current.getFullPath()) && current != resource) {
//								hasUnchecked |= !ResourceSelectionPage.this.changeViewer.getChecked(current);
//							}
//						}
//						if (!hasUnchecked) {
//							ResourceSelectionPage.this.changeViewer.setGrayed(resource, false);
//							ResourceSelectionPage.this.changeViewer.setChecked(resource, true);
//						}
//					}
//				} else {
//					IResource resource = ((ResourceEntry) event.getElement()).getResource();
//					if (resource.getType() != IResource.FILE) {
//						IPath path = resource.getFullPath();
//						for (Object element : ResourceSelectionPage.this.initialSelection) {
//							IResource current = (IResource) element;
//							if (path.isPrefixOf(current.getFullPath())) {
//								ResourceSelectionPage.this.changeViewer.setChecked(current, false);
//							}
//						}
//					}
//					while ((resource = resource.getParent()).getType() != IResource.ROOT) {
//						ResourceSelectionPage.this.changeViewer.setGrayed(resource, true);
//					}
//				}

//				ResourceSelectionPage.this.realSelection = changeViewer.getCheckedElements();

				validatePage();
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

		Control button = createDefineRepositoryMappingsButton(composite);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);

		populateResourcesTree();

		validatePage();

	}

	private void populateResourcesTree() {

		final Collection<ResourceEntry> resourceEntries = new ArrayList<ResourceEntry>();

		IRunnableWithProgress getModifiedResources = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Getting workspace resources data", roots.size());

				final Collection<IResource> resources = new ArrayList<IResource>();
				final Collection<IResource> validResources = new ArrayList<IResource>();

				try {
					for (IResource root : roots) {
						resources.addAll(teamConnector.getResourcesByFilterRecursive(new IResource[] { root },
								ITeamUiResourceConnector.State.SF_ALL));
						monitor.worked(1);
					}

					for (IResource resource : resources) {
						if (resource instanceof IFile) {
							if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_UNVERSIONED)) {
								resourceEntries.add(new ResourceEntry(resource, false, "unversioned"));
								validResources.add(resource);
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_IGNORED)) {
								resourceEntries.add(new ResourceEntry(resource, false, "ignored"));
								validResources.add(resource);
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_ANY_CHANGE)) {
								resourceEntries.add(new ResourceEntry(resource, false, "other change"));
								validResources.add(resource);
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_VERSIONED)) {
								resourceEntries.add(new ResourceEntry(resource, true, "committed"));
								validResources.add(resource);
							} else {
								// ignore the resource
							}
						}
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

		changeViewer.setInput(resourceEntries.toArray(new ResourceEntry[resourceEntries.size()]));

		changeViewer.expandAll();
		setAllChecked(true);
		validatePage();
//		realSelection = getResourcesTreeSelection().toArray();

	}

	// TODO jj code duplication with CrucibleAddChagesetsPage
	protected Control createDefineRepositoryMappingsButton(Composite composite) {
		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Define Repository Mappings");
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(getShell(),
						SourceRepositoryMappingPreferencePage.ID, null, new FishEyePreferenceContextData("",
								taskRepository));
				if (prefDialog != null) {
					if (prefDialog.open() == Window.OK) {
						validatePage();
					}
				}
			}
		});
		return updateData;
	}

	private void setAllChecked(boolean newState) {
		for (Object element : (Object[]) changeViewer.getInput()) {
			changeViewer.setSubtreeChecked(element, newState);
		}

	}

	protected void fillTreeMenu(IMenuManager menuMgr) {
		Action selectAllAction = new Action("Select all") {
			public void run() {
				setAllChecked(true);
				validatePage();
			}
		};
		menuMgr.add(selectAllAction);
		Action deselectAllAction = new Action("Deselect all") {
			public void run() {
				setAllChecked(false);
				validatePage();
			}
		};
		menuMgr.add(deselectAllAction);
	}

	private void validatePage() {
		setErrorMessage(null);

		String errorMessage = null;

		// check selection
		if (changeViewer.getCheckedElements().length == 0) {
			errorMessage = "Nothing selected.";
		}

		// check repository mapping
		// TODO jj for pre-commit files we do not need mapping
		// find commited file and check mapping or skip if there is no commited file (commited file has revision)
		Map.Entry<String, String> sourceRepository = null;
		try {
			sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
					TaskRepositoryUtil.getScmRepositoryMappings(taskRepository), teamConnector.getLocalRevision(
							roots.get(0)).getScmPath());
		} catch (CoreException e) {
			errorMessage = "Cannot get local revision for " + roots.get(0).getName();
		}

		if (sourceRepository == null || sourceRepository.getValue() == null
				|| sourceRepository.getValue().length() == 0) {
			setPageComplete(false);
			setErrorMessage("SCM Repository Mapping is not defined");
		}

		// validate page
		if (errorMessage == null) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
			setErrorMessage(errorMessage);
		}

		if (getContainer().getCurrentPage() != null) {
			getContainer().updateButtons();
		}
	}

	public ITeamUiResourceConnector getTeamResourceConnector() {
		return teamConnector;
	}
}
