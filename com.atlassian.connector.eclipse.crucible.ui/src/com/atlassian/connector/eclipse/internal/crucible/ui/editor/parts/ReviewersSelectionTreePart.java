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

import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.ui.viewers.ArrayTreeContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.User;
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
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Part containing a selection tree for reviewers
 * 
 * @author Thomas Ehrnhoefer
 */
public class ReviewersSelectionTreePart {

	private final Set<User> selectedReviewers;

	private CheckboxFilteredTree tree;

	private ICheckStateListener externalListener;

	private final Collection<User> allReviewers;

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

	public ReviewersSelectionTreePart(@NotNull Set<User> selectedUsers, @NotNull Collection<User> allReviewers) {
		selectedReviewers = selectedUsers == null ? new HashSet<User>() : new HashSet<User>(selectedUsers);
		this.allReviewers = new HashSet<User>(allReviewers);
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

		tree.getViewer().setContentProvider(ArrayTreeContentProvider.getInstance());

		tree.getViewer().setInput(allReviewers);
		// updateInput();

		tree.getViewer().setLabelProvider(new CrucibleUserLabelProvider());

		for (TreeItem item : tree.getViewer().getTree().getItems()) {
			if (selectedReviewers.contains(item.getData())) {
				item.setChecked(true);
			}
		}

		tree.getViewer().setSorter(new ViewerSorter());
		tree.getViewer().setCheckedElements(selectedReviewers.toArray(new User[selectedReviewers.size()]));

		tree.getViewer().addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					selectedReviewers.add((User) event.getElement());
				} else {
					selectedReviewers.remove(event.getElement());
				}
				if (externalListener != null) {
					externalListener.checkStateChanged(event);
				}
			}
		});

		parent.pack();

		return composite;
	}

	public void setAllReviewers(@NotNull Collection<User> allReviewers) {
		this.allReviewers.clear();
		this.allReviewers.addAll(allReviewers);
		tree.getViewer().setInput(this.allReviewers);
	}

	public Set<User> getSelectedReviewers() {
		return selectedReviewers;
	}
}
