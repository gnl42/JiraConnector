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
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionTree.ITreeViewModeSettingProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionTree.TreeViewMode;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceSelectionPage extends AbstractCrucibleWizardPage {

	public class ResourceStatus {

		private final String state;

		private final boolean upToDate;

		public ResourceStatus(boolean upToDate, String state) {
			this.upToDate = upToDate;
			this.state = state;
		}

		public String getState() {
			return state;
		}

		public boolean isUpToDate() {
			return upToDate;
		}
	}

	private final List<IResource> roots = new ArrayList<IResource>();

	private final Map<IResource, ResourceStatus> resourcesToShow = new HashMap<IResource, ResourceStatus>();

	private final Collection<String> scmPaths = new ArrayList<String>();

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

	public Map<IResource, ResourceStatus> getSelection() {

		IResource[] selectedResources = resourceSelectionTree.getSelectedResources();

		Map<IResource, ResourceStatus> ret = new HashMap<IResource, ResourceStatus>();

		for (IResource resource : selectedResources) {
			ret.put(resource, resourcesToShow.get(resource));
		}

		return ret;
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

		resourceSelectionTree = new ResourceSelectionTree(composite, "", resourcesToShow, null,
				new ITreeViewModeSettingProvider() {

					public void setTreeViewMode(TreeViewMode mode) {
						CrucibleUiPlugin.getDefault().setResourcesTreeViewMode(mode);
					}

					public TreeViewMode getTreeViewMode() {
						return CrucibleUiPlugin.getDefault().getResourcesTreeViewMode();
					}
				});

		resourceSelectionTree.getTreeViewer().addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validatePage();
			}
		});

		GridDataFactory.fillDefaults()
				.span(2, 1)
				.hint(SWT.DEFAULT, 220)
				.grab(true, true)
				.applyTo(resourceSelectionTree);

		mappingButtonFactory = new DefineRepositoryMappingButton(this, composite, taskRepository);
		Control buttonControl = mappingButtonFactory.getControl();
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(buttonControl);

		populateResourcesTree();

		validatePage();
	}

	private void populateResourcesTree() {

		resourcesToShow.clear();

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
								resourcesToShow.put(resource, new ResourceStatus(false, "pre-commit"));
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_IGNORED)) {
								resourcesToShow.put(resource, new ResourceStatus(false, "pre-commit"));
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_ANY_CHANGE)) {
								resourcesToShow.put(resource, new ResourceStatus(false, "pre-commit"));
							} else if (teamConnector.isResourceAcceptedByFilter(resource,
									ITeamUiResourceConnector.State.SF_VERSIONED)) {
								resourcesToShow.put(resource, new ResourceStatus(true, "post-commit"));
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

		resourceSelectionTree.setResources(resourcesToShow);
		resourceSelectionTree.refresh();
		resourceSelectionTree.setAllChecked(true);

		validatePage();

	}

	public void validatePage() {
		setErrorMessage(null);

		String errorMessage = null;

		// check selection
		if (resourceSelectionTree.getSelectedResources().length == 0) {
			errorMessage = "Nothing is selected.";
		}

		// check repository mapping for committed root resources
		for (String scmPath : scmPaths) {
			Map.Entry<String, String> sourceRepository = null;
			sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
					TaskRepositoryUtil.getScmRepositoryMappings(taskRepository), scmPath);

			if (sourceRepository == null || sourceRepository.getValue() == null
					|| sourceRepository.getValue().length() == 0) {
				errorMessage = "SCM Repository Mapping is not defined.";
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
