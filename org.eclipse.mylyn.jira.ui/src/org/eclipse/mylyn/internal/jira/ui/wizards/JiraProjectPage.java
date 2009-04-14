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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
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
import org.eclipse.ui.progress.UIJob;

/**
 * Implements a wizard page for selecting a JIRA project.
 * 
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 * @author Thomas Ehrnhoefer (multiple projects selection)
 */
public class JiraProjectPage extends WizardPage {

	private FilteredTree projectTree;

	private final TaskRepository repository;

	public JiraProjectPage(TaskRepository repository) {
		super("jiraProject"); //$NON-NLS-1$
		Assert.isNotNull(repository);
		setTitle(Messages.JiraProjectPage_New_JIRA_Task);
		setDescription(Messages.JiraProjectPage_Pick_a_project_to_open_the_new_bug_editor);
		this.repository = repository;
	}

	@SuppressWarnings("deprecation")
	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		composite.setLayout(new GridLayout());

		// create the list of bug reports
		// TODO e3.5 use new FilteredTree API
		projectTree = new FilteredTree(composite, SWT.SINGLE | SWT.BORDER, //
				new PatternFilter() { // matching on project keys
					@Override
					protected boolean isLeafMatch(Viewer viewer, Object element) {
						if (element instanceof Project) {
							Project project = (Project) element;
							if (wordMatches(project.getKey())) {
								return true;
							}
						}
						return super.isLeafMatch(viewer, element);
					}

				});
		projectTree.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(
				SWT.DEFAULT, 200).create());

		TreeViewer projectTreeViewer = projectTree.getViewer();
		projectTreeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Project) {
					Project project = (Project) element;
					return project.getName() + "  (" + project.getKey() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ""; //$NON-NLS-1$
			}
		});

		projectTreeViewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Project[]) {
					return (Project[]) parentElement;
				}
				return null;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		updateProjectsFromRepository(false);

		final Project[] projects = discoverProject();
		if (projects != null && projects.length > 0) {
			new UIJob("") { // waiting on delayed refresh of filtered tree //$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					TreeViewer viewer = projectTree.getViewer();
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

		projectTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (getSelectedProject() == null) {
					setErrorMessage(Messages.JiraProjectPage_You_must_select_a_project);
				} else {
					setErrorMessage(null);
				}
				getWizard().getContainer().updateButtons();
			}

		});

		projectTreeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (getWizard().canFinish()) {
					if (getWizard().performFinish()) {
						((WizardDialog) getContainer()).close();
					}
				}
			}
		});

		Button updateButton = new Button(composite, SWT.LEFT | SWT.PUSH);
		updateButton.setText(Messages.JiraProjectPage_Update_Projects_from_Repository);
		updateButton.setLayoutData(new GridData());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
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
			try {
				IRunnableWithProgress runner = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
							client.getCache().refreshDetails(monitor);
						} catch (JiraException e) {
							throw new InvocationTargetException(new CoreException(
									JiraCorePlugin.toStatus(repository, e)));
						} catch (OperationCanceledException e) {
							// canceled
							throw new InterruptedException();
						} finally {
							monitor.done();
						}
					}
				};

				if (getContainer().getShell().isVisible()) {
					getContainer().run(true, true, runner);
				} else {
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runner);
				}
			} catch (InterruptedException e) {
				// canceled
				return;
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof CoreException) {
					setErrorMessage(((CoreException) e.getCause()).getMessage());
				} else {
					// FIXME 3.2 replace with proper exception handling
					StatusHandler.fail(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, "Error updating attributes", e)); //$NON-NLS-1$
				}
				return;
			}
		}

		Project[] projects = client.getCache().getProjects();
		projectTree.getViewer().setInput(projects);
		getWizard().getContainer().updateButtons();

		if (projects.length == 1 && projectTree.getViewer().getSelection().isEmpty()) {
			projectTree.getViewer().setSelection(new StructuredSelection(projects[0]));
		} else {
			projectTree.setFocus();
		}
	}

	public Project getSelectedProject() {
		IStructuredSelection selection = (IStructuredSelection) projectTree.getViewer().getSelection();
		return (Project) selection.getFirstElement();
	}

	private Project[] discoverProject() {
		// TODO similarity with TasksUiUtil and Bugzilla implementation. consider adapting to TaskSelection or RepositoryTaskData
		Object element = getSelectedElement();
		if (element == null) {
			return new Project[0];
		}
		if (element instanceof ITask) {
			ITask task = (ITask) element;
			if (task.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
				try {
					TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
					Project project = getProject(taskData);
					if (project != null) {
						return new Project[] { project };
					}
				} catch (CoreException e) {
					// FIXME 3.2 replace with proper exception handling
					StatusHandler.fail(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, "Failed to load task data", //$NON-NLS-1$
							e));
				}
			}
		} else if (element instanceof IRepositoryQuery) {
			IRepositoryQuery query = (IRepositoryQuery) element;
			if (query.getRepositoryUrl().equals(repository.getRepositoryUrl())) {
				JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
				FilterDefinition filter = JiraUtil.getFilterDefinition(repository, client, query, false);
				if (filter != null) {
					ProjectFilter projectFilter = filter.getProjectFilter();
					if (projectFilter != null) {
						return projectFilter.getProjects();
					}
				}
			}
		}
		return new Project[0];
	}

	private Object getSelectedElement() {
		IStructuredSelection selection = getSelection();
		if (selection != null) {
			return selection.getFirstElement();
		} else {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null) {
				return null;
			}
			IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				return null;
			}
			IEditorPart editor = page.getActiveEditor();
			if (editor == null) {
				return null;
			}
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof TaskEditorInput) {
				return ((TaskEditorInput) editorInput).getTask();
			}
		}
		return null;
	}

	private Project getProject(TaskData taskData) {
		if (taskData != null) {
			TaskAttribute attribute = taskData.getRoot().getMappedAttribute(JiraAttribute.PROJECT.id());
			if (attribute != null) {
				JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
				return client.getCache().getProjectById(attribute.getValue());
			}
		}
		return null;
	}

	private IStructuredSelection getSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return null;
	}

}
