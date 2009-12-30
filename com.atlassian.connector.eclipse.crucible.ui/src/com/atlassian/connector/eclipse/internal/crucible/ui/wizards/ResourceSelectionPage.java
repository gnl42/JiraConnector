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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;

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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ResourceSelectionPage extends AbstractCrucibleWizardPage {

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

	private final List<IResource> resourcesToShow = new ArrayList<IResource>();

	private final Collection<String> scmPaths = new ArrayList<String>();

//	private Object[] realSelection;

	private ITeamUiResourceConnector teamConnector;

	private final TaskRepository taskRepository;

	private DefineRepositoryMappingButton mappingButtonFactory;

	private ResourceSelectionTree resourceSelectionTree;

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

		resourceSelectionTree = new ResourceSelectionTree(composite, "", resourcesToShow, null, null);
		resourceSelectionTree.setEnabled(false);

		GridDataFactory.fillDefaults()
				.span(2, 1)
				.hint(SWT.DEFAULT, 220)
				.grab(true, true)
				.applyTo(resourceSelectionTree);

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

		mappingButtonFactory = new DefineRepositoryMappingButton(this, composite, taskRepository);
		Control buttonControl = mappingButtonFactory.getControl();
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(buttonControl);

		populateResourcesTree();

		validatePage();
	}

	private void populateResourcesTree() {

		final Collection<ResourceEntry> resourceEntries = new ArrayList<ResourceEntry>();
		final Collection<IResource> validResources = new ArrayList<IResource>();

		IRunnableWithProgress getModifiedResources = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Getting workspace resources data", IProgressMonitor.UNKNOWN);

				final Collection<IResource> resources = new ArrayList<IResource>();

				try {

					resources.addAll(teamConnector.getResourcesByFilterRecursive(
							roots.toArray(new IResource[roots.size()]), ITeamUiResourceConnector.State.SF_ALL));

					for (IResource resource : resources) {
						if (resource instanceof IFile) {

							// collect all scmPaths in order to find missing mappings
							if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_VERSIONED)
									&& !teamConnector.isResourceAcceptedByFilter(resource,
											ITeamUiResourceConnector.State.SF_ANY_CHANGE)) {
								try {
									LocalStatus status = teamConnector.getLocalRevision(resource);
									if (status.getScmPath() != null && status.getScmPath().length() > 0) {
										scmPaths.add(teamConnector.getLocalRevision(resource.getProject()).getScmPath());
									}
								} catch (CoreException e) {
									// resource is probably not under version control
									// skip
								}
							}

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

		resourcesToShow.clear();
		resourcesToShow.addAll(validResources);
		resourceSelectionTree.setResources(resourcesToShow);
		resourceSelectionTree.refresh();

		changeViewer.expandAll();
		setAllChecked(true);
		validatePage();
//		realSelection = getResourcesTreeSelection().toArray();

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

	public void validatePage() {
		setErrorMessage(null);

		String errorMessage = null;

		// check selection
		if (changeViewer.getCheckedElements().length == 0) {
			errorMessage = "Nothing selected.";
		}

		// check repository mapping for committed root resources
		for (String scmPath : scmPaths) {
			Map.Entry<String, String> sourceRepository = null;
			sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
					TaskRepositoryUtil.getScmRepositoryMappings(taskRepository), scmPath);

			if (sourceRepository == null || sourceRepository.getValue() == null
					|| sourceRepository.getValue().length() == 0) {
				errorMessage = "SCM Repository Mapping is not defined";
				// TODO PLE-841 (do not suggest mapping to user)
//				mappingButtonFactory.setMissingMapping(scmPath);
			}
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
