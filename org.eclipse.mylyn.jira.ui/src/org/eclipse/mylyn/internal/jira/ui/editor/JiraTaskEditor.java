/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.JiraAttributeFactory;
import org.eclipse.mylar.internal.jira.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.mylar.internal.tasks.ui.editors.AbstractTaskEditorInput;
import org.eclipse.mylar.internal.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	private static final String SUBMIT_JOB_LABEL = "Submitting to JIRA repository";

	private JiraRepositoryConnector connector;

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	public void init(IEditorSite site, IEditorInput input) {
		if (!(input instanceof RepositoryTaskEditorInput))
			return;

		editorInput = (AbstractTaskEditorInput) input;
		repository = editorInput.getRepository();
		connector = (JiraRepositoryConnector) TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
				repository.getKind());

		setSite(site);
		setInput(input);
		isDirty = false;
		updateEditorTitle();
	}

	@Override
	protected void addAttachContextButton(Composite buttonComposite, ITask task) {
		// disabled
	}

	@Override
	protected void addSelfToCC(Composite composite) {
		// disabled
	}

	@Override
	protected void createCustomAttributeLayout(Composite composite) {
		// ignore
	}

	@Override
	protected void submitToRepository() {
		if (isDirty()) {
			this.doSave(new NullProgressMonitor());
		}
		updateTask();
		submitButton.setEnabled(false);
		showBusy(true);

		final JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
		if (jiraServer == null) {
			submitButton.setEnabled(true);
			JiraTaskEditor.this.showBusy(false);
			return;
		}

		// TODO: build a new issue object rather then retrieving from server
		final Issue issue = jiraServer.getIssue(this.getRepositoryTaskData().getAttributeValue(
				JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
		if (issue == null) {
			MylarStatusHandler.log("Unable to retrieve issue from repository", this);
			submitButton.setEnabled(true);
			JiraTaskEditor.this.showBusy(false);
			return;
		}

		final String comment = getNewCommentText();
		final AbstractRepositoryTask task = (AbstractRepositoryTask) TasksUiPlugin.getTaskListManager().getTaskList()
				.getTask(AbstractRepositoryTask.getHandle(repository.getUrl(), getRepositoryTaskData().getId()));
		final boolean attachContext = false;

		JobChangeAdapter listener = new JobChangeAdapter() {
			public void done(final IJobChangeEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (event.getJob().getResult().isOK()) {
							if (attachContext) {
								// TODO check for task == null
								// TODO should be done as part of job
								try {
									connector.attachContext(repository, (AbstractRepositoryTask) task, "",
											TasksUiPlugin.getDefault().getProxySettings());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							close();
						} else {
							submitButton.setEnabled(true);
							JiraTaskEditor.this.showBusy(false);
						}
					}
				});
			}
		};

		Job submitJob = new Job(SUBMIT_JOB_LABEL) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					
					Issue issue = JiraRepositoryConnector.buildJiraIssue(getRepositoryTaskData(), jiraServer);
					
					RepositoryOperation operation = getRepositoryTaskData().getSelectedOperation();
					if (operation != null) {
						//String action = operation.getKnobName();
						if("leave".equals(operation.getKnobName())) {
							jiraServer.updateIssue(issue, comment);
						}
//						else if(org.tigris.jira.core.model.Status.RESOLVED_ID.equals(operation.getKnobName())) {
//							String value = operation.getOptionValue("resolution");							
//							jiraServer.resolveIssue(issue, jiraServer.getResolutionById(value), fixVersions, comment, assigneeType, user);
//						}
						
					}

					if (task != null) {
						// XXX hack to avoid message about lost changes to local
						// task
						task.setTaskData(null);
						TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, null);
					}
					return Status.OK_STATUS;
				} catch (Exception e) {
					MylarStatusHandler.fail(e, "Error submitting to JIRA server", true);
					return Status.OK_STATUS;
				}
			}

		};

		submitJob.addJobChangeListener(listener);
		submitJob.schedule();
	}

	@Override
	protected void validateInput() {
		// TODO Auto-generated method stub
	}

}
