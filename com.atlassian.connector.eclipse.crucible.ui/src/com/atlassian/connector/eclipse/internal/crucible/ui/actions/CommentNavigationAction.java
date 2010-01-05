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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;

import java.util.ResourceBundle;

/**
 * This class is modelled after {@link org.eclipse.team.internal.ui.synchronize.actions.NavigateAction}
 * 
 * @author wseliga
 */
public class CommentNavigationAction extends Action {
	private final boolean isNext;

	private static final String ACTION_BUNDLE = "com.atlassian.connector.eclipse.internal.crucible.ui.actions.actions"; //$NON-NLS-1$

	public CommentNavigationAction(IViewSite viewSite, boolean next) {
		this.isNext = next;
		IActionBars bars = viewSite.getActionBars();
		if (next) {
			Utils.initAction(this, "action.navigateNext.", ResourceBundle.getBundle(ACTION_BUNDLE)); //$NON-NLS-1$
			setActionDefinitionId(ActionFactory.NEXT.getCommandId());
			if (bars != null) {
				bars.setGlobalActionHandler(ActionFactory.NEXT.getId(), this);
			}
		} else {
			Utils.initAction(this, "action.navigatePrevious.", ResourceBundle.getBundle(ACTION_BUNDLE)); //$NON-NLS-1$
			setActionDefinitionId(ActionFactory.PREVIOUS.getCommandId());
			if (bars != null) {
				bars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), this);
			}
		}
	}

	public void run() {
		MessageDialog.openInformation(null, "Info", "Navigate to " + (isNext ? "Next" : "Previous")
				+ " comment Not yet implemented");
	}
}