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

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditor;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditorInput;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action to open a Bamboo Rich Editor
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
public class OpenBambooEditorAction extends EclipseBambooBuildSelectionListenerAction {

	public OpenBambooEditorAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setText("Open");
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		BambooEditorInput input = new BambooEditorInput(eclipseBambooBuild);
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
						"Failed to open Bamboo Rich Editor: no available workbench window. Please try again."));
			} else {
				window.getActivePage().openEditor(input, BambooEditor.ID);
			}
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Failed to open Bamboo Rich Editor: "
					+ e.getMessage(), e));
		}
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		return true;
	}

}
