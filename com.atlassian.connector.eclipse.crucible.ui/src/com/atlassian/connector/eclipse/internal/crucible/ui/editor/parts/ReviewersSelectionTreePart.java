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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewerBean;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.provisional.commons.ui.SubstringPatternFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * Part containing a selection tree for reviewers
 * 
 * @author Thomas Ehrnhoefer
 */
public class ReviewersSelectionTreePart {

	private final Set<Reviewer> selectedReviewers;

	private final Review review;

	private CheckboxFilteredTree tree;

	private ICheckStateListener externalListener;

//	private final Set<Reviewer> allReviewers;

//	private final Review review;

	private class CheckboxFilteredTree extends FilteredTree {

		protected CheckboxFilteredTree(Composite parent) {
			super(parent);
		}

		public CheckboxFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
			super(parent, treeStyle, filter);
		}

		@Override
		protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
			return new CheckboxTreeViewer(parent, style);
		}

		@Override
		public CheckboxTreeViewer getViewer() {
			return (CheckboxTreeViewer) super.getViewer();
		}

	}

	public ReviewersSelectionTreePart(Review review) {
		this(new HashSet<Reviewer>(), review);
	}

	public ReviewersSelectionTreePart(Set<Reviewer> selectedUsers, Review review) {
		selectedReviewers = selectedUsers == null ? new HashSet<Reviewer>() : selectedUsers;
		this.review = review;
	}

	public void setCheckStateListener(ICheckStateListener listener) {
		externalListener = listener;
	}

	public Composite createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		composite.setLayoutData(gd);

		tree = new CheckboxFilteredTree(composite, SWT.CHECK | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER,
				new SubstringPatternFilter());

		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 250).applyTo(tree);

		tree.getViewer().setContentProvider(new CrucibleUserContentProvider());

		updateInput();

		tree.getViewer().setLabelProvider(new CrucibleUserLabelProvider());

		for (TreeItem item : tree.getViewer().getTree().getItems()) {
			if (selectedReviewers.contains(item.getText())) {
				item.setChecked(true);
			}
		}

		tree.getViewer().setSorter(new ViewerSorter());

		tree.getViewer().setCheckedElements(getCachedUsersFromReviewers(selectedReviewers));

		tree.getViewer().addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					selectedReviewers.add(createReviewerFromCachedUser((CrucibleCachedUser) event.getElement()));
				} else {
					selectedReviewers.remove(createReviewerFromCachedUser((CrucibleCachedUser) event.getElement()));
				}
				if (externalListener != null) {
					externalListener.checkStateChanged(event);
				}
			}
		});

		parent.pack();

		return composite;
	}

	public void updateInput() {
		tree.getViewer().setInput(CrucibleUiUtil.getCachedUsers(review));
	}

	private CrucibleCachedUser[] getCachedUsersFromReviewers(Set<Reviewer> reviewers) {
		CrucibleCachedUser[] users = new CrucibleCachedUser[reviewers.size()];
		int i = 0;
		for (Reviewer reviewer : reviewers) {
			users[i++] = new CrucibleCachedUser(reviewer);
		}
		return users;
	}

	private ReviewerBean createReviewerFromCachedUser(CrucibleCachedUser user) {
		ReviewerBean reviewer = new ReviewerBean();
		reviewer.setDisplayName(user.getDisplayName());
		reviewer.setUserName(user.getUserName());
		boolean completed = false;
		try {
			for (Reviewer r : review.getReviewers()) {
				if (r.getUserName().equals(reviewer.getUserName())) {
					completed = r.isCompleted();
					selectedReviewers.add(reviewer);
					break;
				}
			}
		} catch (ValueNotYetInitialized e) {
			// ignore
		}
		reviewer.setCompleted(completed);
		return reviewer;
	}

	public Set<Reviewer> getSelectedReviewers() {
		return selectedReviewers;
	}
}
