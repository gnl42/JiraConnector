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

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepostioryMappingEditor;
import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.TeamUiResourceManager;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class DefineRepositoryMappingsPage extends WizardPage {

	private final TaskRepository taskRepository;

	private SourceRepostioryMappingEditor mappingEditor;

	private Set<IResource> resources;

	private Collection<String> scmPaths;

	public DefineRepositoryMappingsPage(TaskRepository repository, Collection<IResource> resources) {
		super("crucibleRepoMapping");

		this.taskRepository = repository;
		this.resources = MiscUtil.buildHashSet();
		this.resources.addAll(resources);

		setTitle("Define Repository Mapping");
		setDescription("Define repository mapping used to create review.");
	}

	public DefineRepositoryMappingsPage(Collection<String> scmPaths, TaskRepository repository) {
		super("crucibleRepoMapping");

		this.taskRepository = repository;
		this.scmPaths = MiscUtil.buildArrayList();
		this.scmPaths.addAll(scmPaths);

		setTitle("Define Repository Mapping");
		setDescription("Define repository mapping used to create review.");
	}

	public Composite createRepositoryMappingComposite(Composite ancestor, int hSizeHint) {
		final Composite parent = new Composite(ancestor, SWT.NONE);

		mappingEditor = new SourceRepostioryMappingEditor(parent, taskRepository);
		mappingEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				try {
					FishEyeUiUtil.setScmRepositoryMappings(mappingEditor.getMapping());
				} catch (IOException e) {
					ErrorDialog.openError(getShell(), AtlassianCorePlugin.PRODUCT_NAME,
							"Error while saving FishEye mapping configuration", new Status(IStatus.ERROR,
									CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
				}
				validatePage();
			}
		});
		mappingEditor.setRepositoryMappings(FishEyeUiUtil.getActiveScmRepositoryMappings());

		return parent;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		validatePage();

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

	protected void validatePage() {
		setErrorMessage(null);
		setMessage("All necessary mappings are defined.");

		//check if all custom repositories are mapped to Crucible repositories
		boolean allFine = true;

		if (scmPaths == null) {
			if (resources != null) {
				TeamUiResourceManager teamManager = AtlassianTeamUiPlugin.getDefault().getTeamResourceManager();

				for (IResource resource : resources) {
					ITeamUiResourceConnector teamConnector = teamManager.getTeamConnector(resource);

					final LocalStatus revision;
					try {
						revision = teamConnector.getLocalRevision(resource);
					} catch (CoreException e) {
						setErrorMessage(e.getMessage());
						return;
					}

					if (revision != null && revision.isVersioned()) {
						if (TaskRepositoryUtil.getMatchingSourceRepository(
								TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()), revision.getScmPath()) == null) {
							setErrorMessage(NLS.bind(
									"Unable to map SCM path {0} to Crucible repository. Please define a mapping.",
									revision.getScmPath()));
							allFine = false;
							break;
						}
					}
				}
			}
			/*
			else {
				// no input to validate so the page is valid
			}
			*/
		} else {
			for (String path : scmPaths) {
				if (TaskRepositoryUtil.getMatchingSourceRepository(
						TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()), path) == null) {
					allFine = false;
					setErrorMessage(NLS.bind(
							"Unable to map SCM path {0} to Crucible repository. Please define a mapping.", path));
				}
			}
		}

		setPageComplete(allFine);
		getContainer().updateButtons();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Composite repositoryMappingViewer = createRepositoryMappingComposite(composite, 700);
		GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(
				repositoryMappingViewer);
		repositoryMappingViewer.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}
}
