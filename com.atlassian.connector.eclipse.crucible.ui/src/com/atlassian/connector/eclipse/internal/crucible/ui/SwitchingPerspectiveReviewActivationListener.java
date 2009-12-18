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

package com.atlassian.connector.eclipse.internal.crucible.ui;

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * 
 * @author Pawel Niewiadomski
 */
public class SwitchingPerspectiveReviewActivationListener implements IReviewActivationListener {

	private IPerspectiveDescriptor previousPerspective;

	private IPerspectiveDescriptor getActivePerspective() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			return activePage.getPerspective();
		}
		return null;
	}

	public void reviewActivated(ITask task, Review review) {
		IPerspectiveDescriptor perspective = getActivePerspective();
		if (!perspective.getId().equals(CrucibleUiPlugin.REVIEW_PERSPECTIVE_ID)) {
			previousPerspective = perspective;
			try {
				PlatformUI.getWorkbench().showPerspective(CrucibleUiPlugin.REVIEW_PERSPECTIVE_ID,
						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			} catch (WorkbenchException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
						"Unable to switch perspectives", e));
			}
		} else {
			previousPerspective = null;
		}
	}

	public void reviewDeactivated(ITask task, Review review) {
		IPerspectiveDescriptor perspective = getActivePerspective();
		if (previousPerspective != null) {
			if (perspective.getId().equals(CrucibleUiPlugin.REVIEW_PERSPECTIVE_ID)) {
				try {
					PlatformUI.getWorkbench().showPerspective(previousPerspective.getId(),
							PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
							"Unable to switch perspectives", e));
				}
			}
		}
	}

	public void reviewUpdated(ITask task, Review review) {
		// do nothing
	}

}
