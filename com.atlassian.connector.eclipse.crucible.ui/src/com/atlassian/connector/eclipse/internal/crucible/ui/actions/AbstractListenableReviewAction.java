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

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;

public abstract class AbstractListenableReviewAction extends AbstractReviewAction implements IReviewAction {

	private IReviewActionListener actionListener;

	public AbstractListenableReviewAction(String text) {
		super(text);
	}

	public void setActionListener(IReviewActionListener listener) {
		actionListener = listener;
	}

	@Override
	public final void run() {
		if (actionListener != null) {
			actionListener.actionAboutToRun(this);
		}
		run(this);
		if (actionListener != null) {
			actionListener.actionRan(this);
		}
	}

}