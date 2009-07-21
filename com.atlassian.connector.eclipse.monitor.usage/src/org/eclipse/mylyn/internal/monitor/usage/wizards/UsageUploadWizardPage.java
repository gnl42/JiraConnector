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

package org.eclipse.mylyn.internal.monitor.usage.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Page to upload the file to the server
 * 
 * @author Shawn Minto
 * @author Mik Kersten
 */
public class UsageUploadWizardPage extends WizardPage {

	// private static final int MAX_NUM_LINES = 1000;

	/** A text box to hold the address of the server */
	private Text serverAddrText;

	/** A text box to hold the location of the usage statistics file */
	private Text usageFileText;

	// /** A text box to hold the location of the log file */
	// private Text logFileText;

	/** A text file to show the id of the user */
	private Text idText;

	private final UsageSubmissionWizard wizard;

	/**
	 * Constructor
	 */
	public UsageUploadWizardPage(UsageSubmissionWizard wizard) {
		super("Usage Data Submission Wizard");

		setTitle("Usage Data Submission");
		if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
			String customizedTitle = UiUsageMonitorPlugin.getDefault().getStudyParameters().getTitle();
			if (!customizedTitle.equals("")) {
				setTitle(customizedTitle + ": Usage Data Upload");
			}
		}

		setDescription("The usage file listed below will be uploaded along with the archived files you selected (there may not have been any to select from).\n"
				+ "Information about program elements that you worked with is obfuscated to ensure privacy.");
		// setDescription(
		// "The files listed below will be uploaded. Information about program
		// elements that you "
		// + "worked with is obfuscated to ensure privacy.");
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

		Label label;
		if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
			label = new Label(parent, SWT.NULL);
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			label.setText(UiUsageMonitorPlugin.getDefault().getCustomizedByMessage());
		}

		label = new Label(topContainer, SWT.NULL);
		label.setText("Upload URL:");

		serverAddrText = new Text(topContainer, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		serverAddrText.setLayoutData(gd);
		serverAddrText.setEditable(false);
		serverAddrText.setText(UiUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl());

		label = new Label(topContainer, SWT.NULL);
		label.setText("Usage file location:");

		usageFileText = new Text(topContainer, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		usageFileText.setLayoutData(gd);
		usageFileText.setEditable(false);

		usageFileText.setText(wizard.getMonitorFileName());

		Composite bottomContainer = new Composite(container, SWT.NULL);
		GridLayout bottomContainerLayout = new GridLayout();
		bottomContainer.setLayout(bottomContainerLayout);
		bottomContainerLayout.numColumns = 2;

		Label submissionLabel = new Label(bottomContainer, SWT.NONE);
		submissionLabel.setText("Only events from org.eclipse.* packages will be submitted to Eclipse.org");

		setControl(container);
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

	public void updateUid() {
		if (idText != null && !idText.isDisposed()) {
			idText.setText(wizard.getUid() + "");
		}
	}
}
