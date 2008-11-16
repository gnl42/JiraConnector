/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.utility.UserInputHistory;

/**
 * Credentials composite
 * 
 * @author Sergiy Logvin
 */
public class CredentialsComposite extends Composite {
	protected static final String USER_HISTORY_NAME = "repositoryUser";

	protected Combo userName;
	protected Text password;
	protected Button savePassword;

	protected String usernameInput;
	protected String passwordInput;
	protected boolean passwordSaved;

	protected UserInputHistory userHistory;

	public CredentialsComposite(Composite parent, int style) {
		super(parent, style);
		this.createControls();
	}

	public void initialize() {
		if (this.usernameInput != null
				&& this.usernameInput.trim().length() > 0) {
			this.userName.setText(this.usernameInput);
		} else {
			this.userName.setFocus();
		}

		if (this.passwordInput != null) {
			this.password.setText(this.passwordInput);
		}
		if (this.usernameInput != null
				&& this.usernameInput.trim().length() > 0) {
			this.password.setFocus();
			this.password.selectAll();
		}

		this.savePassword.setSelection(this.passwordSaved);
	}

	public Text getPassword() {
		return this.password;
	}

	public Button getSavePassword() {
		return this.savePassword;
	}

	public UserInputHistory getUserHistory() {
		return this.userHistory;
	}

	public void setUserHistory(UserInputHistory userHistory) {
		this.userHistory = userHistory;
	}

	public Combo getUsername() {
		return this.userName;
	}

	public void setPasswordInput(String passwordInput) {
		this.passwordInput = passwordInput;
	}

	public void setPasswordSaved(boolean passwordSaved) {
		this.passwordSaved = passwordSaved;
	}

	public void setUsernameInput(String usernameInput) {
		this.usernameInput = usernameInput;
	}

	private void createControls() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);

		Group authGroup = new Group(this, SWT.NULL);
		layout = new GridLayout();
		layout.verticalSpacing = 12;
		authGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 2;
		authGroup.setLayoutData(data);
		authGroup.setText(Activator.getDefault().getResource(
				"CredentialsComposite.Authentication"));

		Composite inner = new Composite(authGroup, SWT.FILL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		inner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		inner.setLayoutData(data);

		Label description = new Label(inner, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = false;
		data.horizontalIndent = 0;
		description.setLayoutData(data);
		description.setText(Activator.getDefault().getResource(
				"CredentialsComposite.User"));

		this.userHistory = new UserInputHistory(
				CredentialsComposite.USER_HISTORY_NAME);

		this.userName = new Combo(inner, SWT.DROP_DOWN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.userName.setLayoutData(data);
		this.userName.setVisibleItemCount(this.userHistory.getDepth());
		this.userName.setItems(this.userHistory.getHistory());

		description = new Label(inner, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = false;
		data.horizontalIndent = 0;
		description.setLayoutData(data);
		description.setText(Activator.getDefault().getResource(
				"CredentialsComposite.Password"));

		this.password = new Text(inner, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		this.password.setLayoutData(data);

		inner = new Composite(authGroup, SWT.FILL);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		inner.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		inner.setLayoutData(data);

		this.savePassword = new Button(inner, SWT.CHECK);
		data = new GridData();
		this.savePassword.setLayoutData(data);
		this.savePassword.setText(Activator.getDefault().getResource(
				"CredentialsComposite.SavePassword"));

		new SecurityWarningComposite(inner);
	}

}
