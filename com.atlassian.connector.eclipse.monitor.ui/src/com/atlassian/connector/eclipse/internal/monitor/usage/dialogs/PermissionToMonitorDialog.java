/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.UsageMonitorImages;
import com.atlassian.connector.eclipse.internal.ui.IBrandingConstants;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;

public class PermissionToMonitorDialog extends Dialog {

	public PermissionToMonitorDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.EnabledMonitoringNoticeDialog_title);

		Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		new Label(composite, SWT.NONE).setImage(UsageMonitorImages.getImage(UsageMonitorImages.LOGO));

		Label messageLabel = new Label(composite, SWT.WRAP);
		messageLabel.setText(Messages.EnabledMonitoringNoticeDialog_please_consider_uploading);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).hint(
				convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT).applyTo(
				messageLabel);

		Link details = new Link(composite, SWT.NULL);
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(details);

		details.setText(NLS.bind(Messages.EnabledMonitoringNoticeDialog_learn_more, MonitorCorePlugin.HELP_URL,
				IBrandingConstants.PRODUCT_NAME));
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}
		});

		Link disable = new Link(composite, SWT.WRAP);
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(disable);

		disable.setText(Messages.EnabledMonitoringNoticeDialog_to_disable);
		disable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
				PreferencesUtil.createPreferenceDialogOn(WorkbenchUtil.getShell(),
						"com.atlassian.connector.eclipse.monitor.usage.preferences", null, null).open();
			};
		});

		messageLabel = new Label(composite, SWT.WRAP);
		messageLabel.setText("Enable monitoring?");
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(messageLabel);

		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}

}
