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

package com.atlassian.connector.eclipse.internal.monitor.usage.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.atlassian.connector.eclipse.commons.core.CoreConstants;
import com.atlassian.connector.eclipse.internal.monitor.core.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.UsageMonitorImages;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;

/**
 * Page to upload the file to the server
 * 
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class UsageUploadWizardPage extends WizardPage {

	// private static final int MAX_NUM_LINES = 1000;

	/** A text box to hold the location of the usage statistics file */
	private Text usageFileText;

	// /** A text box to hold the location of the log file */
	// private Text logFileText;

	private final UsageSubmissionWizard wizard;

	/**
	 * Constructor
	 */
	public UsageUploadWizardPage(UsageSubmissionWizard wizard) {
		super(Messages.UsageUploadWizardPage_page_title);

		setTitle(Messages.UsageUploadWizardPage_title);
		setDescription(Messages.UsageUploadWizardPage_description);

		this.wizard = wizard;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;

		Composite topContainer = new Composite(container, SWT.NULL);
		GridLayout topContainerLayout = new GridLayout();
		topContainer.setLayout(topContainerLayout);
		topContainerLayout.numColumns = 2;
		topContainerLayout.verticalSpacing = 9;

		Label label = new Label(topContainer, SWT.NULL);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);
		label.setText(Messages.UsageUploadWizardPage_recipients);

		Composite uc = new Composite(topContainer, SWT.NONE);
		uc.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(uc);

		new Label(uc, SWT.NONE).setImage(UsageMonitorImages.getImage(UsageMonitorImages.LOGO));

		Link details = new Link(uc, SWT.NULL);
		details.setText(String.format("<A HREF=\"%s\">%s</A>", MonitorCorePlugin.HELP_URL, CoreConstants.PRODUCT_NAME));
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}
		});

		label = new Label(topContainer, SWT.NULL | SWT.BEGINNING);
		label.setText(Messages.UsageUploadWizardPage_usage_file_location);

		usageFileText = new Text(topContainer, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(usageFileText);

		usageFileText.setText(wizard.getMonitorFileName());

		Composite bottomContainer = new Composite(container, SWT.NULL);
		GridLayout bottomContainerLayout = new GridLayout();
		bottomContainer.setLayout(bottomContainerLayout);
		bottomContainerLayout.numColumns = 2;

		setControl(container);
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

}
