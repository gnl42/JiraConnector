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

package com.atlassian.theplugin.eclipse.ui.panel;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.atlassian.theplugin.eclipse.ui.verifier.AbstractVerificationKeyListener;
import com.atlassian.theplugin.eclipse.ui.verifier.AbstractVerifier;
import com.atlassian.theplugin.eclipse.ui.verifier.IValidationManager;

/**
 * Abstract dialog panel
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractDialogPanel implements IDialogPanel,
		IValidationManager {
	private AbstractVerificationKeyListener changeListener;

	protected IDialogManager manager;

	protected String dialogTitle;
	protected String dialogDescription;
	protected String defaultMessage;
	protected String imagePath;
	protected String[] buttonNames;

	public AbstractDialogPanel() {
		this(new String[] { IDialogConstants.OK_LABEL,
				IDialogConstants.CANCEL_LABEL });
	}

	public AbstractDialogPanel(String[] buttonNames) {
		this.buttonNames = buttonNames;
		this.changeListener = new VerificationKeyListener();
	}

	public void initPanel(IDialogManager manager) {
		this.manager = manager;
	}

	public void postInit() {
		this.validateContent();
		this.setMessage(IDialogManager.LEVEL_OK, null);
	}

	public void addListeners() {
		this.changeListener.addListeners();
	}

	public void dispose() {
		this.detachAll();
	}

	public String getDialogTitle() {
		return this.dialogTitle;
	}

	public String getDialogDescription() {
		return this.dialogDescription;
	}

	public String getDefaultMessage() {
		return this.defaultMessage;
	}

	public String getImagePath() {
		return this.imagePath;
	}

	public Point getPrefferedSize() {
		return new Point(470, SWT.DEFAULT);
	}

	public String[] getButtonNames() {
		return this.buttonNames;
	}

	public String getHelpId() {
		return null;
	}

	public void createControls(Composite parent) {

	}

	public void buttonPressed(int idx) {
		if (idx == 0) {
			this.saveChanges();
		} else {
			this.cancelChanges();
		}
	}

	public boolean isFilledRight() {
		return this.changeListener.isFilledRight();
	}

	public void attachTo(Control cmp, AbstractVerifier verifier) {
		this.changeListener.attachTo(cmp, verifier);
	}

	public void detachFrom(Control cmp) {
		this.changeListener.detachFrom(cmp);
	}

	public void detachAll() {
		this.changeListener.detachAll();
	}

	public void validateContent() {
		this.changeListener.validateContent();
	}

	protected void setMessage(int level, String message) {
		this.manager.setMessage(level, message);
	}

	protected void setButtonsEnabled(boolean enabled) {

	}

	protected abstract void saveChanges();

	protected abstract void cancelChanges();

	/*
	 * return false if dialog should not be closed override if needed
	 */
	public boolean canClose() {
		return true;
	};

	protected class VerificationKeyListener extends
			AbstractVerificationKeyListener {
		public VerificationKeyListener() {
			super();
		}

		public void hasError(String errorReason) {
			AbstractDialogPanel.this.setMessage(IDialogManager.LEVEL_ERROR,
					errorReason);
			this.handleButtons();
		}

		public void hasWarning(String warningReason) {
			AbstractDialogPanel.this.setMessage(IDialogManager.LEVEL_WARNING,
					warningReason);
			this.handleButtons();
		}

		public void hasNoError() {
			AbstractDialogPanel.this.setMessage(IDialogManager.LEVEL_OK, null);
			this.handleButtons();
		}

		protected void handleButtons() {
			AbstractDialogPanel.this.manager.setButtonEnabled(0, this
					.isFilledRight());
			AbstractDialogPanel.this.setButtonsEnabled(this.isFilledRight());
		}

	}

}
