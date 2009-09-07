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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.ReviewersSelectionTreePart;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.Set;

/**
 * Dialog for selecting reviewers
 * 
 * @author Thomas Ehrnhoefer
 */
public class ReviewerSelectionDialog extends Dialog {

	private final Set<Reviewer> selectedReviewers;

	private final Set<Reviewer> allReviewers;

	private final Review review;

	private ReviewersSelectionTreePart reviewersSelectionTreePart;

	public ReviewerSelectionDialog(Shell shell, Review review, Set<User> users) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.review = review;
		try {
			selectedReviewers = review.getReviewers();
		} catch (ValueNotYetInitialized e) {
			throw new RuntimeException(e);
		}
		allReviewers = CrucibleUiUtil.getAllCachedUsersAsReviewers(review);
	}

//	private ReviewerBean createReviewerFromCachedUser(CrucibleCachedUser user) {
//		ReviewerBean reviewer = new ReviewerBean();
//		reviewer.setDisplayName(user.getDisplayName());
//		reviewer.setUserName(user.getUserName());
//		boolean completed = false;
//		try {
//			for (Reviewer r : review.getReviewers()) {
//				if (r.getUserName().equals(reviewer.getUserName())) {
//					completed = r.isCompleted();
//					selectedReviewers.add(reviewer);
//					break;
//				}
//			}
//		} catch (ValueNotYetInitialized e) {
//			// ignore
//		}
//		reviewer.setCompleted(completed);
//		return reviewer;
//	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select Reviewer(s)");
		reviewersSelectionTreePart = new ReviewersSelectionTreePart(selectedReviewers, allReviewers);
		Composite composite = reviewersSelectionTreePart.createControl(parent);

		applyDialogFont(composite);

		return composite;
	}

	public Set<Reviewer> getSelectedReviewers() {
		return reviewersSelectionTreePart.getSelectedReviewers();
	}
}
