/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.composite;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.debugmail.IMailSettingsProvider;
import com.atlassian.theplugin.eclipse.ui.debugmail.Reporter;
import com.atlassian.theplugin.eclipse.ui.dialog.DefaultDialog;
import com.atlassian.theplugin.eclipse.ui.panel.reporting.PreviewErrorReportPanel;
import com.atlassian.theplugin.eclipse.ui.panel.reporting.PreviewReportPanel;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.ui.utility.UserInputHistory;
import com.atlassian.theplugin.eclipse.ui.verifier.IValidationManager;
import com.atlassian.theplugin.eclipse.ui.verifier.NonEmptyFieldVerifier;
import com.atlassian.theplugin.eclipse.util.StringId;

/**
 * Reporting form composite
 * 
 * @author Alexander Gurov
 */
public class ReportingComposite extends Composite {
	public static final String MAIL_HISTORY = "mailHistory";
	public static final String USER_NAME_HISTORY = "userNameHistory";

	protected UserInputHistory mailHistory;
	protected UserInputHistory userNameHistory;

	protected Combo providersCombo;
	protected Text emailText;
	protected Text nameText;
	protected Text commentText;
	protected Button doNotShowAgainButton;

	protected boolean doNotShowAgain;
	protected String comment;
	protected String email;
	protected String name;
	protected String reportId;
	protected String report;

	protected String reportType;
	protected boolean isError;
	protected String pluginId;
	protected IStatus status;
	protected IMailSettingsProvider[] providers;
	protected int selectedProviderIdx;

	public ReportingComposite(Composite parent, String reportType,
			String pluginId, IStatus status, String optionName,
			boolean isError, IValidationManager manager) {
		super(parent, SWT.NONE);
		this.isError = isError;
		this.reportType = reportType;
		this.pluginId = pluginId;
		this.status = status;
		this.reportId = StringId.generateRandom("Report ID", 5);
		this.providers = null; // ExtensionsManager.getInstance().
								// getMailSettingsProviders();
		this.selectedProviderIdx = 0;
		this.createControls(optionName, manager);
	}

	public IMailSettingsProvider getMailSettingsProvider() {
		return this.providers[this.selectedProviderIdx];
	}

	public String getComment() {
		return this.comment;
	}

	public String getEmail() {
		return this.email;
	}

	public String getUserName() {
		return this.name;
	}

	public String getReportId() {
		return this.reportId;
	}

	public boolean isNotShowAgain() {
		return this.doNotShowAgain;
	}

	public void saveChanges() {
		this.comment = this.commentText.getText().trim();
		this.name = this.nameText.getText().trim();
		this.email = this.emailText.getText().trim();
		if (this.email.length() > 0) {
			this.mailHistory.addLine(this.email);
		} else {
			this.mailHistory.clear();
		}
		if (this.name.length() > 0) {
			this.userNameHistory.addLine(this.name);
		} else {
			this.userNameHistory.clear();
		}
		this.doNotShowAgain = this.doNotShowAgainButton.getSelection();
		this.report = this.saveReport();
	}

	public void cancelChanges() {
		this.doNotShowAgain = this.doNotShowAgainButton.getSelection();
	}

