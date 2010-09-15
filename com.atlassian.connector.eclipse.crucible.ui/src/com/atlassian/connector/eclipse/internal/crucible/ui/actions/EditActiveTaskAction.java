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

import com.atlassian.connector.eclipse.internal.crucible.ui.ActiveReviewManager.IReviewActivationListener;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.Display;
import java.util.Collection;

public class EditActiveTaskAction extends Action implements IReviewActivationListener {

	private ITask activeTask;

	public EditActiveTaskAction() {
		setImageDescriptor(CommonImages.BROWSER_OPEN_TASK);
		setText("Open Active Task");
		setToolTipText("Open Active Task Editor");
		setEnabled(false);
	}

	public void run() {
		TasksUiUtil.openTask(activeTask);
	}

	public void reviewActivated(final ITask task, Review review) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				activeTask = task;
				setEnabled(activeTask != null);
			}
		});
	}

	public void reviewDeactivated(ITask task, Review review) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				activeTask = null;
				setEnabled(false);
			}
		});
	}

	public void reviewUpdated(ITask task, Review review, Collection<CrucibleNotification> differences) {
		reviewActivated(task, review);
	}

}
