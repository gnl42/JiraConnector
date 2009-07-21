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

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Page to get a user study id for the user.
 * 
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class GetNewUserIdPage extends WizardPage {

	private static final String SELECT_BELOW = "<Select Below>";

	private Text firstName;

	private Text lastName;

	private Text emailAddress;

	private Button contactAgreement;

	// private Button anonymous;
	private Button getNewUid;

	private Button getExistingUid;

	private String first;

	private String last;

	private String email;

	private boolean contactEmail = false;

	private boolean anon;

	private boolean hasValidated = false;

	private String jobFunction = SELECT_BELOW;

	private String companySize = SELECT_BELOW;

	private String companyFunction = SELECT_BELOW;

	private final UsageSubmissionWizard wizard;

	private final boolean performUpload;

	private boolean extendedMonitor = false;

	public GetNewUserIdPage(UsageSubmissionWizard wizard, boolean performUpload) {
		super("Statistics Wizard");
		this.performUpload = performUpload;
		setTitle("Get Mylyn Feedback User ID");
		setDescription("In order to submit usage feedback you first need to get a User ID.\n");
		this.wizard = wizard;
		if (UiUsageMonitorPlugin.getDefault().getCustomizingPlugin() != null) {
			extendedMonitor = true;
			String customizedTitle = UiUsageMonitorPlugin.getDefault().getStudyParameters().getTitle();
			if (!customizedTitle.equals("")) {
				setTitle(customizedTitle + ": Consent Form and User ID");
			}
		}
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		if (extendedMonitor) {
			createBrowserSection(container);
			// createAnonymousSection(container);
			createInstructionSection(container);
			createNamesSection(container);
			createJobDetailSection(container);
			if (UiUsageMonitorPlugin.getDefault().usingContactField()) {
				createContactSection(container);
			}
			createUserIdButtons(container);
		} else {
			createAnonymousParticipationButtons(container);
		}
		setControl(container);
	}

	@SuppressWarnings("deprecation")
	private void createBrowserSection(Composite parent) {
		if (extendedMonitor) {
			Label label = new Label(parent, SWT.NULL);
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			label.setText(UiUsageMonitorPlugin.getDefault().getCustomizedByMessage());

			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;
			Browser browser = new Browser(parent, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 200;
			gd.widthHint = 600;
			browser.setLayoutData(gd);

			URL url = Platform.getBundle(UiUsageMonitorPlugin.getDefault().getCustomizingPlugin()).getEntry(
					UiUsageMonitorPlugin.getDefault().getStudyParameters().getFormsConsent());
			try {
				URL localURL = Platform.asLocalURL(url);
				browser.setUrl(localURL.toString());
			} catch (Exception e) {
				browser.setText("Feedback description could not be located.");
			}
		} else {
			Label label = new Label(parent, SWT.NULL);
			label.setText("bla bla");
		}
	}

	// private void createAnonymousSection(Composite parent) {
	// Composite container = new Composite(parent, SWT.NULL);
	// GridLayout layout = new GridLayout();
	// container.setLayout(layout);
	// layout.numColumns = 1;
	//        
	// anonymous = new Button(container, SWT.CHECK);
	// GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	// anonymous.setLayoutData(gd);
	// anonymous.setSelection(false);
	// anonymous.setText("Anonymous (you must still provide your name and email
	// for consent purposes)");
	// anonymous.addSelectionListener(new SelectionListener() {
	// public void widgetSelected(SelectionEvent e) {
	// if (e.widget instanceof Button) {
	// Button b = (Button) e.widget;
	// anon = b.getSelection();
	// updateEnablement();
	// // boolean edit = !anon;
	// // firstName.setEditable(edit);
	// // lastName.setEditable(edit);
	// // emailAddress.setEditable(edit);
	// GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
	// }
	// }
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // don't care about default selection
	// }
	// });
	// }

	private void createNamesSection(Composite parent) {
		Composite names = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(6, true);
		layout.verticalSpacing = 9;
		layout.horizontalSpacing = 4;
		names.setLayout(layout);

		Label label = new Label(names, SWT.NULL);
		label.setText("First Name:");

		firstName = new Text(names, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		firstName.setLayoutData(gd);
		firstName.setEditable(true);
		firstName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				first = firstName.getText();
				updateEnablement();
				GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
			}
		});

		label = new Label(names, SWT.NULL);
		label.setText("Last Name:");

		lastName = new Text(names, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		lastName.setLayoutData(gd);
		lastName.setEditable(true);
		lastName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				last = lastName.getText();
				updateEnablement();
				GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
			}
		});

		label = new Label(names, SWT.NONE);
		label.setText("Email Address:");

		emailAddress = new Text(names, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
		gd.horizontalSpan = 5;
		emailAddress.setLayoutData(gd);
		emailAddress.setEditable(true);
		emailAddress.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				email = emailAddress.getText();
				updateEnablement();
				GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
			}
		});
	}

	private void createJobDetailSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;

		Label l = new Label(container, SWT.NULL);
		l.setText("Job Function:");
		final Combo jobFunctionCombo = new Combo(container, SWT.DROP_DOWN);
		jobFunctionCombo.setText(jobFunction);
		jobFunctionCombo.add("Application Developer");
		jobFunctionCombo.add("QA/Testing");
		jobFunctionCombo.add("Program Director");
		jobFunctionCombo.add("CIO/CTO");
		jobFunctionCombo.add("VP Development Systems Integrator");
		jobFunctionCombo.add("Application Architect");
		jobFunctionCombo.add("Project Manager");
		jobFunctionCombo.add("Student");
		jobFunctionCombo.add("Faculty");
		jobFunctionCombo.add("Business");
		jobFunctionCombo.add("Analyst");
		jobFunctionCombo.add("Database Administrator");
		jobFunctionCombo.add("Other");
		jobFunctionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				jobFunction = jobFunctionCombo.getText();
				updateEnablement();
			}
		});

		l = new Label(container, SWT.NULL);
		l.setText("Company Size:");
		final Combo companySizecombo = new Combo(container, SWT.DROP_DOWN);
		companySizecombo.setText(companySize);
		companySizecombo.add("Individual");
		companySizecombo.add("<50");
		companySizecombo.add("50-100");
		companySizecombo.add("100-500");
		companySizecombo.add("500-1000");
		companySizecombo.add("1000-2500");
		companySizecombo.add(">2500");
		companySizecombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				companySize = companySizecombo.getText();
				updateEnablement();
			}
		});

		l = new Label(container, SWT.NULL);
		l.setText("Company Business");
		final Combo companyBuisnesscombo = new Combo(container, SWT.DROP_DOWN);
		companyBuisnesscombo.setText(companyFunction);
		companyBuisnesscombo.add("Financial service/insurance");
		companyBuisnesscombo.add("Energy");
		companyBuisnesscombo.add("Government");
		companyBuisnesscombo.add("Hardware Manufacturer");
		companyBuisnesscombo.add("Networking");
		companyBuisnesscombo.add("Pharmaceutical/Medical");
		companyBuisnesscombo.add("Automotive");
		companyBuisnesscombo.add("Software Manufacturer");
		companyBuisnesscombo.add("Communications");
		companyBuisnesscombo.add("Transportation");
		companyBuisnesscombo.add("Retail");
		companyBuisnesscombo.add("Utilities");
		companyBuisnesscombo.add("Other Manufacturing");
		companyBuisnesscombo.add("Academic/Education");
		companyBuisnesscombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				companyFunction = companyBuisnesscombo.getText();
				updateEnablement();
			}
		});
	}

	private void createInstructionSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		Label l = new Label(container, SWT.NONE);
		// l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		l.setText("To create a user ID please fill in the following information. If you already have an ID please fill out the information again to retrieve it.");

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		l.setLayoutData(gd);
	}

	private void createContactSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		contactAgreement = new Button(container, SWT.CHECK);
		contactAgreement.setText("I would be willing to receive email about my participation in this study.");
		contactAgreement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				contactEmail = contactAgreement.getSelection();
			}
		});
	}

	private void createUserIdButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		Label l = new Label(container, SWT.NONE);
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		l.setText("By clicking \"I consent\" you acknowledge that you have received this consent form, and are consenting to participate in the study.");
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		l.setLayoutData(gd);

		container = new Composite(parent, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		getNewUid = new Button(container, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		getNewUid.setLayoutData(gd);
		getNewUid.setSelection(false);
		getNewUid.setText("I consent; get me a new user ID");
		getNewUid.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					if (hasAllFields(false)) {
						if (wizard.getNewUid(first, last, email, anon, jobFunction, companySize, companyFunction,
								contactEmail) != -1) {
							if (wizard.getUploadPage() != null) {
								wizard.getUploadPage().updateUid();
							}
							hasValidated = true;
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Mylyn User Study ID",
									"Your Mylyn user study ID is: " + wizard.getUid());
						}
					} else {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Incomplete Form Input",
								"Please complete all of the fields.");
					}
					GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about default selected
			}
		});

		getExistingUid = new Button(container, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		getExistingUid.setLayoutData(gd);
		getExistingUid.setSelection(false);
		getExistingUid.setText("I have already consented; retrieve my existing user ID");
		getExistingUid.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					if (hasAllFields(true)) {
						if (wizard.getExistingUid(first, last, email, anon) != -1) {
							if (wizard.getUploadPage() != null) {
								wizard.getUploadPage().updateUid();
							}
							hasValidated = true;
							MessageDialog.openInformation(
									Display.getDefault().getActiveShell(),
									"Mylyn Feedback User ID",
									"Your Mylyn feedback ID is: "
											+ wizard.getUid()
											+ "\n\nPlease record this number if you are using multiple copies of eclipse so that you do not have to register again.\n\nYou can also retrieve this ID by repeating the consent process at a later time.");
						}
					} else {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Incomplete Form Input",
								"Please complete all of the fields.");
					}
					GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about default selected
			}
		});

		updateEnablement();
	}

	private void createAnonymousParticipationButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NONE);
		label.setText("Your data will not be traceable back to you, but an ID helps us analyze the usage statistics.");
		label = new Label(container, SWT.NONE);
		label.setText("Before switching workspaces please retrieve this ID from the Mylyn Preferences so that you can use it again.");
		// GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		// label.setLayoutData(gd);

		container = new Composite(parent, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		getNewUid = new Button(container, SWT.PUSH);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		getNewUid.setLayoutData(gd);
		getNewUid.setSelection(false);
		getNewUid.setText("Create or Retrieve ID");
		getNewUid.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					if (wizard.getNewUid(null, null, null, true, null, null, null, false) != -1) {
						if (wizard.getUploadPage() != null) {
							wizard.getUploadPage().updateUid();
						}
						hasValidated = true;
						MessageDialog.openInformation(
								Display.getDefault().getActiveShell(),
								"Mylyn User Study ID",
								"Your Mylyn user study id is: "
										+ wizard.getUid()
										+ "\n Please record this number if you are using multiple copies of eclipse so that you do not have to register again.");
					}
					GetNewUserIdPage.this.setPageComplete(GetNewUserIdPage.this.isPageComplete());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// don't care about default selected
			}
		});
		updateEnablement();
	}

	private void updateEnablement() {
		if (!extendedMonitor) {
			return;
		}
		boolean nameFilled = (!firstName.getText().equals("") && !lastName.getText().equals("") && !emailAddress.getText()
				.equals(""))
				|| anon;
		// if(nameFilled){
		// getExistingUid.setEnabled(true);
		boolean jobFilled = !jobFunction.equals(SELECT_BELOW) && !companyFunction.equals(SELECT_BELOW)
				&& !companySize.equals(SELECT_BELOW);
		// if(jobFilled){
		// getNewUid.setEnabled(true);
		// } else {
		// getNewUid.setEnabled(false);
		// }
		if (nameFilled && jobFilled) {
			getNewUid.setEnabled(true);
			getExistingUid.setEnabled(true);
		} else {
			getExistingUid.setEnabled(false);
			getNewUid.setEnabled(false);
		}
	}

	public boolean hasAllFields(boolean existing) {
		if (!extendedMonitor) {
			return true;
		}
		boolean nameFilled = !firstName.getText().equals("") && !lastName.getText().equals("")
				&& !emailAddress.getText().equals("");
		if (!existing) {
			boolean jobFilled = !jobFunction.equals(SELECT_BELOW) && !companyFunction.equals(SELECT_BELOW)
					&& !companySize.equals(SELECT_BELOW);
			return (jobFilled && nameFilled);
		} else {
			return nameFilled || anon;
		}
	}

	@Override
	public boolean isPageComplete() {
		if (hasAllFields(true) && hasValidated) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IWizardPage getNextPage() {
		if (isPageComplete() && performUpload) {
			wizard.addPage(wizard.getUploadPage());
		}

		return super.getNextPage();

	}

	public boolean isAnonymous() {
		return anon;
	}

	public String getEmailAddress() {
		return email;
	}

	public String getFirstName() {
		return first;
	}

	public String getLastName() {
		return last;
	}
}
