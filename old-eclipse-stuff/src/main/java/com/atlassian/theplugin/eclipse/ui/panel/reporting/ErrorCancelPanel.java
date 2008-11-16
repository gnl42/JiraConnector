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

package com.atlassian.theplugin.eclipse.ui.panel.reporting;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.composite.ReportingComposite;
import com.atlassian.theplugin.eclipse.ui.debugmail.IMailSettingsProvider;
import com.atlassian.theplugin.eclipse.ui.dialog.DefaultDialog;
import com.atlassian.theplugin.eclipse.ui.panel.AbstractAdvancedDialogPanel;
import com.atlassian.theplugin.eclipse.ui.panel.IDialogManagerEx;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;

/**
 * Error or cancel panel
 * 
 * @author Sergiy Logvin
 */
public class ErrorCancelPanel extends AbstractAdvancedDialogPanel {
	private static final int A_100 = 100;
	protected static final int ERROR_PANEL_TYPE = 0;
	protected static final int CANCEL_PANEL_TYPE = 1;

	protected ReportingComposite reportingComposite;

	protected Text errorTextField;
	protected String optionName;
	protected String simpleMessage;
	protected String advancedMessage;
	protected boolean isSimple;
	protected int panelType;
	protected boolean sendMail;

	protected IStatus errorStatus;
	protected String plugin;

	protected String originalReport;

