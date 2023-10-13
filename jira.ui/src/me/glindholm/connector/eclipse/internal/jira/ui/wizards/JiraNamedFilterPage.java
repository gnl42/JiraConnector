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

package me.glindholm.connector.eclipse.internal.jira.ui.wizards;

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
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
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

import me.glindholm.connector.eclipse.internal.commons.ui.dialogs.RemoteApiLockedDialog;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraCaptchaRequiredException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiPlugin;

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

    private JiraNamedFilter[] filters = null;

    private JiraFilterDefinitionPage filterDefinitionPage;

    private final JiraNamedFilter workingCopy;

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

        private PredefinedFilter(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public JiraNamedFilterPage(final TaskRepository repository) {
        this(repository, null);
    }

    public JiraNamedFilterPage(final TaskRepository repository, final IRepositoryQuery query) {
        super(Messages.JiraNamedFilterPage_New_Jira_Query, repository, query);
        workingCopy = getFilter(query);
        client = JiraClientFactory.getDefault().getJiraClient(repository);
        setTitle(Messages.JiraNamedFilterPage_New_Jira_Query);
        setDescription(Messages.JiraNamedFilterPage_Please_select_a_query_type);
        setPageComplete(false);
    }

    private JiraNamedFilter getFilter(final IRepositoryQuery query) {
        JiraNamedFilter filter = null;
        if (query != null) {
            filter = JiraUtil.getNamedFilter(query);
        }
        if (filter == null) {
            filter = new JiraNamedFilter();
        }
        return filter;
    }

    @Override
    public void applyTo(final IRepositoryQuery query) {
        JiraFilter filter = null;
        if (buttonSaved.getSelection()) {
            final JiraNamedFilter f = getSavedFilter();
            query.setSummary(f.getName());
            filter = f;
        } else if (buttonPredefined.getSelection()) {
            final FilterDefinition f = getPredefinedFilter(query);

            filter = f;
        }
        JiraUtil.setQuery(getTaskRepository(), query, filter);
    }

    private FilterDefinition getPredefinedFilter(final IRepositoryQuery query) {
        final FilterDefinition filter = new FilterDefinition();

        final IStructuredSelection projectSelection = (IStructuredSelection) projectList.getSelection();
        if (projectSelection != null && !projectSelection.isEmpty()) {
            final Object selected = projectSelection.getFirstElement();
            if (selected instanceof JiraProject) {
                filter.setProjectFilter(new ProjectFilter((JiraProject) selected));
            }
        }

        final IStructuredSelection filterSelection = (IStructuredSelection) predefinedFiltersList.getSelection();

        if (filterSelection != null && !filterSelection.isEmpty()) {
            final PredefinedFilter selected = (PredefinedFilter) filterSelection.getFirstElement();

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

                final java.util.List<JiraStatus> statuses = new ArrayList<>();

                for (final JiraStatus status : client.getCache().getStatuses()) {
                    if (JIRA_STATUS_RESOLVED.equals(status.getId()) || JIRA_STATUS_CLOSED.equals(status.getId())) {
                        statuses.add(status);
                    }
                }
                filter.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
                break;
            case ASSIGNED_TO_ME:
                // empty (but not null) resolution filter means UNRESOLVED
                filter.setResolutionFilter(new ResolutionFilter(new JiraResolution[0]));
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

    @Override
    public void createControl(final Composite parent) {
        final IRepositoryQuery query = getQuery();
        final boolean isCustom = query == null || JiraUtil.isFilterDefinition(query);

        final Composite innerComposite = new Composite(parent, SWT.NONE);
        innerComposite.setLayoutData(new GridData());
        final GridLayout gl = new GridLayout(2, true);
        innerComposite.setLayout(gl);

        buttonCustom = new Button(innerComposite, SWT.RADIO);
        buttonCustom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        buttonCustom.setText(Messages.JiraNamedFilterPage_Create_query_using_form);
        buttonCustom.setSelection(isCustom);
        buttonCustom.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setErrorMessage(null);
                updateButton.setEnabled(!buttonCustom.getSelection());
                getContainer().updateButtons();
            }

        });
        buttonCustom.setEnabled(query == null || isCustom);

        buttonSaved = new Button(innerComposite, SWT.RADIO);
        buttonSaved.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        buttonSaved.setText(Messages.JiraNamedFilterPage_Use_saved_filter_from_the_repository);
        buttonSaved.setSelection(!isCustom);
        buttonSaved.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setErrorMessage(null);
                final boolean selection = buttonSaved.getSelection();
                if (filters != null && filters.length > 0) {
                    savedFilterList.setEnabled(selection);
                }
                updateButton.setEnabled(!buttonCustom.getSelection());
                getContainer().updateButtons();
            }
        });
        buttonSaved.setEnabled(query == null || !isCustom);

        savedFilterList = new List(innerComposite, SWT.V_SCROLL | SWT.BORDER);
        savedFilterList.add(Messages.JiraNamedFilterPage_Downloading_);
        savedFilterList.deselectAll();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd.horizontalIndent = 15;
        gd.minimumHeight = 90;
        gd.heightHint = 90;
        savedFilterList.setLayoutData(gd);
        savedFilterList.setEnabled(!isCustom);
        savedFilterList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateButton.setEnabled(!buttonCustom.getSelection());
                getContainer().updateButtons();
            }
        });

        buttonPredefined = new Button(innerComposite, SWT.RADIO);
        buttonPredefined.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        buttonPredefined.setText(Messages.JiraNamedFilterPage_Use_project_specific_predefined_filter);
        buttonPredefined.setSelection(false);
        buttonPredefined.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setErrorMessage(null);
                final boolean selection = buttonPredefined.getSelection();
                projectList.getControl().setEnabled(selection);
                predefinedFiltersList.getControl().setEnabled(selection);
                getContainer().updateButtons();
            }
        });
        buttonPredefined.setEnabled(query == null || !isCustom);

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
            public void widgetSelected(final SelectionEvent e) {
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
            public void widgetSelected(final SelectionEvent e) {
                getContainer().updateButtons();
            }
        });

        updateButton = new Button(innerComposite, SWT.LEFT | SWT.PUSH);
        gd = new GridData(SWT.LEFT, SWT.TOP, false, true);
        gd.horizontalIndent = 15;
        updateButton.setLayoutData(gd);
        updateButton.setText(Messages.JiraNamedFilterPage_Update_from_Repository);
        updateButton.setEnabled(!isCustom);
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {

                setErrorMessage(null);

                updateButton.setEnabled(false);

                if (buttonSaved.getSelection()) {
                    final String[] s = savedFilterList.getSelection();
                    savedFilterList.setEnabled(false);
                    savedFilterList.removeAll();
                    savedFilterList.add(Messages.JiraNamedFilterPage_Downloading_);
                    savedFilterList.deselectAll();

                    getContainer().updateButtons();

                    // download filters
                    downloadFilters();

                    //savedFilterList.setEnabled(buttonSaved.getSelection() && filters != null && filters.length > 0);
                    //savedFilterList.setSelection(s);

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
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);

        if (visible) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (downloadFilters()) {
                        if (!client.getCache().hasDetails()) {

                            final boolean projectListEnabled = projectList.getControl().getEnabled();
                            final boolean updateProjectsButtonEnabled = updateButton.getEnabled();

                            projectList.getControl().setEnabled(false);
                            updateButton.setEnabled(false);

                            downloadProjects();

                            projectList.getControl().setEnabled(projectListEnabled);
                            updateButton.setEnabled(updateProjectsButtonEnabled);

                        }
                    }
                    projectList.setInput(client.getCache().getProjects());
                }
            });
        }
    }

    private void initializeProjects() {
        projectList.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                final JiraProject[] projects = (JiraProject[]) inputElement;
                final Object[] elements = new Object[projects.length + 1];
                elements[0] = Messages.JiraFilterDefinitionPage_All_Projects;
                System.arraycopy(projects, 0, elements, 1, projects.length);
                return elements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }

        });

        projectList.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(final Object element) {
                if (element instanceof String) {
                    return (String) element;
                }
                return ((JiraProject) element).getName();
            }
        });

    }

    private void initializePredefinedFilters() {
        predefinedFiltersList.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return (PredefinedFilter[]) inputElement;
            }
        });

        predefinedFiltersList.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(final Object element) {
                return ((PredefinedFilter) element).getName();
            }

        });

        predefinedFiltersList.setInput(PredefinedFilter.values());

    }

    /**
     * Called by the download job when the filters have been downloaded
     */
    public void displayFilters(final JiraNamedFilter[] filters) {

        savedFilterList.removeAll();

        if (filters.length == 0) {
            savedFilterList.setEnabled(false);
            savedFilterList.add(Messages.JiraNamedFilterPage_No_filters_found);
            savedFilterList.deselectAll();
            return;
        }

        int n = 0;
        for (int i = 0; i < filters.length; i++) {
            savedFilterList.add(filters[i].getName());
            if (filters[i].getId().equals(workingCopy.getId())) {
                n = i;
            }
        }

        savedFilterList.select(n);
        savedFilterList.showSelection();
        savedFilterList.setEnabled(buttonSaved.getSelection());
        setPageComplete(true);
        //getContainer().updateButtons();
    }

    private void showFilters(final JiraNamedFilter[] loadedFilters) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!savedFilterList.isDisposed()) {
                    displayFilters(loadedFilters);
                }
            }
        });
    }

    protected boolean downloadFilters() {
        final boolean[] results = { false };

        final IRunnableWithProgress job = new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                JiraNamedFilter[] loadedFilters = {};
                try {
                    monitor.beginTask(Messages.JiraNamedFilterPage_Downloading_list_of_filters,
                            IProgressMonitor.UNKNOWN);
                    final JiraClient jiraServer = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
                    loadedFilters = jiraServer.getNamedFilters(monitor);
                    filters = loadedFilters;
                    results[0] = true;
                } catch (final JiraCaptchaRequiredException e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            new RemoteApiLockedDialog(WorkbenchUtil.getShell(), getTaskRepository().getRepositoryUrl()).open();
                        }
                    });
                    handleError(e, Messages.JiraNamedFilterPage_Could_not_update_filters);
                } catch (final JiraException e) {
                    handleError(e, Messages.JiraNamedFilterPage_Could_not_update_filters);
                } finally {
                    showFilters(loadedFilters);
                    monitor.done();
                }
            }
        };

        try {
            getRunnableContext().run(true, true, job);
        } catch (final Exception e) {
            handleError(e, Messages.JiraNamedFilterPage_Could_not_update_filters);
        }
        return results[0];
    }

    private IRunnableContext getRunnableContext() {
        IRunnableContext context = getContainer();
        if (context == null) {
            context = getSearchContainer().getRunnableContext();
        }
        if (context == null) {
            context = PlatformUI.getWorkbench().getProgressService();
        }
        return context;
    }

    private boolean downloadProjects() {
        final boolean[] results = { false };

        final ISelection selection = projectList.getSelection();

        final IRunnableWithProgress job = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(Messages.JiraNamedFilterPage_Downloading_Projects, IProgressMonitor.UNKNOWN);
                try {
                    client.getCache().refreshDetails(monitor);
                    results[0] = true;
                } catch (final JiraCaptchaRequiredException e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            new RemoteApiLockedDialog(WorkbenchUtil.getShell(), getTaskRepository().getRepositoryUrl()).open();
                        }
                    });
                    handleError(e, Messages.JiraNamedFilterPage_Download_Projects_Failed);
                } catch (final JiraException e) {
                    handleError(e, Messages.JiraNamedFilterPage_Download_Projects_Failed);
                } finally {
                    monitor.done();
                }

            }
        };

        try {
            getRunnableContext().run(true, true, job);
        } catch (final Exception e) {
            handleError(e, Messages.JiraNamedFilterPage_Download_Projects_Failed);
        }

        projectList.setInput(client.getCache().getProjects());
        projectList.setSelection(selection, true);

        return results[0];
    }

    protected void handleError(final Throwable e, final String message) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
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
            final IStructuredSelection filterSelection = (IStructuredSelection) predefinedFiltersList.getSelection();

            if (filterSelection != null && !filterSelection.isEmpty()) {
                final PredefinedFilter selected = (PredefinedFilter) filterSelection.getFirstElement();

                final IStructuredSelection projectSelection = (IStructuredSelection) projectList.getSelection();

                if (projectSelection != null && !projectSelection.isEmpty()) {
                    final Object project = projectSelection.getFirstElement();

                    String projectName = null;

                    if (project instanceof String) {
                        projectName = (String) project;
                    } else if (project instanceof JiraProject) {
                        projectName = ((JiraProject) project).getName();
                    }

                    if (projectName != null) {
                        return selected.getName() + " (" + projectName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                return selected.getName();
            }
        }

        return null;
    }

    /** Returns the filter selected by the user or null on failure */
    private JiraNamedFilter getSavedFilter() {
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
