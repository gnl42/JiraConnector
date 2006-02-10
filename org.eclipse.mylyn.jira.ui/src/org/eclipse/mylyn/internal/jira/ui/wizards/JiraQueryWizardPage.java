/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.tigris.jira.core.model.NamedFilter;
import org.tigris.jira.core.service.JiraServer;

/**
 * Wizard page that allows the user to select a named Jira filter they have
 * defined on the server.
 * 
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraQueryWizardPage extends WizardPage {

	private static final int COMBO_WIDTH_HINT = 200;

	private static final String TITLE = "New Jira Query";

	private static final String DESCRIPTION = "Please select a filter defined on the server.";

	private static final String COMBO_LABEL = "Filter:";

	private static final String WAIT_MESSAGE = "Downloading...";

	private static final String JOB_LABEL = "Downloading Filter Names";

	private NamedFilter[] filters = null;

	private Combo filterCombo = null;

	public JiraQueryWizardPage(TaskRepository repository) {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {

		Composite innerComposite = new Composite(parent, SWT.NONE);
		innerComposite.setLayoutData(new GridData());
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		innerComposite.setLayout(gl);

		Label label = new Label(innerComposite, SWT.NONE);
		label.setText(COMBO_LABEL);
		label.setLayoutData(new GridData());

		filterCombo = new Combo(innerComposite, SWT.READ_ONLY);
		filterCombo.add(WAIT_MESSAGE);
		filterCombo.select(0);

		GridData data = new GridData();
		data.widthHint = COMBO_WIDTH_HINT;
		filterCombo.setLayoutData(data);

		setControl(innerComposite);
		downloadFilters();
	}

	protected void downloadFilters() {

		Job j = new Job(JOB_LABEL) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer();
					filters = jiraServer.getNamedFilters();

					monitor.worked(1);
					monitor.done();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							displayFilters(filters);
						}
					});
				} catch (Exception e) {
					JiraServerFacade.handleConnectionException(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};

		j.schedule();

	}

	/** Called by the download job when the filters have been downloaded */
	public void displayFilters(NamedFilter[] filters) {
		if (filters.length == 0) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No Filters Found",
					"Please specify a filter using your Jira web interface");
			return;
		}

		filterCombo.removeAll();

		for (int i = 0; i < filters.length; i++) {
			filterCombo.add(filters[i].getName());
		}

		filterCombo.select(0);
		setPageComplete(true);
	}

	/** Returns the filter selected by the user or null on failure */
	public NamedFilter getSelectedFilter() {
		if (filters != null && filters.length > 0) {
			return filters[filterCombo.getSelectionIndex()];
		}
		return null;
	}

}
