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

package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.MarkCommentsReadJob;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Pawel Niewiadomski
 */
final class MarkCommentsReadSelectionListener implements ISelectionChangedListener {
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Iterator<?> it = selection.iterator();
		final Collection<Comment> markAsRead = MiscUtil.buildArrayList();

		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof Comment) {
				if (((Comment) element).getReadState().equals(ReadState.UNREAD)
						&& !((Comment) element).getReadState().equals(ReadState.LEAVE_UNREAD)) {
					markAsRead.add((Comment) element);
				}
			}
		}

		if (markAsRead.size() > 0) {
			Review activeReview = CrucibleUiPlugin.getDefault().getActiveReviewManager().getActiveReview();
			MarkCommentsReadJob job = new MarkCommentsReadJob(activeReview, markAsRead, true);
			job.schedule();
		}
	}
}