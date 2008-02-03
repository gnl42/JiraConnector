/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.ui.JiraRepositoryQuery;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.search.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * Wizard page that allows the user to select a named Jira filter they have defined on the server.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Eugene Kuleshov (layout and other improvements)
 * @author Steffen Pingel
 */
public class JiraQueryWizardPage extends AbstractRepositoryQueryPage {

	private static final String TITLE = "New Jira Query";

	private static final String DESCRIPTION = "Please select a query type.";

	private static final String WAIT_MESSAGE = "Downloading...";

	private static final String JOB_LABEL = "Downloading Filter Names";

	private NamedFilter[] filters = null;

	private List filterList;

	private Button updateButton = null;

	private Button buttonCustom;

	private Button buttonSaved;

	private JiraQueryPage filterSummaryPage;

	private AbstractRepositoryQuery query;

	public JiraQueryWizardPage(TaskRepository repository) {
		this(repository, null);
	}

	public JiraQueryWizardPage(TaskRepository repository, AbstractRepositoryQuery query) {
		super(TITLE);
		this.repository = repository;
		this.query = query;
		setTitle(TITLE);
		setDescription(DESCRIPTION);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		boolean isCustom = query == null || query instanceof JiraCustomQuery;
		boolean isRepository = query instanceof JiraRepositoryQuery;

		final Composite innerComposite = new Composite(parent, SWT.NONE);
		innerComposite.setLayoutData(new GridData());
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		innerComposite.setLayout(gl);

		buttonCustom = new Button(innerComposite, SWT.RADIO);
		buttonCustom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonCustom.setText("&Create query using form");
		buttonCustom.setSelection(isCustom);

		buttonSaved = new Button(innerComposite, SWT.RADIO);
		buttonSaved.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonSaved.setText("Use saved &filter from the repository");
		buttonSaved.setSelection(isRepository);

		buttonSaved.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = buttonSaved.getSelection();
				if (filters != null) {
					filterList.setEnabled(selection);
				}
				updateButton.setEnabled(selection);
				setPageComplete(selection);
			}
		});

		filterList = new List(innerComposite, SWT.V_SCROLL | SWT.BORDER);
		filterList.add(WAIT_MESSAGE);
		filterList.deselectAll();

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalIndent = 15;
		filterList.setLayoutData(data);
		filterList.setEnabled(false);

		updateButton = new Button(innerComposite, SWT.LEFT | SWT.PUSH);
		final GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, true);
		updateButton.setLayoutData(gridData);
		updateButton.setText("Update from &Repository");
		updateButton.setEnabled(isRepository);
		updateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				filterList.setEnabled(false);
				filterList.removeAll();
				filterList.add(WAIT_MESSAGE);
				filterList.deselectAll();

				getContainer().updateButtons();
				updateButton.setEnabled(false);

				downloadFilters();
			}

		});

		setControl(innerComposite);
		downloadFilters();
	}

	@Override
	public boolean isPageComplete() {
		return buttonCustom.getSelection() ? super.isPageComplete() : filterList.getSelectionCount() == 1;
	}

	@Override
	public IWizardPage getNextPage() {
		if (!buttonCustom.getSelection()) {
			return null;
		}

		if (filterSummaryPage == null) {
			if (query instanceof JiraCustomQuery) {
				filterSummaryPage = new JiraQueryPage(repository, (JiraCustomQuery) query);
			} else {
				filterSummaryPage = new JiraQueryPage(repository);
			}
			filterSummaryPage.setWizard(getWizard());
		}
		return filterSummaryPage;
	}

	@Override
	public boolean canFlipToNextPage() {
		return buttonCustom.getSelection();
	}

	protected void downloadFilters() {
		Job job = new Job(JOB_LABEL) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Downloading list of filters", IProgressMonitor.UNKNOWN);
				NamedFilter[] loadedFilters = new NamedFilter[0];
				IStatus status = Status.OK_STATUS;
				try {
					JiraClient jiraServer = JiraClientFactory.getDefault().getJiraClient(repository);
					loadedFilters = jiraServer.getNamedFilters();
					filters = loadedFilters;

				} catch (JiraException e) {
					status = RepositoryStatus.createStatus(repository.getUrl(), IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
							"Could not download saved filters: " + e.getMessage() + "\n"
									+ "Please check repository settings in the Task Repositories view");
					return Status.CANCEL_STATUS;
				} catch (Exception e) {
					status = RepositoryStatus.createStatus(repository.getUrl(), IStatus.ERROR, JiraCorePlugin.ID_PLUGIN,
							"Could not download saved filters from Jira repository.\n"
									+ "Please check repository settings in the Task Repositories view");
					StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, status.getMessage(), e));
					return Status.CANCEL_STATUS;
				} finally {
					showFilters(loadedFilters, status);
					monitor.done();
				}
				return status;
			}

			private void showFilters(final NamedFilter[] loadedFilters, final IStatus status) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!filterList.isDisposed()) {
							displayFilters(loadedFilters, status);
						}
						if (!updateButton.isDisposed() && !buttonSaved.isDisposed()) {
							updateButton.setEnabled(buttonSaved.getSelection());
						}
					}
				});
			}
		};
		job.schedule();
	}

	/**
	 * Called by the download job when the filters have been downloaded
	 * 
	 * @param status
	 */
	public void displayFilters(NamedFilter[] filters, IStatus status) {
		if (!status.isOK()) {
			setMessage(status.getMessage(), IMessageProvider.ERROR);
		}

		filterList.removeAll();

		if (filters.length == 0) {
			filterList.setEnabled(false);
			filterList.add("No filters found");
			filterList.deselectAll();

			if (status.isOK()) {
				setMessage("No saved filters found. Please create filters using JIRA web interface or"
						+ " follow to the next page to create custom query.", IMessageProvider.WARNING);
			}
			setPageComplete(false);
			return;
		}

		String id = null;
		if (query instanceof JiraRepositoryQuery) {
			id = ((JiraRepositoryQuery) query).getNamedFilter().getId();
		}

		int n = 0;
		for (int i = 0; i < filters.length; i++) {
			filterList.add(filters[i].getName());
			if (filters[i].getId().equals(id)) {
				n = i;
			}
		}

		filterList.select(n);
		filterList.showSelection();
		filterList.setEnabled(buttonSaved.getSelection());
		setPageComplete(status.isOK());
	}

	/** Returns the filter selected by the user or null on failure */
	private NamedFilter getSelectedFilter() {
		if (filters != null && filters.length > 0) {
			return filters[filterList.getSelectionIndex()];
		}
		return null;
	}

	@Override
	public AbstractRepositoryQuery getQuery() {
		if (buttonSaved.getSelection()) {
			return new JiraRepositoryQuery(repository.getUrl(), getSelectedFilter());
		}

		if (filterSummaryPage != null) {
			return filterSummaryPage.getQuery();
		}

		return null;
	}

}
