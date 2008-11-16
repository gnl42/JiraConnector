/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Vladimir Bykov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.atlassian.theplugin.eclipse.ui.panel.AbstractAdvancedDialogPanel;
import com.atlassian.theplugin.eclipse.ui.panel.IDialogManagerEx;

/**
 * Advanced dialog implementation
 * 
 * @author Vladimir Bykov
 */
public class AdvancedDialog extends DefaultDialog implements IDialogManagerEx {
	protected Button []buttonsEx;
	protected String []buttonLabelsEx;
	protected int basePanelButtonsCount;
	protected int focusButtonIdx;
	
    public AdvancedDialog(Shell parentShell, AbstractAdvancedDialogPanel panel) {
		super(parentShell, panel);
		this.basePanelButtonsCount = panel.getButtonNames().length;
		this.buttonLabelsEx = panel.getButtonNamesEx();
	}
    
    public AdvancedDialog(Shell parentShell, AbstractAdvancedDialogPanel panel, int focusButtonIdx) {
		super(parentShell, panel);
		this.basePanelButtonsCount = panel.getButtonNames().length;
		this.buttonLabelsEx = panel.getButtonNamesEx();
		this.focusButtonIdx = focusButtonIdx;
	}
   
	protected void buttonPressed(int buttonId) {
	    if (buttonId < this.basePanelButtonsCount) {
	    	this.baseButtonPressed(buttonId);
	    } else {
	    	this.extendedButtonPressed(buttonId - this.basePanelButtonsCount);
	    }
	}
	
	protected void baseButtonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}
		
	protected void extendedButtonPressed(int buttonId) {
		((AbstractAdvancedDialogPanel) this.panel).extendedButtonPressed(buttonId);
	}

	protected Control createButtonPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
	    
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		buttonPanel.setLayout(layout);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		buttonPanel.setLayoutData(data);

		this.createExtendedButtonPanel(buttonPanel);
		this.createBaseButtonPanel(buttonPanel);
		
		ArrayList<Button> allButtons = new ArrayList<Button>();
		for (int i = 0; i < this.getButtonLabels().length; i++) {
			allButtons.add(this.getButton(i));
		}
		this.setButtons((Button[]) allButtons.toArray(new Button[allButtons.size()]));
		if (this.focusButtonIdx != 0) {
			this.getShell().setDefaultButton(this.getButton(this.focusButtonIdx));
		}
		return buttonPanel;
	}
	
	protected Control createExtendedButtonPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
	    
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonPanel.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = SWT.LEFT;
		buttonPanel.setLayoutData(data);
		
		return this.createExtendedButtonBar(buttonPanel);
	}
	
	protected Control createBaseButtonPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
	    
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonPanel.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = SWT.RIGHT;
		buttonPanel.setLayoutData(data);
		
		return this.createButtonBar(buttonPanel);
	}
	
	protected Control createExtendedButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		// Add the buttons to the left button bar.
		this.createButtonsForExtendedButtonBar(composite);
		return composite;
	}
	
	protected void createButtonsForExtendedButtonBar(Composite parent) {
		this.buttonsEx = new Button[this.buttonLabelsEx.length];
		for (int i = 0; i < this.buttonsEx.length; i++) {
			String label = this.buttonLabelsEx[i];
			Button button = this.createButton(parent, this.basePanelButtonsCount + i, label, false);
			this.buttonsEx[i] = button;
		}
	}
	
	public Button getButtonEx(int idx) {
		return this.buttonsEx[idx];
	}
	
	public void setButtonEx(Button[] newButtons) {
		this.buttonsEx = newButtons;
	}
	
	public void setExtendedButtonEnabled(int idx, boolean enabled) {
		this.buttonsEx[idx].setEnabled(enabled);
	}
	
	public void setExtendedButtonCaption(int idx, String text) {
		this.buttonsEx[idx].setText(text);
	}
	
}
