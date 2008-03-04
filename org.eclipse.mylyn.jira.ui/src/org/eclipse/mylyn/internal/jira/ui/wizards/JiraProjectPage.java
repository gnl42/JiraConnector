/*******************************************************************************
 * Copyright (c) 2003, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.internal.jira.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraCustomQuery;
import org.eclipse.mylyn.internal.jira.ui.JiraTask;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.tasks.core.TaskDataManager;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskSelection;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRepositoryTaskEditorInput;
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
 */
@SuppressWarnings("restriction")
public class JiraProjectPage extends WizardPage {

	private static final String DESCRIPTION = "Pick a project to open the new bug editor.\n"
			+ "Press the Update button if the project is not in the list.";

	private static final String LABEL_UPDATE = "Update Projects from Repository";

	private FilteredTree projectTree;

	private final TaskRepository repository;

	public JiraProjectPage(TaskRepository repository) {
		super("jiraProject");
		setTitle("New JIRA Task");
		setDescription(DESCRIPTION);

		this.repository = repository;
	}

	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page
		composite.setLayout(new GridLayout());

		// create the list of bug reports
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
					return project.getName() + "  (" + project.getKey() + ")";
				}
				return "";
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

		final Project project = discoverProject();
		if (project != null) {
			new UIJob("") { // waiting on delayed refresh of filtered tree
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					TreeViewer viewer = projectTree.getViewer();
					if (viewer != null && viewer.getTree() != null && !viewer.getTree().isDisposed()) {
						viewer.setSelection(new StructuredSelection(project));
						viewer.reveal(project);
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
					setErrorMessage("You must select a project");
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
		updateButton.setText(LABEL_UPDATE);
		updateButton.setLayoutData(new GridData());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				updateProjectsFromRepository(true);
			}
		});

		// set the composite as the control for this page
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
					StatusHandler.fail(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, "Error updating attributes", e));
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

	private Project discoverProject() {
		// TODO similarity with TasksUiUtil and Bugzilla implementation. consider adapting to TaskSelection or RepositoryTaskData

		Object element;

		IStructuredSelection selection = getSelection();
		if (selection != null) {
			element = selection.getFirstElement();
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
				element = ((TaskEditorInput) editorInput).getTask();
			} else if (editorInput instanceof AbstractRepositoryTaskEditorInput) {
				element = ((AbstractRepositoryTaskEditorInput) editorInput).getOldTaskData();
			} else {
				return null;
			}
		}

		if (element == null) {
			return null;
		}

		if (element instanceof JiraTask) {
			JiraTask jiraTask = (JiraTask) element;
			// API 3.0 need to provide public access to the task data
			if (jiraTask.getRepositoryUrl().equals(repository.getUrl())) {
				TaskDataManager taskDataManager = TasksUiPlugin.getTaskDataManager();
				Project project = getProject(taskDataManager.getNewTaskData(repository.getUrl(), jiraTask.getTaskId()));
				if (project != null) {
					return project;
				}
			}
		}

		if (element instanceof JiraCustomQuery) {
			JiraCustomQuery query = (JiraCustomQuery) element;
			if (query.getRepositoryUrl().equals(repository.getUrl())) {
				JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);
				FilterDefinition filter = query.getFilterDefinition(client, false);
				if (filter != null) {
					ProjectFilter projectFilter = filter.getProjectFilter();
					if (projectFilter != null) {
						return projectFilter.getProject();
					}
				}
			}
		}

//		if (element instanceof JiraRepositoryQuery) {
//			JiraRepositoryQuery query = (JiraRepositoryQuery) element;
//			NamedFilter namedFilter = query.getNamedFilter();
//			if (namedFilter != null) {
//				String projectId = namedFilter.getProject();
//				if (projectId != null) {
//					return client.getProjectById(projectId);
//				}
//			}
//		}

		RepositoryTaskData taskData = null;
		if (element instanceof RepositoryTaskData) {
			taskData = (RepositoryTaskData) getAdapter(element, RepositoryTaskData.class);
		}
		if (taskData == null) {
			TaskSelection taskSelection = (TaskSelection) getAdapter(element, TaskSelection.class);
			if (taskSelection != null) {
				taskData = taskSelection.getTaskData();
			}
		}

		if (taskData != null && taskData.getRepositoryUrl().equals(repository.getUrl())) {
			Project project = getProject(taskData);
			if (project != null) {
				return project;
			}
		}

		return null;
	}

	private Project getProject(RepositoryTaskData taskData) {
		if (taskData != null) {
			String projectName = taskData.getProduct();
			if (projectName != null && projectName.length() > 0) {
				JiraClient client = JiraClientFactory.getDefault().getJiraClient(repository);

				for (Project project : client.getCache().getProjects()) {
					if (projectName.equals(project.getName())) {
						return project;
					}
				}

				return client.getCache().getProjectByKey(projectName);
			}
		}
		return null;
	}

	private static Object getAdapter(Object adaptable, Class<?> adapterType) {
		if (adaptable.getClass().isAssignableFrom(adapterType)) {
			return adaptable;
		}

		if (adaptable instanceof IAdaptable) {
			Object adapter = ((IAdaptable) adaptable).getAdapter(adapterType);
			if (adapter != null) {
				return adapter;
			}
		}

		return Platform.getAdapterManager().getAdapter(adaptable, adapterType);
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
