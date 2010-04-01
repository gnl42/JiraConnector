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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard.Type;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Display;

import java.util.Set;

public class AddPatchToActiveReviewAction extends Action implements IReviewActivationListener {

	protected Review activeReview;

	public AddPatchToActiveReviewAction() {
		setText("Add Patch...");
		setToolTipText("Add a patch to the Review");
		setImageDescriptor(CrucibleImages.ADD_PATCH);
	}

	public void run() {
		ReviewWizard wizard = new ReviewWizard(activeReview, Type.ADD_PATCH);
		wizard.setWindowTitle("Add Patch");
		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(), wizard);
		wd.setBlockOnOpen(true);
		wd.open();
	};

	public void reviewActivated(final ITask task, final Review review) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				activeReview = review;

				Set<CrucibleAction> actions = activeReview.getActions();

				setEnabled(activeReview != null && actions != null && actions.contains(CrucibleAction.MODIFY_FILES));
			}
		});
	}

	public void reviewDeactivated(ITask task, Review review) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				activeReview = null;
				setEnabled(false);
			}
		});
	}

	public void reviewUpdated(ITask task, Review review) {
		reviewActivated(task, review);
	};

}
