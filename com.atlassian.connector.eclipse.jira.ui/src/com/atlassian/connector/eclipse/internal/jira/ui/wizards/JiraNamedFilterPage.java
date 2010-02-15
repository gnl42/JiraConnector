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

package com.atlassian.connector.eclipse.internal.jira.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
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
import org.eclipse.ui.PlatformUI;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;

/**
 * Wizard page that allows the user to select a named Jira filter they have defined on the server.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Eugene Kuleshov (layout and other improvements)
 * @author Steffen Pingel
 * @author Jacek Jaroczynski
 */
public class JiraNamedFilterPage extends AbstractRepositoryQueryPage {

	private static final String JIRA_STATUS_CLOSED = "6"; //$NON-NLS-1$

	private static final String JIRA_STATUS_RESOLVED = "5"; //$NON-NLS-1$

	private Button buttonCustom;

	private Button buttonSaved;

	private List savedFilterList;

	private NamedFilter[] filters = null;

	private JiraFilterDefinitionPage filterDefinitionPage;

	private final NamedFilter workingCopy;

	private Button buttonPredefined;

	private ListViewer projectList;

	private Button updateButton;

	private ListViewer predefinedFiltersList;

	private final JiraClient client;

	private enum PredefinedFilter {
		ASSIGNED_TO_ME(Messages.JiraNamedFilterPage_Predefined_filter_assigned_to_me), REPORTED_BY_ME(
				Messages.JiraNamedFilterPage_Predefined_filter_reported_by_me), ADDED_RECENTLY(
				Messages.JiraNamedFilterPage_Predefined_filter_added_recently), UPDATED_RECENTLY(
				Messages.JiraNamedFilterPage_Predefined_filter_updated_recently), RESOLVED_RECENTLY(
				Messages.JiraNamedFilterPage_Predefined_filter_resolved_recently);

		private String name;

		private PredefinedFilter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public JiraNamedFilterPage(TaskRepository repository) {
		this(repository, null);
	}

	public JiraNamedFilterPage(TaskRepository repository, IRepositoryQuery query) {
		super(Messages.JiraNamedFilterPage_New_Jira_Query, repository, query);
		this.workingCopy = getFilter(query);
		this.client = JiraClientFactory.getDefault().getJiraClient(repository);
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
		JiraFilter filter = null;
		if (buttonSaved.getSelection()) {
			NamedFilter f = getSavedFilter();
			query.setSummary(f.getName());
			filter = f;
		} else if (buttonPredefined.getSelection()) {
			FilterDefinition f = getPredefinedFilter(query);

			filter = f;
		}
		JiraUtil.setQuery(getTaskRepository(), query, filter);
	}

