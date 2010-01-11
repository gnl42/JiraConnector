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

package com.atlassian.connector.eclipse.internal.fisheye.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddRepositoryUrlMappingDialog extends TitleAreaDialog {

	private final String repositoryName;

	private Button okButton;

	private String scmPath;

	public AddRepositoryUrlMappingDialog(@NotNull Shell parentShell, @NotNull String repositoryName, @Nullable String repositoryUrl) {
		super(parentShell);
		this.repositoryName = repositoryName;
		this.scmPath = repositoryUrl;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Crucible Action");
		setTitle("Define Crucible Repository Mapping");
		setMessage(NLS.bind("Please define a mapping between Crucible repository {0} and SCM URL.", repositoryName));
		setHelpAvailable(false);

		Composite composite = (Composite) super.createDialogArea(parent);

		Composite pageContainer = new Composite(composite, SWT.NONE);
		pageContainer.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		pageContainer.setLayoutData(gd);
		pageContainer.setFont(parent.getFont());

		Label label = new Label(pageContainer, SWT.WRAP);
		label.setText(NLS.bind("To proceed with the action please define what SCM URL Crucible repository {0} is associated with.", repositoryName));
		GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).grab(true, false).applyTo(label);

		Composite editComposite = new Composite(pageContainer, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(editComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(editComposite);

		label = new Label(editComposite, SWT.SINGLE);
		label.setText("SCM Path:");

		final Text text = new Text(editComposite, SWT.SINGLE | SWT.BORDER);
		if (scmPath != null) {
			text.setText(scmPath);
		}
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scmPath = text.getText();
				updateOkButtonState();
			}
		});
		GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).grab(true, false).applyTo(text);

		label = new Label(pageContainer, SWT.WRAP);
		label.setText("The SCM URL you provide should be accessible from this machine. If you provide wrong URL action will fail and you will need to edit it in preferences.");
		GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).grab(true, false).applyTo(label);

		applyDialogFont(composite);

		return composite;
	}

	private void updateOkButtonState() {
		boolean isEnabled = (scmPath != null && scmPath.length() > 0);
		okButton.setEnabled(isEnabled);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		updateOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public String getScmPath() {
		return scmPath;
	}
}
