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

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

/**
 * Page to select a patch file. Overriding validatePage was necessary to allow entering a file name that already exists.
 */
public class WorkspacePatchSelectionPage extends WizardPage {
	private CheckboxTreeViewer resourceSelectionTree;

	private CheckboxTreeViewer changeViewer;

	private Object[] initialSelection;

	private final IResource[] roots;

	private Object[] realSelection;

	private final TaskRepository taskRepository;

	private final ReviewWizard wizard;

	private final Set<ITeamResourceConnector> teamConnectors;

	public WorkspacePatchSelectionPage(@NotNull TaskRepository taskRepository, @NotNull ReviewWizard wizard,
			@Nullable IResource[] roots) {
		super("Add Workspace Patch to Review");
		setTitle("Add Workspace Patch to Review");
		setDescription("Attach a patch from the workspace to the review.");

		this.taskRepository = taskRepository;
		this.wizard = wizard;
		this.roots = roots;
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
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		setControl(composite);
		initializeDialogUnits(composite);

		if (this.roots != null) {
			this.changeViewer = new CheckboxTreeViewer(composite, SWT.BORDER);
			this.changeViewer.setContentProvider(new WorkbenchContentProvider() {
				public Object[] getChildren(Object element) {
					if (element instanceof IProject || element instanceof IFolder) {
						try {
							//return SVNRemoteStorage.instance().getRegisteredChildren((IContainer) element);
						} catch (Exception e) {
							// do nothing
						}
					}
					return super.getChildren(element);
				}
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
			this.changeViewer.addFilter(new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {

					if (element instanceof IResource) {
						IResource resource = (IResource) element;
						IPath resourcePath = resource.getFullPath();
						for (IResource root : WorkspacePatchSelectionPage.this.roots) {
							IPath rootPath = root.getFullPath();
							if (rootPath.isPrefixOf(resourcePath) || resourcePath.isPrefixOf(rootPath)) {
								for (ITeamResourceConnector connector : WorkspacePatchSelectionPage.this.teamConnectors) {
									if (connector.checkForResourcesPresenceRecursive(new IResource[] { resource },
											ITeamResourceConnector.State.SF_ANY_CHANGE)) {
										return true;
									}
								}
							}

						}
					}
					return false;
				}
			});
			this.changeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
			this.changeViewer.expandAll();
			this.changeViewer.setAllChecked(true);
			this.realSelection = this.initialSelection = this.changeViewer.getCheckedElements();
		}
	}
}