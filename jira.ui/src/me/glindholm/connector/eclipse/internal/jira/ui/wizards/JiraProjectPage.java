/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     Atlassian - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.wizards;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.UIJob;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiPlugin;

/**
 * Implements a wizard page for selecting a JIRA project.
 *
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class JiraProjectPage extends WizardPage {

    private FilteredTree projectTree;

    private final TaskRepository repository;

    private Button offlineButton;

    public JiraProjectPage(final TaskRepository repository) {
        super("jiraProject"); //$NON-NLS-1$
        Assert.isNotNull(repository);
        setTitle(Messages.JiraProjectPage_New_JIRA_Task);
        setDescription(Messages.JiraProjectPage_Pick_a_project_to_open_the_new_bug_editor);
        this.repository = repository;
    }

    @Override
    public void createControl(final Composite parent) {
        // create the composite to hold the widgets
        final Composite composite = new Composite(parent, SWT.NULL);

        // create the desired layout for this wizard page
        composite.setLayout(new GridLayout());

        final PatternFilter patternFilter = new PatternFilter() { // matching on project keys
            @Override
            protected boolean isLeafMatch(final Viewer viewer, final Object element) {
                if (element instanceof final JiraProject project) {
                    if (wordMatches(project.getKey())) {
                        return true;
                    }
                }
                return super.isLeafMatch(viewer, element);
            }

        };
        projectTree = new FilteredTree(composite, SWT.SINGLE | SWT.BORDER, patternFilter, true, false); // FIXME Should we use fasthash instead?
        projectTree.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 200).create());

        final TreeViewer projectTreeViewer = projectTree.getViewer();
        projectTreeViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(final Object element) {
                if (element instanceof final JiraProject project) {
                    return project.getName() + "  (" + project.getKey() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                return ""; //$NON-NLS-1$
            }
        });

        projectTreeViewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public Object[] getChildren(final Object parentElement) {
                if (parentElement instanceof JiraProject[]) {
                    return (JiraProject[]) parentElement;
                }
                return null;
            }

            @Override
            public Object getParent(final Object element) {
                return null;
            }

            @Override
            public boolean hasChildren(final Object element) {
                return false;
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return getChildren(inputElement);
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }
        });

        updateProjectsFromRepository(false);

        final JiraProject[] projects = discoverProject();
        if (projects != null && projects.length > 0) {
            new UIJob("") { // waiting on delayed refresh of filtered tree //$NON-NLS-1$
                @Override
                public IStatus runInUIThread(final IProgressMonitor monitor) {
                    final TreeViewer viewer = projectTree.getViewer();
                    if (viewer != null && viewer.getTree() != null && !viewer.getTree().isDisposed()) {
                        viewer.setSelection(new StructuredSelection(projects));
                        viewer.reveal(projects);
                        viewer.getTree().showSelection();
                        viewer.getTree().setFocus();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule(300L);
        }

        projectTreeViewer.addSelectionChangedListener(event -> {
            if (getSelectedProject() == null) {
                setErrorMessage(Messages.JiraProjectPage_You_must_select_a_project);
            } else if (!getSelectedProject().hasDetails()) {
                setMessage(Messages.JiraProjectPage_This_project_has_details_missing);
            } else {
                setErrorMessage(null);
                setMessage(null);
            }
            getWizard().getContainer().updateButtons();
        });

        projectTreeViewer.addOpenListener(event -> {
            if (getWizard().canFinish()) {
                if (getWizard().performFinish()) {
                    ((WizardDialog) getContainer()).close();
                }
            }
        });

        projectTreeViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                if (element instanceof JiraProject) {
                    return offlineButton != null && !offlineButton.getSelection() || ((JiraProject) element).hasDetails();
                }
                return false;
            }
        });

        projectTree.getFilterControl().addModifyListener(e -> {
            final String text = projectTree.getFilterControl().getText();
            if (!StringUtils.isEmpty(text) && !text.equals(WorkbenchMessages.FilteredTree_FilterMessage)) {
                if (offlineButton != null && offlineButton.getSelection()) {
                    offlineButton.setSelection(false);
                }
            }
        });

        offlineButton = new Button(composite, SWT.CHECK);
        offlineButton.setText("Show only projects available in offline mode");
        offlineButton.setSelection(true);
        offlineButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                projectTree.getViewer().refresh();
            }
        });
        GridDataFactory.fillDefaults().applyTo(offlineButton);

        final Button updateButton = new Button(composite, SWT.LEFT | SWT.PUSH);
        updateButton.setText(Messages.JiraProjectPage_Update_Project_Listing);
        updateButton.setLayoutData(new GridData());
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                updateProjectsFromRepository(true);
            }
        });

        Dialog.applyDialogFont(composite);
        setControl(composite);
    }

    @Override
    public boolean isPageComplete() {
        return getSelectedProject() != null;
    }

    private void updateProjectsFromRepository(final boolean force) {
        final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
        if (!client.getCache().hasDetails() || force) {
            final ICoreRunnable runner = monitor -> {
                try {
                    final JiraClient client1 = JiraClientFactory.getDefault().getJiraClient(repository);
                    client1.getCache().refreshDetails(monitor);
                } catch (final JiraException e) {
                    throw new CoreException(JiraCorePlugin.toStatus(repository, e));
                }
            };

            try {
                if (getContainer().getShell().isVisible()) {
                    CommonUiUtil.run(getContainer(), runner);
                } else {
                    WorkbenchUtil.busyCursorWhile(runner);
                }
            } catch (final OperationCanceledException e) {
                // canceled
                return;
            } catch (final CoreException e) {
                CommonUiUtil.setMessage(this, e.getStatus());
            }
        }

        final JiraProject[] projects = client.getCache().getProjects();
        projectTree.getViewer().setInput(projects);
        getWizard().getContainer().updateButtons();

        if (projects.length == 1 && projectTree.getViewer().getSelection().isEmpty()) {
            projectTree.getViewer().setSelection(new StructuredSelection(projects[0]));
        } else {
            projectTree.setFocus();
        }
    }

    public JiraProject getSelectedProject() {
        final IStructuredSelection selection = (IStructuredSelection) projectTree.getViewer().getSelection();
        return (JiraProject) selection.getFirstElement();
    }

    private JiraProject[] discoverProject() {
        // TODO similarity with TasksUiUtil and Bugzilla implementation. consider adapting to TaskSelection
        // or RepositoryTaskData
        final Object element = getSelectedElement();
        if (element == null) {
            return new JiraProject[0];
        }
        if (element instanceof final ITask task) {
            if (task.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
                try {
                    final TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
                    final JiraProject project = getProject(taskData);
                    if (project != null) {
                        return new JiraProject[] { project };
                    }
                } catch (final CoreException e) {
                    StatusHandler.log(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, "Failed to determine selected project", //$NON-NLS-1$
                            e));
                }
            }
        } else if (element instanceof final IRepositoryQuery query) {
            if (query.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
                final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
                final FilterDefinition filter = JiraUtil.getFilterDefinition(repository, client, query, false);
                if (filter != null) {
                    final ProjectFilter projectFilter = filter.getProjectFilter();
                    if (projectFilter != null) {
                        return projectFilter.getProjects();
                    }
                }
            }
        }
        return new JiraProject[0];
    }

    private Object getSelectedElement() {
        final IStructuredSelection selection = getSelection();
        if (selection != null) {
            return selection.getFirstElement();
        } else {
            final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window == null) {
                return null;
            }
            final IWorkbenchPage page = window.getActivePage();
            if (page == null) {
                return null;
            }
            final IEditorPart editor = page.getActiveEditor();
            if (editor == null) {
                return null;
            }
            final IEditorInput editorInput = editor.getEditorInput();
            if (editorInput instanceof TaskEditorInput) {
                return ((TaskEditorInput) editorInput).getTask();
            }
        }
        return null;
    }

    private JiraProject getProject(final TaskData taskData) {
        if (taskData != null) {
            final TaskAttribute attribute = taskData.getRoot().getMappedAttribute(JiraAttribute.PROJECT.id());
            if (attribute != null) {
                final JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
                return client.getCache().getProjectById(attribute.getValue());
            }
        }
        return null;
    }

    private IStructuredSelection getSelection() {
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final ISelection selection = window.getSelectionService().getSelection();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        return null;
    }

}
