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

import org.eclipse.jdt.internal.junit.ui.IJUnitHelpContextIds;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Toggles console auto-scroll
 */
public class ScrollLockAction extends Action {

	private final TestResultsView fRunnerViewPart;

	public ScrollLockAction(TestResultsView viewer) {
		super(JUnitMessages.ScrollLockAction_action_label);
		fRunnerViewPart = viewer;
		setToolTipText(JUnitMessages.ScrollLockAction_action_tooltip);
		setDisabledImageDescriptor(JUnitPlugin.getImageDescriptor("dlcl16/lock.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/lock.gif")); //$NON-NLS-1$
		setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/lock.gif")); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJUnitHelpContextIds.OUTPUT_SCROLL_LOCK_ACTION);
		setChecked(false);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fRunnerViewPart.setAutoScroll(!isChecked());
	}
}
