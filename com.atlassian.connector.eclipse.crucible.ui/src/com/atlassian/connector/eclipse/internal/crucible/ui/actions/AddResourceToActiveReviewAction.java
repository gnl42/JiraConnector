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

import com.atlassian.connector.eclipse.internal.core.jobs.JobWithStatus;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddDecoratedResourcesToReviewJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.RefreshReviewAndTaskListJob;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionPage;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Action to add a file to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddResourceToActiveReviewAction extends AbstractReviewFromResourcesAction {

	private final class AddResourceToActiveReviewWizard extends Wizard {
		private final List<IResource> resources;

		private ResourceSelectionPage resourceSelectionPage;

		private final ITeamUiResourceConnector teamConnector;

		private AddResourceToActiveReviewWizard(ITeamUiResourceConnector teamConnector, List<IResource> resources) {
			this.resources = resources;
			this.teamConnector = teamConnector;
		}

		@Override
		public void addPages() {
			resourceSelectionPage = new ResourceSelectionPage(
					CrucibleUiUtil.getCrucibleTaskRepository(getActiveReview()), teamConnector, resources);
			addPage(resourceSelectionPage);
		}

		@Override
		public boolean performFinish() {
			setErrorMessage(null);

			final List<DecoratedResource> resources = resourceSelectionPage.getSelection();
			if (resources != null && resources.size() > 0) {
				Review review = getActiveReview();
				IStatus result = runJobInContainer(new AddDecoratedResourcesToReviewJob(review,
						resourceSelectionPage.getTeamResourceConnector(), resources));
				if (!result.isOK()) {
					StatusHandler.log(result);
					setErrorMessage(result.getMessage());
					return false;
				} else {
					runJobInContainer(new RefreshReviewAndTaskListJob(review));
				}
			}

			return true;
		}

		private void setErrorMessage(String message) {
			IWizardPage page = getContainer().getCurrentPage();
			if (page instanceof WizardPage) {
				((WizardPage) page).setErrorMessage(message != null ? message.replace("\n", " ") : null);
			}
		}

		private IStatus runJobInContainer(final JobWithStatus job) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					job.run(monitor);
				}
			};
			try {
				getContainer().run(true, true, runnable);
			} catch (Exception e) {
				return new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, String.format("Job \"%s\" failed",
						job.getName()), e);
			}
			return job.getStatus();
		}
	}

	public AddResourceToActiveReviewAction() {
		super("Add to Active Review");
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		Review review = getActiveReview();
		if (selection == null || selection.size() == 0 || review == null) {
			return false;
		}

		if (!getActiveReview().getActions().contains(CrucibleAction.MODIFY_FILES)) {
			return false;
		}
		return true;
	}

	protected Review getActiveReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	protected void openReviewWizard(final ResourceEditorBean selection, final ITeamUiResourceConnector connector,
			boolean isPostCommit, final Shell shell) {

		openReviewWizard(connector, Arrays.asList(selection.getResource()), isPostCommit, shell);
	}

	protected void openReviewWizard(final ITeamUiResourceConnector teamConnector, final List<IResource> resources,
			boolean isCrucible21Required, Shell shell) {
		WizardDialog wd = null;
		Wizard wizard = new AddResourceToActiveReviewWizard(teamConnector, resources);
		wd = new WizardDialog(shell, wizard);
		wd.setBlockOnOpen(true);
		wd.open();
	}
}
