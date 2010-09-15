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
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Set;

/**
 * Dialog for selecting reviewers
 * 
 * @author Thomas Ehrnhoefer
 */
public class ReviewerSelectionDialog extends Dialog {

	private final Set<User> selectedReviewers;

	private final Set<User> allReviewers;

	private ReviewersSelectionTreePart reviewersSelectionTreePart;

	public ReviewerSelectionDialog(Shell shell, @NotNull Review review, @NotNull Collection<User> users) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		selectedReviewers = CrucibleUiUtil.toUsers(review.getReviewers());
		allReviewers = MiscUtil.buildHashSet(users);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select Reviewer(s)");
		reviewersSelectionTreePart = new ReviewersSelectionTreePart(selectedReviewers, allReviewers);
		final Composite composite = reviewersSelectionTreePart.createControl(parent);
		applyDialogFont(composite);
		return composite;
	}

	public Set<User> getSelectedReviewers() {
		return reviewersSelectionTreePart.getSelectedReviewers();
	}
}
