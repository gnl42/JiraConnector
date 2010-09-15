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
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskActivateAction;
import org.eclipse.mylyn.internal.tasks.ui.actions.TaskSelectionDialogWithRandom;
import org.eclipse.mylyn.internal.tasks.ui.commands.ActivateTaskHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import java.util.Collection;

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
		if (!isUserInteraction()) {
			return;
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
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
		});
	}

	@SuppressWarnings("restriction")
	private boolean isUserInteraction() {
		Exception e = new Exception();
		e.fillInStackTrace();
		StackTraceElement[] stack = e.getStackTrace();
		for (StackTraceElement element : stack) {
			String className = element.getClassName();
			if (className.contains(TaskActivateAction.class.getName())
					|| className.contains(ActivateTaskHandler.class.getName())
					|| className.contains(TaskSelectionDialogWithRandom.class.getName())) {
				return true;
			}
		}
		return false;
	}

	public void reviewDeactivated(ITask task, Review review) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
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
		});
	}

	public void reviewUpdated(ITask task, Review review, Collection<CrucibleNotification> differences) {
		// do nothing
	}

}
