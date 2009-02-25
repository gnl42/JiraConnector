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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class SetRefreshIntervalAction implements IViewActionDelegate {

	public void init(IViewPart view) {
	}

	public void run(IAction action) {
		InputDialog syncIntervalDialog = new InputDialog(null, "Set Preference",
				"Set the interval (in minutes) in between automatic refreshing",
				String.valueOf(BambooCorePlugin.getRefreshIntervalMinutes()), new IInputValidator() {
					public String isValid(String newText) {
						try {
							int number = Integer.parseInt(newText);
							if (number < 1) {
								return "Please enter the refreshing interval (in minutes). [Value needs to be > 0]";
							}
						} catch (Exception e) {
							return "Please enter the refreshing interval (in minutes). [Value needs to be a number]";
						}
						return null;
					}
				});
		if (syncIntervalDialog.open() == Window.OK) {
			BambooCorePlugin.setRefreshIntervalMinutes(Integer.parseInt(syncIntervalDialog.getValue()));
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
