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

package me.glindholm.connector.eclipse.internal.bamboo.ui.views;

import org.eclipse.jface.action.Action;

import me.glindholm.connector.eclipse.internal.bamboo.ui.BambooImages;

class ShowNextFailureAction extends Action {

	private final TestResultsView fPart;

	public ShowNextFailureAction(TestResultsView part) {
		super("Next Failure");
		setDisabledImageDescriptor(BambooImages.getImageDescriptor("dlcl16/select_next.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(BambooImages.getImageDescriptor("elcl16/select_next.gif")); //$NON-NLS-1$
		setImageDescriptor(BambooImages.getImageDescriptor("elcl16/select_next.gif")); //$NON-NLS-1$
		setToolTipText("Next Failed Test");
		fPart = part;
	}

	public void run() {
		fPart.selectNextFailure();
	}
}
