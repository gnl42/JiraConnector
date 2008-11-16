/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.dialog.bamboo;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Validate location error dialog
 * 
 * @author Alexander Gurov
 */
public class NonValidBambooLocationErrorDialog extends MessageDialog {

	public NonValidBambooLocationErrorDialog(Shell parentShell, String message) {
		super(parentShell, 
			Activator.getDefault().getResource("NonValidBambooLocationErrorDialog.Title"), 
			null, 
			MessageFormat.format(Activator.getDefault().getResource("NonValidBambooLocationErrorDialog.Message"), new Object[] {message == null ? "" : message + "\n\n"}),
			MessageDialog.QUESTION, 
			new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
			0);
	}

}