	public ErrorCancelPanel(String title, int numberOfErrors,
			String simpleMessage, String advancedMessage, boolean sendMail,
			String optionName) {
		this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title,
				simpleMessage, advancedMessage, sendMail, optionName);
	}

	public ErrorCancelPanel(String title, String simpleMessage,
			String advancedMessage, boolean sendMail, String optionName) {
		this(ErrorCancelPanel.CANCEL_PANEL_TYPE, 0, title, simpleMessage,
				advancedMessage, sendMail, optionName);
	}

	public ErrorCancelPanel(String title, int numberOfErrors,
			String simpleMessage, String advancedMessage, boolean sendMail,
			String optionName, IStatus errorStatus, String plugin) {
		this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title,
				simpleMessage, advancedMessage, sendMail, optionName);
		this.errorStatus = errorStatus;
		this.plugin = plugin;
	}

	public ErrorCancelPanel(String title, int numberOfErrors,
			String simpleMessage, String advancedMessage, boolean sendMail,
			String optionName, IStatus errorStatus, String plugin,
			String originalReport) {
		this(ErrorCancelPanel.ERROR_PANEL_TYPE, numberOfErrors, title,
				simpleMessage, advancedMessage, sendMail, optionName);
		this.errorStatus = errorStatus;
		this.plugin = plugin;
		this.originalReport = originalReport;
	}

	protected ErrorCancelPanel(int panelType, int numberOfErrors, String title,
			String simpleMessage, String advancedMessage, boolean sendMail,
			String optionName) {
		super(
				sendMail ? new String[] {
						Activator.getDefault().getResource(
								"ErrorCancelPanel.Send"),
						Activator.getDefault().getResource(
								"ErrorCancelPanel.DontSend") }
						: new String[] { IDialogConstants.OK_LABEL },
				new String[] { Activator.getDefault().getResource(
						"Button.Advanced") });
		this.panelType = panelType;
		this.sendMail = sendMail;
		this.dialogTitle = Activator
				.getDefault()
				.getResource(
						panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Title.Failed"
								: "ErrorCancelPanel.Title.Cancelled");
		if (title == null || title.length() == 0) {
			this.dialogDescription = Activator
					.getDefault()
					.getResource(
							panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Description.Failed.Empty"
									: "ErrorCancelPanel.Description.Cancelled.Empty");
		} else {
			this.dialogDescription = Activator
					.getDefault()
					.getResource(
							panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "ErrorCancelPanel.Description.Failed"
									: "ErrorCancelPanel.Description.Cancelled");
			this.dialogDescription = MessageFormat.format(
					this.dialogDescription, new Object[] { title });
		}
		if (sendMail) {
			this.defaultMessage = Activator.getDefault().getResource(
					"ErrorCancelPanel.Message.Send");
		} else {
			if (panelType == ErrorCancelPanel.ERROR_PANEL_TYPE) {
				if (numberOfErrors == 1) {
					this.defaultMessage = Activator.getDefault().getResource(
							"ErrorCancelPanel.Message.DontSend.Single");
				} else {
					this.defaultMessage = Activator.getDefault().getResource(
							"ErrorCancelPanel.Message.DontSend.Multi");
					this.defaultMessage = MessageFormat.format(
							this.defaultMessage, new Object[] { String
									.valueOf(numberOfErrors) });
				}
			} else {
				this.defaultMessage = Activator.getDefault().getResource(
						"ErrorCancelPanel.Message.DontSend");
			}
		}

		this.simpleMessage = simpleMessage == null ? Activator.getDefault()
				.getResource("ErrorCancelPanel.NoInfo") : simpleMessage;
		this.advancedMessage = advancedMessage == null ? Activator.getDefault()
				.getResource("ErrorCancelPanel.NoAdvancedInfo")
				: advancedMessage;
		this.isSimple = false;
		this.optionName = optionName;
	}

	public IMailSettingsProvider getMailSettingsProvider() {
		return this.reportingComposite.getMailSettingsProvider();
	}

	public boolean doNotShowAgain() {
		return this.reportingComposite != null ? this.reportingComposite
				.isNotShowAgain() : false;
	}

	public void createControls(Composite parent) {
		GridData data = null;
		this.errorTextField = new Text(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = A_100;
		this.errorTextField.setLayoutData(data);
		this.errorTextField.setEditable(false);
		this.errorTextField.setText(this.simpleMessage);

		if (this.sendMail) {
			final Composite mailComposite = new Composite(parent, SWT.FILL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			data = new GridData(GridData.FILL_BOTH);
			mailComposite.setLayout(layout);
			mailComposite.setLayoutData(data);

			Label separator = new Label(mailComposite, SWT.SEPARATOR
					| SWT.HORIZONTAL);
			data = new GridData(GridData.FILL_HORIZONTAL);
			separator.setLayoutData(data);

			this.reportingComposite = new ReportingComposite(parent,
					this.dialogTitle, this.plugin, this.errorStatus,
					this.optionName, true, null);
			data = new GridData(GridData.FILL_BOTH);
			this.reportingComposite.setLayoutData(data);
		} else {
			if (this.originalReport != null) {
				Button viewButton = new Button(parent, SWT.PUSH);
				viewButton.setText(Activator.getDefault().getResource(
						"ErrorCancelPanel.OriginalReport"));
				data = new GridData();
				data.widthHint = DefaultDialog.computeButtonWidth(viewButton);
				viewButton.setLayoutData(data);
				viewButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						PreviewReportPanel panel = new PreviewReportPanel(
								Activator
										.getDefault()
										.getResource(
												"ErrorCancelPanel.OriginalReportPreview"),
								ErrorCancelPanel.this.originalReport);
						DefaultDialog dialog = new DefaultDialog(UIMonitorUtil
								.getDisplay().getActiveShell(), panel);
						dialog.open();
					}
				});
			}
		}
	}

	public String getHelpId() {
		return "org.eclipse.team.svn.help.errorDialogContext";
	}

	protected void saveChanges() {
		if (this.sendMail) {
			this.reportingComposite.saveChanges();
		}
	}

	protected void cancelChanges() {
		if (this.sendMail) {
			this.reportingComposite.cancelChanges();
		}
	}

	protected void showDetails() {

	}

	public void postInit() {
		super.postInit();
		((IDialogManagerEx) this.manager)
				.setExtendedButtonEnabled(0, this.simpleMessage
						.equalsIgnoreCase(this.advancedMessage) ? false : true);
	}

	public String getImagePath() {
		return "icons/dialogs/"
				+ (this.panelType == ErrorCancelPanel.ERROR_PANEL_TYPE ? "operation_error.gif"
						: "select_revision.gif");
	}

	public void extendedButtonPressed(int idx) {
		if (this.isSimple) {
			((IDialogManagerEx) this.manager).setExtendedButtonCaption(idx,
					Activator.getDefault().getResource("Button.Advanced"));
			this.errorTextField.setText(this.simpleMessage);
			this.isSimple = false;
		} else {
			((IDialogManagerEx) this.manager).setExtendedButtonCaption(idx,
					Activator.getDefault().getResource("Button.Simple"));
			this.errorTextField.setText(this.advancedMessage);
			this.isSimple = true;
		}
	}

	public String getComment() {
		return this.reportingComposite.getComment();
	}

	public String getEmail() {
		return this.reportingComposite.getEmail();
	}

	public String getName() {
		return this.reportingComposite.getUserName();
	}

	public String getReportId() {
		return this.reportingComposite.getReportId();
	}

	public String getReport() {
		return this.reportingComposite.getReport();
	}

}
