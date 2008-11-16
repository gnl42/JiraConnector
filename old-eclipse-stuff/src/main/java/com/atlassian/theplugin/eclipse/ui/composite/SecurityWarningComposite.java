/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.dialog.DefaultDialog;

/**
 * Security warning composite
 * 
 * @author Sergiy Logvin
 */
public class SecurityWarningComposite extends Composite {
	
	public SecurityWarningComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.init();
	}
	
	protected void init() {
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label warningLabel = new Label(this, SWT.NONE);
		warningLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		warningLabel.setImage(Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
		
		Label description = new Label(this, SWT.WRAP);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		Dialog.applyDialogFont(description);
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(description, 2);
		description.setLayoutData(data);
		description.setText(Activator.getDefault().getResource("SecurityWarningComposite.Message"));
    }
	
}