	private void createControls(String optionName, IValidationManager manager) {
		GridLayout layout = null;
		GridData data = null;

		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		this.setLayout(layout);

		Composite composite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(data);

		Label description1 = new Label(composite, SWT.NONE);
		data = new GridData();
		description1.setLayoutData(data);
		description1.setText(Activator.getDefault().getResource(
				"ReportingComposite.Product"));

		this.providersCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.providersCombo.setLayoutData(data);
		Arrays.sort(this.providers, new Comparator<IMailSettingsProvider>() {
			public int compare(IMailSettingsProvider first,
					IMailSettingsProvider second) {
				return first.getPluginName().compareTo(second.getPluginName());
			}
		});
		String[] names = new String[this.providers.length];
		for (int i = 0; i < this.providers.length; i++) {
			names[i] = this.providers[i].getPluginName();
		}
		this.providersCombo.setItems(names);
		this.providersCombo.select(0);
		this.providersCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ReportingComposite.this.selectedProviderIdx = ReportingComposite.this.providersCombo
						.getSelectionIndex();
			}
		});

		Label description = new Label(this, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = DefaultDialog.convertHeightInCharsToPixels(this,
				this.isError ? 4 : 3);
		description.setLayoutData(data);
		description.setText(Activator.getDefault().getResource(
				this.isError ? "ReportingComposite.ErrorHint"
						: "ReportingComposite.Hint"));

		composite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(data);

		this.mailHistory = new UserInputHistory(
				ReportingComposite.MAIL_HISTORY, 1);
		this.userNameHistory = new UserInputHistory(
				ReportingComposite.USER_NAME_HISTORY, 1);

		Label description2 = new Label(composite, SWT.NONE);
		data = new GridData();
		description2.setLayoutData(data);
		description2.setText(Activator.getDefault().getResource(
				"ReportingComposite.EMail"));

		this.emailText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.emailText.setLayoutData(data);
		this.emailText.setFocus();
		String[] mailName = this.mailHistory.getHistory();
		if (mailName != null && mailName.length > 0) {
			this.emailText.setText(mailName[0]);
		}

		Label description3 = new Label(composite, SWT.NONE);
		data = new GridData();
		description3.setLayoutData(data);
		description3.setText(Activator.getDefault().getResource(
				"ReportingComposite.Name"));

		int widthHint = Math.max(description1.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).x, Math.max(description2.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).x, description3.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).x));
		((GridData) description1.getLayoutData()).widthHint = widthHint;
		((GridData) description2.getLayoutData()).widthHint = widthHint;
		((GridData) description3.getLayoutData()).widthHint = widthHint;

		this.nameText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.nameText.setLayoutData(data);
		String[] userName = this.userNameHistory.getHistory();
		if (userName != null && userName.length > 0) {
			this.nameText.setText(userName[0]);
		}

		Label commentLabel = new Label(this, SWT.LEFT);
		data = new GridData();
		commentLabel.setLayoutData(data);
		commentLabel.setText(Activator.getDefault().getResource(
				"ReportingComposite.Comment"));

		this.commentText = new Text(this, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER
				| SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 100;
		this.commentText.setLayoutData(data);
		if (mailName != null && mailName.length > 0) {
			this.commentText.setFocus();
		}
		if (manager != null) {
			manager.attachTo(this.commentText, new NonEmptyFieldVerifier(
					Activator.getDefault().getResource(
							"ReportingComposite.Comment.Verifier")));
		}

		Composite buttonsComposite = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.numColumns = 2;
		buttonsComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		buttonsComposite.setLayoutData(data);

		this.doNotShowAgainButton = new Button(buttonsComposite, SWT.CHECK);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.doNotShowAgainButton.setLayoutData(data);
		this.doNotShowAgainButton.setSelection(false);
		if (optionName != null) {
			this.doNotShowAgainButton.setText(optionName);
		} else {
			this.doNotShowAgainButton.setVisible(false);
		}

		Button previewButton = new Button(buttonsComposite, SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.FILL_HORIZONTAL);
		previewButton.setText(Activator.getDefault().getResource(
				"ReportingComposite.Preview"));
		data.widthHint = DefaultDialog.computeButtonWidth(previewButton);
		previewButton.setLayoutData(data);

		previewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreviewReportPanel panel = null;
				if (ReportingComposite.this.isError) {
					String report = Reporter.formReport(ReportingComposite.this
							.getMailSettingsProvider(),
							ReportingComposite.this.status,
							ReportingComposite.this.pluginId,
							ReportingComposite.this.commentText.getText(),
							ReportingComposite.this.emailText.getText(),
							ReportingComposite.this.nameText.getText(),
							ReportingComposite.this.reportId);
					panel = new PreviewErrorReportPanel(report);
				} else {
					String report = Reporter.formReport(ReportingComposite.this
							.getMailSettingsProvider(),
							ReportingComposite.this.commentText.getText(),
							ReportingComposite.this.emailText.getText(),
							ReportingComposite.this.nameText.getText(),
							ReportingComposite.this.reportId,
							ReportingComposite.this.isError);
					String msg = Activator.getDefault().getResource(
							"ReportingComposite.Preview.Title");
					panel = new PreviewReportPanel(
							MessageFormat
									.format(
											msg,
											new Object[] { ReportingComposite.this.reportType }),
							report);
				}
				DefaultDialog dialog = new DefaultDialog(UIMonitorUtil
						.getDisplay().getActiveShell(), panel);
				dialog.open();
			}
		});
	}

	public String saveReport() {
		String report = null;
		if (ReportingComposite.this.isError) {
			report = Reporter.formReport(ReportingComposite.this
					.getMailSettingsProvider(), ReportingComposite.this.status,
					ReportingComposite.this.pluginId,
					ReportingComposite.this.commentText.getText(),
					ReportingComposite.this.emailText.getText(),
					ReportingComposite.this.nameText.getText(),
					ReportingComposite.this.reportId);
		} else {
			report = Reporter.formReport(ReportingComposite.this
					.getMailSettingsProvider(),
					ReportingComposite.this.commentText.getText(),
					ReportingComposite.this.emailText.getText(),
					ReportingComposite.this.nameText.getText(),
					ReportingComposite.this.reportId,
					ReportingComposite.this.isError);
		}
		return report;
	}

	public String getReport() {
		return this.report;
	}

}