	private FilterDefinition getPredefinedFilter(IRepositoryQuery query) {
		FilterDefinition filter = new FilterDefinition();

		IStructuredSelection projectSelection = (IStructuredSelection) projectList.getSelection();
		if (projectSelection != null && !projectSelection.isEmpty()) {
			Object selected = projectSelection.getFirstElement();
			if (selected instanceof Project) {
				filter.setProjectFilter(new ProjectFilter((Project) selected));
			}
		}

		IStructuredSelection filterSelection = (IStructuredSelection) predefinedFiltersList.getSelection();

		if (filterSelection != null && !filterSelection.isEmpty()) {
			PredefinedFilter selected = (PredefinedFilter) filterSelection.getFirstElement();

			query.setSummary(getQueryTitle());

			switch (selected) {
			case ADDED_RECENTLY:
				filter.setCreatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$
				break;
			case UPDATED_RECENTLY:
				filter.setUpdatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$
				break;
			case RESOLVED_RECENTLY:
				filter.setUpdatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$

				java.util.List<JiraStatus> statuses = new ArrayList<JiraStatus>();

				for (JiraStatus status : client.getCache().getStatuses()) {
					if (status.getId().equals(JIRA_STATUS_RESOLVED) || status.getId().equals(JIRA_STATUS_CLOSED)) {
						statuses.add(status);
					}
				}
				filter.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
				break;
			case ASSIGNED_TO_ME:
				// empty (but not null) resolution filter means UNRESOLVED 
				filter.setResolutionFilter(new ResolutionFilter(new Resolution[0]));
				filter.setAssignedToFilter(new CurrentUserFilter());
				break;
			case REPORTED_BY_ME:
				filter.setReportedByFilter(new CurrentUserFilter());
				break;
			}
		}

		return filter;
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
		GridLayout gl = new GridLayout(2, true);
		innerComposite.setLayout(gl);

		buttonCustom = new Button(innerComposite, SWT.RADIO);
		buttonCustom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonCustom.setText(Messages.JiraNamedFilterPage_Create_query_using_form);
		buttonCustom.setSelection(isCustom);
		buttonCustom.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				updateButton.setEnabled(!buttonCustom.getSelection());
				getContainer().updateButtons();
			}

		});

		buttonSaved = new Button(innerComposite, SWT.RADIO);
		buttonSaved.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonSaved.setText(Messages.JiraNamedFilterPage_Use_saved_filter_from_the_repository);
		buttonSaved.setSelection(!isCustom);
		buttonSaved.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				boolean selection = buttonSaved.getSelection();
				if (filters != null && filters.length > 0) {
					savedFilterList.setEnabled(selection);
				}
				getContainer().updateButtons();
			}
		});

		savedFilterList = new List(innerComposite, SWT.V_SCROLL | SWT.BORDER);
		savedFilterList.add(Messages.JiraNamedFilterPage_Downloading_);
		savedFilterList.deselectAll();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.horizontalIndent = 15;
		gd.minimumHeight = 90;
		gd.heightHint = 90;
		savedFilterList.setLayoutData(gd);
		savedFilterList.setEnabled(false);
		savedFilterList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}
		});

		buttonPredefined = new Button(innerComposite, SWT.RADIO);
		buttonPredefined.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		buttonPredefined.setText(Messages.JiraNamedFilterPage_Use_project_specific_predefined_filter);
		buttonPredefined.setSelection(false);
		buttonPredefined.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				boolean selection = buttonPredefined.getSelection();
				projectList.getControl().setEnabled(selection);
				predefinedFiltersList.getControl().setEnabled(selection);
				getContainer().updateButtons();
			}
		});

		projectList = new ListViewer(innerComposite, SWT.V_SCROLL | SWT.BORDER);
		projectList.add(Messages.JiraNamedFilterPage_Downloading_);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = 15;
		gd.minimumHeight = 90;
		gd.heightHint = 90;
		projectList.getControl().setLayoutData(gd);
		projectList.getControl().setEnabled(false);
		projectList.getList().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}
		});

		predefinedFiltersList = new ListViewer(innerComposite, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = 15;
		gd.minimumHeight = 90;
		gd.heightHint = 90;
		predefinedFiltersList.getControl().setLayoutData(gd);
		predefinedFiltersList.getControl().setEnabled(false);
		predefinedFiltersList.getList().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}
		});

		updateButton = new Button(innerComposite, SWT.LEFT | SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, true);
		gd.horizontalIndent = 15;
		updateButton.setLayoutData(gd);
		updateButton.setText(Messages.JiraNamedFilterPage_Update_from_Repository);
		updateButton.setEnabled(false);
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setErrorMessage(null);

				updateButton.setEnabled(false);

				if (buttonSaved.getSelection()) {
					String[] s = savedFilterList.getSelection();
					savedFilterList.setEnabled(false);
					savedFilterList.removeAll();
					savedFilterList.add(Messages.JiraNamedFilterPage_Downloading_);
					savedFilterList.deselectAll();

					getContainer().updateButtons();

					// download filters
					downloadFilters();

					savedFilterList.setEnabled(buttonSaved.getSelection() && filters != null && filters.length > 0);
					savedFilterList.setSelection(s);

					getContainer().updateButtons();

				} else if (buttonPredefined.getSelection()) {

					projectList.getControl().setEnabled(false);

					getContainer().updateButtons();

					// download projects
					downloadProjects();

					projectList.getControl().setEnabled(buttonPredefined.getSelection());
					getContainer().updateButtons();
				}
				updateButton.setEnabled(true);
			}
		});

		initializeProjects();
		initializePredefinedFilters();

		Dialog.applyDialogFont(innerComposite);
		setControl(innerComposite);
		downloadFilters();

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (!client.getCache().hasDetails()) {

				boolean projectListEnabled = projectList.getControl().getEnabled();
				boolean updateProjectsButtonEnabled = updateButton.getEnabled();

				projectList.getControl().setEnabled(false);
				updateButton.setEnabled(false);

				downloadProjects();

				projectList.getControl().setEnabled(projectListEnabled);
				updateButton.setEnabled(updateProjectsButtonEnabled);

			}

			projectList.setInput(client.getCache().getProjects());

		}
	}

	private void initializeProjects() {
		projectList.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				Project[] projects = (Project[]) inputElement;
				Object[] elements = new Object[projects.length + 1];
				elements[0] = Messages.JiraFilterDefinitionPage_All_Projects;
				System.arraycopy(projects, 0, elements, 1, projects.length);
				return elements;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		projectList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				}
				return ((Project) element).getName();
			}
		});

	}

	private void initializePredefinedFilters() {
		predefinedFiltersList.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				return (PredefinedFilter[]) inputElement;
			}
		});

		predefinedFiltersList.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				return ((PredefinedFilter) element).getName();
			}

		});

		predefinedFiltersList.setInput(PredefinedFilter.values());

	}

	/**
	 * Called by the download job when the filters have been downloaded
	 */
	public void displayFilters(NamedFilter[] filters) {

		savedFilterList.removeAll();

		if (filters.length == 0) {
			savedFilterList.setEnabled(false);
			savedFilterList.add(Messages.JiraNamedFilterPage_No_filters_found);
			savedFilterList.deselectAll();

//			setMessage(Messages.JiraNamedFilterPage_No_saved_filters_found, IMessageProvider.WARNING);
//			setPageComplete(false);
			return;
		}

		int n = 0;
		for (int i = 0; i < filters.length; i++) {
			savedFilterList.add(filters[i].getName());
			if (filters[i].getId().equals(workingCopy.getId())) {
				n = i;
			}
		}

//		savedFilterList.select(n);
//		savedFilterList.showSelection();
		savedFilterList.setEnabled(buttonSaved.getSelection());
		setPageComplete(true);
	}

	protected void downloadFilters() {

		IRunnableWithProgress job = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.JiraNamedFilterPage_Downloading_list_of_filters, IProgressMonitor.UNKNOWN);
				NamedFilter[] loadedFilters = new NamedFilter[0];
				try {
					JiraClient jiraServer = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
					loadedFilters = jiraServer.getNamedFilters(monitor);
					filters = loadedFilters;
				} catch (JiraException e) {
					handleError(e, Messages.JiraNamedFilterPage_Could_not_update_filters);
				} finally {
					showFilters(loadedFilters);
					monitor.done();
				}
			}

			private void showFilters(final NamedFilter[] loadedFilters) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!savedFilterList.isDisposed()) {
							displayFilters(loadedFilters);
						}
