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

package com.atlassian.connector.eclipse.internal.crucible.ui.operations;

import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.LocalStatus;
import com.atlassian.connector.eclipse.ui.team.ScmRepository;
import com.atlassian.connector.eclipse.ui.team.TeamResourceManager;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.RevisionData;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddResourcesToReviewJob extends JobWithStatus {

	private final IResource[] resources;

	private final Review review;

	private final TaskRepository repository;

	private final Map<String, String> repositoryMappings;

	public AddResourcesToReviewJob(@NotNull Review review, @NotNull IResource[] resources) {
		super("Add resources to Review");
		this.resources = resources;
		this.review = review;
		this.repository = CrucibleUiUtil.getCrucibleTaskRepository(review);
		this.repositoryMappings = TaskRepositoryUtil.getScmRepositoryMappings(repository);
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) {
		SubMonitor submonitor = SubMonitor.convert(monitor);

		if (resources.length == 0) {
			return;
		}

		CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
		CrucibleClient client = connector.getClientManager().getClient(getTaskRepository());

		List<RevisionData> revisions = MiscUtil.buildArrayList();
		final Set<ScmRepository> repositoriesWithoutMapping = MiscUtil.buildHashSet();

		TeamResourceManager teamManager = AtlassianUiPlugin.getDefault().getTeamResourceManager();
		for (IResource resource : resources) {
			ITeamResourceConnector teamConnector = teamManager.getTeamConnector(resource);
			if (teamConnector == null) {
				tellUserToCommitFirst();
				return;
			}

			ScmRepository repositoryInfo;
			try {
				repositoryInfo = teamConnector.getApplicableRepository(resource);
			} catch (CoreException e1) {
				setStatus(e1.getStatus());
				return;
			}

			if (!repositoryMappings.containsKey(repositoryInfo.getScmPath())) {
				repositoriesWithoutMapping.add(repositoryInfo);
			}
		}

		// if there are some repositories missing mapping ask user to define them
		if (repositoriesWithoutMapping.size() > 0) {
			final boolean[] abort = { false };

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new DefineRepositoryMappingsWizard(
							getTaskRepository(), repositoriesWithoutMapping));
					wd.setBlockOnOpen(true);
					if (wd.open() == Window.CANCEL) {
						abort[0] = true;
					}
				}
			});

			if (abort[0]) {
				return;
			}
		}

		// 
		for (IResource resource : resources) {
			ITeamResourceConnector teamConnector = teamManager.getTeamConnector(resource);
			if (teamConnector == null) {
				tellUserToCommitFirst();
				return;
			}

			ScmRepository repositoryInfo;
			try {
				repositoryInfo = teamConnector.getApplicableRepository(resource);
			} catch (CoreException e1) {
				setStatus(e1.getStatus());
				return;
			}

			LocalStatus revision;
			try {
				revision = teamConnector.getLocalRevision(resource);
			} catch (CoreException e) {
				setStatus(e.getStatus());
				return;
			}

			if (revision.isDirty() || revision.isAdded()) {
				tellUserToCommitFirst();
				return;
			}

			String crucibleProject = repositoryMappings.get(repositoryInfo.getScmPath());

			RevisionData rd = new RevisionData(crucibleProject, revision.getScmPath().replace(
					repositoryInfo.getScmPath(), ""), Arrays.asList(revision.getRevision())); //$NON-NLS-1$

			revisions.add(rd);
		}

		AddFilesToReviewRemoteOperation operation = new AddFilesToReviewRemoteOperation(getTaskRepository(), review,
				revisions, submonitor.newChild(1));

		try {
			client.execute(operation);
		} catch (CoreException e) {
			setStatus(e.getStatus());
			return;
		}

		// hack to trigger task list synchronization
		try {
			client.getReview(getTaskRepository(), CrucibleUtil.getTaskIdFromPermId(review.getPermId().getId()), true,
					monitor);
		} catch (CoreException e) {
			setStatus(e.getStatus());
			return;
		}
	}

	protected TaskRepository getTaskRepository() {
		return repository;
	}

	protected void tellUserToCommitFirst() {
		WorkbenchUtil.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageBox mb = new MessageBox(WorkbenchUtil.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setText(AtlassianCorePlugin.PRODUCT_NAME);
				mb.setMessage("Some resources you selected are unversioned or have local modifications. Please commit them first and try again.");
				mb.open();
				return;
			}
		});
	}

}
