/*******************************************************************************
 * Copyright (c) 2003 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.jira.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.core.MylarStatusHandler;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Implements a wizard page for selecting a JIRA project.
 * 
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
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
		projectTree = new FilteredTree(composite, SWT.SINGLE | SWT.BORDER, new PatternFilter());
		projectTree.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(
				SWT.DEFAULT, 200).create());

		TreeViewer projectTreeViewer = projectTree.getViewer();
		projectTreeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (element instanceof Project) ? ((Project) element).getName() : "";
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

		projectTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (projectTree.getViewer().getSelection().isEmpty()) {
					setErrorMessage("You must select a product");
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

		isPageComplete();
		getWizard().getContainer().updateButtons();
	}

	@Override
	public boolean isPageComplete() {
		return !projectTree.getViewer().getSelection().isEmpty();
	}

	private void updateProjectsFromRepository(final boolean force) {
		final JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
		if (!client.hasDetails() || force) {
			try {
				getContainer().run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							JiraClient client = JiraClientFacade.getDefault().getJiraClient(repository);
							client.refreshDetails(monitor);
						} catch (JiraException e) {
							showWarning(NLS.bind( //
									"Error updating attributes: {0}\n"
											+ "Please check repository settings in the Task Repositories view.", //
									e.getMessage()));
						} catch (Exception e) {
							String msg = NLS.bind( //
									"Error updating attributes: {0}\n"
											+ "Please check repository settings in the Task Repositories view.", //
									e.getMessage());
							showWarning(msg);
							MylarStatusHandler.fail(e, msg, false);
						} finally {
							monitor.done();
						}
					}
					private void showWarning(final String msg) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								JiraProjectPage.this.setErrorMessage(msg);
							}
						});
					}
				});
			} catch (Exception e) {
				return;
			}
		}
		
		projectTree.getViewer().setInput(client.getProjects());
	}

	public Project getSelectedProject() {
		IStructuredSelection selection = (IStructuredSelection) projectTree.getViewer().getSelection();
		return (Project) selection.getFirstElement();
	}

}