//						if (!updateFiltersButton.isDisposed() && !buttonSaved.isDisposed()) {
//							updateFiltersButton.setEnabled(buttonSaved.getSelection());
//						}
					}
				});
			}
		};

		IRunnableContext context = getContainer();
		if (context == null) {
			context = getSearchContainer().getRunnableContext();
		}
		if (context == null) {
			context = PlatformUI.getWorkbench().getProgressService();
		}

		try {
			context.run(true, true, job);
		} catch (Exception e) {
			handleError(e, Messages.JiraNamedFilterPage_Could_not_update_filters);
		}

	}

	private void downloadProjects() {

		ISelection selection = projectList.getSelection();

		IRunnableWithProgress job = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				monitor.beginTask(Messages.JiraNamedFilterPage_Downloading_Projects, IProgressMonitor.UNKNOWN);
				try {
					client.getCache().refreshDetails(monitor);
				} catch (JiraException e) {
					handleError(e, Messages.JiraNamedFilterPage_Download_Projects_Failed);
				} finally {
					monitor.done();
				}

			}
		};

		IRunnableContext context = getContainer();
		if (context == null) {
			context = getSearchContainer().getRunnableContext();
		}
		if (context == null) {
			context = PlatformUI.getWorkbench().getProgressService();
		}

		try {
			context.run(true, true, job);
		} catch (Exception e) {
			handleError(e, Messages.JiraNamedFilterPage_Download_Projects_Failed);
		}

		projectList.setInput(client.getCache().getProjects());
		projectList.setSelection(selection, true);
	}

	protected void handleError(final Throwable e, final String message) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, message, e));
				setErrorMessage(message + ": " + e.getMessage()); //$NON-NLS-1$
			}
		});

	}

	@Override
	public IWizardPage getNextPage() {
		if (buttonSaved.getSelection() || buttonPredefined.getSelection()) {
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
		if (buttonSaved.getSelection()) {
			return getSavedFilter() != null ? getSavedFilter().getName() : null;
		} else if (buttonPredefined.getSelection()) {
			IStructuredSelection filterSelection = (IStructuredSelection) predefinedFiltersList.getSelection();

			IStructuredSelection projectSelection = (IStructuredSelection) projectList.getSelection();

			if (filterSelection != null && !filterSelection.isEmpty() && projectSelection != null
					&& !projectSelection.isEmpty()) {
				PredefinedFilter selected = (PredefinedFilter) filterSelection.getFirstElement();
				Object project = projectSelection.getFirstElement();

				String projectName = null;

				if (project instanceof String) {
					projectName = (String) project;
				} else if (project instanceof Project) {
					projectName = ((Project) project).getName();
				}

				if (projectName != null) {
					return selected.getName() + " (" + projectName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}

				return selected.getName();

			}
		}

		return null;
	}

	/** Returns the filter selected by the user or null on failure */
	private NamedFilter getSavedFilter() {
		if (filters != null && filters.length > 0 && savedFilterList.getSelectionIndex() >= 0) {
			return filters[savedFilterList.getSelectionIndex()];
		}
		return null;
	}

	@Override
	public boolean isPageComplete() {
		boolean ret = false;

		if (buttonSaved.getSelection() && savedFilterList.getSelectionCount() == 1) {
			ret = true;
		} else if (buttonPredefined.getSelection() && projectList.getList().getSelectionCount() == 1
				&& predefinedFiltersList.getList().getSelectionCount() == 1) {
			ret = true;
		}

		return ret; //&& super.isPageComplete(); (do not check name duplicates)
	}

}
