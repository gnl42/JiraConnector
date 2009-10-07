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

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog.Type;

/**
 * Action to add a label to a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
public class AddLabelToBuildAction extends EclipseBambooBuildSelectionListenerAction {

	public AddLabelToBuildAction() {
		super(null);
		inititalize();
	}

	private void inititalize() {
		setText("Add Label to Build...");
		setToolTipText("Add Label to Build");
		setImageDescriptor(BambooImages.LABEL);
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(null, eclipseBambooBuild.getBuild(),
				eclipseBambooBuild.getTaskRepository(), Type.LABEL);
		dialog.open();
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		return true;
	}
}
