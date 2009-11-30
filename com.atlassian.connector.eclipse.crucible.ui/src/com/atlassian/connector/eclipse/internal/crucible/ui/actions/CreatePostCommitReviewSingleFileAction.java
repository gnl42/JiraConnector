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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.core.AtlassianCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleRepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucible21RepositoryPage;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.connector.eclipse.ui.team.ITeamResourceConnector;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.RevisionInfo;
import com.atlassian.connector.eclipse.ui.team.TeamConnectorType;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.team.internal.ui.actions.TeamAction;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("restriction")
public class CreatePostCommitReviewSingleFileAction extends TeamAction {

	public CreatePostCommitReviewSingleFileAction() {
		// ignore
	}

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IResource[] resources = getSelectedResources();

		if (resources != null && resources.length > 0 && resources[0] != null) {

			ITeamResourceConnector connector = AtlassianUiPlugin.getDefault()
					.getTeamResourceManager()
					.getTeamConnector(resources[0]);

			if (connector.getType() != TeamConnectorType.SVN) {
				MessageBox mb = new MessageBox(WorkbenchUtil.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setText(AtlassianCorePlugin.PRODUCT_NAME);
				mb.setMessage("Cannot create review from non subversion resource. Only subversion is supported.");
				mb.open();
				return;
			}

			// TODO show info if the file is dirty

			try {
				RevisionInfo revisionInfo = TeamUiUtils.getLocalRevision(resources[0]);
				RepositoryInfo repositoryInfo = TeamUiUtils.getApplicableRepository(resources[0]);

				if (revisionInfo != null && repositoryInfo != null) {
					openReviewWizard(revisionInfo, repositoryInfo);
				} else {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"Cannot get revision number for selected resource."));
				}

			} catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Cannot get local revision for selected resource", e));
			}
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Nothing selected. Cannot create review."));
		}

	}

	private void openReviewWizard(final RevisionInfo revisionInfo, final RepositoryInfo repositoryInfo) {
		SelectCrucible21RepositoryPage selectRepositoryPage = new SelectCrucible21RepositoryPage() {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				ReviewWizard wizard = new ReviewWizard(taskRepository,
						MiscUtil.buildHashSet(ReviewWizard.Type.ADD_SCM_FILE));
				wizard.setSelectedScmResource(revisionInfo, repositoryInfo);
				return wizard;
			}
		};

		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), new CrucibleRepositorySelectionWizard(
				selectRepositoryPage));
		wd.setBlockOnOpen(true);
		wd.open();
	}

	@Override
	protected void setActionEnablement(IAction action) {
		// TeamAction overrides plugin.xml settings so do it here
		if (getSelectedResources().length == 1) {
			action.setEnabled(enabledFor(getSelectedResources()[0]));
		} else {
			action.setEnabled(false);
		}
	}

	private boolean enabledFor(IResource selected) {
		RevisionInfo localRevision = null;
		try {
			localRevision = TeamUiUtils.getLocalRevision(selected);
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
					"Cannot enable action (cannot determine local revision).", e));
			return false;
		}

		if (localRevision != null) {
			String stringRevision = localRevision.getRevision();
			if (stringRevision != null) {
				try {
					return Double.valueOf(stringRevision).doubleValue() > 0;
				} catch (NumberFormatException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"Cannot enable action. Unrecognized revison number [" + stringRevision + "]", e));
					return false;
				}
			}
		}

		return false;
	}
}
