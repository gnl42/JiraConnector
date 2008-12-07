/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
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
public class JiraNamedFilterPage extends AbstractRepositoryQueryPage {

	private Button buttonCustom;

	private Button buttonSaved;

	private List filterList;

	private NamedFilter[] filters = null;

	private JiraFilterDefinitionPage filterDefinitionPage;

	private Button updateButton = null;

	private final NamedFilter workingCopy;

	public JiraNamedFilterPage(TaskRepository repository) {
		this(repository, null);
	}

	public JiraNamedFilterPage(TaskRepository repository, IRepositoryQuery query) {
		super(Messages.JiraNamedFilterPage_New_Jira_Query, repository, query);
		this.workingCopy = getFilter(query);
		setTitle(Messages.JiraNamedFilterPage_New_Jira_Query);
		setDescription(Messages.JiraNamedFilterPage_Please_select_a_query_type);
		setPageComplete(false);
	}

	private NamedFilter getFilter(IRepositoryQuery query) {
		NamedFilter filter = null;
		if (query != null) {
			filter = JiraUtil.getNamedFilter(query);
		}
		if (filter == null) {
			filter = new NamedFilter();
		}
		return filter;
	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		NamedFilter filter = getSelectedFilter();
		query.setSummary(filter.getName());
		JiraUtil.setQuery(getTaskRepository(), query, filter);
	}

	@Override
	public boolean canFlipToNextPage() {
		return buttonCustom.getSelection();
	}

	public void createControl(Composite parent) {
		IRepositoryQuery query = getQuery();
		boolean isCustom = query == null || JiraUtil.isFilterDefinition(query);

		final Composite innerComposite = new Composite(parent, SWT.NONE);
		innerComposite.setLayoutData(new GridData());
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		innerComposite.setLayout(gl);

		buttonCustom = new Button(innerComposite, SWT.RADIO);
		buttonCustom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonCustom.setText(Messages.JiraNamedFilterPage_Create_query_using_form);
		buttonCustom.setSelection(isCustom);

		buttonSaved = new Button(innerComposite, SWT.RADIO);
		buttonSaved.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonSaved.setText(Messages.JiraNamedFilterPage_Use_saved_filter_from_the_repository);
		buttonSaved.setSelection(!isCustom);
		buttonSaved.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				boolean selection = buttonSaved.getSelection();
				if (filters != null) {
					filterList.setEnabled(selection);
				}
				updateButton.setEnabled(selection);
				getContainer().updateButtons();
			}
		});

		filterList = new List(innerComposite, SWT.V_SCROLL | SWT.BORDER);
		filterList.add(Messages.JiraNamedFilterPage_Downloading_);
		filterList.deselectAll();
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalIndent = 15;
		filterList.setLayoutData(data);
		filterList.setEnabled(false);
		filterList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}
		});

		updateButton = new Button(innerComposite, SWT.LEFT | SWT.PUSH);
		final GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, true);
		updateButton.setLayoutData(gridData);
		updateButton.setText(Messages.JiraNamedFilterPage_Update_from_Repository);
		updateButton.setEnabled(isCustom);
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				filterList.setEnabled(false);
				filterList.removeAll();
				filterList.add(Messages.JiraNamedFilterPage_Downloading_);
				filterList.deselectAll();

				getContainer().updateButtons();
				updateButton.setEnabled(false);

				downloadFilters();
			}
		});

		Dialog.applyDialogFont(innerComposite);
		setControl(innerComposite);
		downloadFilters();
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
			filterList.add(Messages.JiraNamedFilterPage_No_filters_found);
			filterList.deselectAll();

			if (status.isOK()) {
				setMessage(Messages.JiraNamedFilterPage_No_saved_filters_found, IMessageProvider.WARNING);
			}
			setPageComplete(false);
			return;
		}

		int n = 0;
		for (int i = 0; i < filters.length; i++) {
			filterList.add(filters[i].getName());
			if (filters[i].getId().equals(workingCopy.getId())) {
				n = i;
			}
		}

		filterList.select(n);
		filterList.showSelection();
		filterList.setEnabled(buttonSaved.getSelection());
		setPageComplete(status.isOK());
	}

	protected void downloadFilters() {
		Job job = new Job(Messages.JiraNamedFilterPage_Downloading_Filter_Names) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(Messages.JiraNamedFilterPage_Downloading_list_of_filters, IProgressMonitor.UNKNOWN);
				NamedFilter[] loadedFilters = new NamedFilter[0];
				IStatus status = Status.OK_STATUS;
				try {
					JiraClient jiraServer = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
					loadedFilters = jiraServer.getNamedFilters(monitor);
					filters = loadedFilters;
				} catch (JiraException e) {
					status = RepositoryStatus.createStatus(getTaskRepository().getRepositoryUrl(), IStatus.ERROR,
							JiraCorePlugin.ID_PLUGIN, Messages.JiraNamedFilterPage_Could_not_update_filters
									+ e.getMessage() + "\n"); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				} catch (OperationCanceledException e) {
					return Status.CANCEL_STATUS;
				} finally {
					showFilters(loadedFilters, status);
					monitor.done();
				}
				return Status.OK_STATUS;
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

	@Override
	public IWizardPage getNextPage() {
		if (!buttonCustom.getSelection()) {
			return null;
		}
		if (filterDefinitionPage == null) {
			filterDefinitionPage = new JiraFilterDefinitionPage(getTaskRepository(), getQuery());
			if (getWizard() instanceof Wizard) {
				((Wizard) getWizard()).addPage(filterDefinitionPage);
			}
		}
		return filterDefinitionPage;
	}

	@Override
	public String getQueryTitle() {
		return getSelectedFilter() != null ? getSelectedFilter().getName() : null;
	}

	/** Returns the filter selected by the user or null on failure */
	private NamedFilter getSelectedFilter() {
		if (filters != null && filters.length > 0) {
			return filters[filterList.getSelectionIndex()];
		}
		return null;
	}

	@Override
	public boolean isPageComplete() {
		return buttonCustom.getSelection() ? false : filterList.getSelectionCount() == 1 && super.isPageComplete();
	}

}
