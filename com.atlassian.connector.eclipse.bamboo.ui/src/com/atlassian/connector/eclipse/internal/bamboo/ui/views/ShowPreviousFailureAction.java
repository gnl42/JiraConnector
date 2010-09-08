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

package com.atlassian.connector.eclipse.internal.bamboo.ui.views;

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jface.action.Action;

class ShowPreviousFailureAction extends Action {

	private final TestResultsView fPart;

	public ShowPreviousFailureAction(TestResultsView part) {
		super(JUnitMessages.ShowPreviousFailureAction_label);
		setDisabledImageDescriptor(JUnitPlugin.getImageDescriptor("dlcl16/select_prev.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/select_prev.gif")); //$NON-NLS-1$
		setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/select_prev.gif")); //$NON-NLS-1$
		setToolTipText(JUnitMessages.ShowPreviousFailureAction_tooltip);
		fPart = part;
	}

	public void run() {
		fPart.selectPreviousFailure();
	}
}
