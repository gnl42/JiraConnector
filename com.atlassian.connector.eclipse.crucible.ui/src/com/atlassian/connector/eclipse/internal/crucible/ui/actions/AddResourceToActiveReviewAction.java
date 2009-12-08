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
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddResourcesToReviewJob;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

/**
 * Action to add a file to the active review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class AddResourceToActiveReviewAction extends TeamAction {

	public AddResourceToActiveReviewAction() {
	}

	@Override
	protected void setActionEnablement(IAction action) {
		IResource[] resources = getSelectedResources();
		action.setEnabled(true);

		if (resources == null || resources.length == 0 || getActiveReview() == null) {
			action.setEnabled(false);
			return;
		}
	}

	protected Review getActiveReview() {
		return CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
	}

	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final AddResourcesToReviewJob job = new AddResourcesToReviewJob(getActiveReview(), getSelectedResources());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				final IStatus status = job.getStatus();
				if (!status.isOK()) {
					StatusHandler.log(status);

					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							MessageBox mb = new MessageBox(WorkbenchUtil.getShell(), SWT.OK | SWT.ICON_INFORMATION);
							mb.setText(AtlassianCorePlugin.PRODUCT_NAME);
							String message = NLS.bind(
									"Failed to add selected resources to active review. Error message was: \n\n{0}",
									status.getMessage());
							if (status.getMessage().contains("does not exist")) {
								message += "\n\nCheck if your mappings are correct in preferences Atlassian->Repository Mappings.";
							}
							mb.setMessage(message);
							mb.open();
						}
					});
				} else {

				}
			}
		});
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}
