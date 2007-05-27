/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	private Composite attributesComposite;
	private Composite buttonComposite;

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
//		super.showAttachments = false;
	}

//	@Override
//	protected void addAttachContextButton(Composite buttonComposite, ITask task) {
//		// disabled
//	}

	@Override
	protected void addSelfToCC(Composite composite) {
		// disabled
	}
	
	@Override
	protected void addCCList(Composite attributesComposite) {
		// disabled
	}

	@Override
	protected void createAttributeLayout(Composite attributesComposite) {
		this.attributesComposite = attributesComposite;
		if(isTaskParametersSynchronized()) {
			super.createAttributeLayout(attributesComposite);
		}
	}
	
	@Override
	protected void addRadioButtons(Composite buttonComposite) {
		this.buttonComposite = buttonComposite;
		if(isTaskParametersSynchronized()) {
			super.addRadioButtons(buttonComposite);
		}
	}
	
	@Override
	protected void addActionButtons(Composite buttonComposite) {
		if(isTaskParametersSynchronized()) {
			super.addActionButtons(buttonComposite);
		}
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		if(!isTaskParametersSynchronized()) {
			final String taskKey = taskData.getTaskKey();
			new Job("Retrieving data for task " + taskKey) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {

					JiraClient server = JiraClientFacade.getDefault().getJiraClient(repository);

//					RepositoryOperation[] availableOperations;
//					try {
//						availableOperations = server.getAvailableOperations(taskKey);
//						for (int i = 0; i < availableOperations.length; i++) {
//							RepositoryOperation operation = availableOperations[i];
//							System.err.println("#### " + operation.getKnobName() + " : " + operation.getOperationName());
//						}
//					} catch (JiraException e) {
//						MylarStatusHandler.log(e, "Can't retrieve avaialble operations for " + taskKey);
//					}

					AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(getRepositoryTask());
					AbstractAttributeFactory attributeFactory = connector.getTaskDataHandler().getAttributeFactory(taskData);
					
					try {
						HashSet<String> editableKeys = new HashSet<String>();
						RepositoryTaskAttribute[] editableAttributes = server.getEditableAttributes(taskKey);
						if(editableAttributes!=null) {
							for (RepositoryTaskAttribute attribute : editableAttributes) {
								editableKeys.add(attributeFactory.mapCommonAttributeKey(attribute.getID()));
							}
						}

						for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
							boolean editable = editableKeys.contains(attribute.getID().toLowerCase());
							// System.err.println(" " + attribute.getID() + " : " + attribute.getName() + " : " + editable);
							attribute.setReadOnly(!editable);
							if (editable && !attributeFactory.getIsHidden(attribute.getID())) {
								attribute.setHidden(false);
							}
						}

					} catch (JiraException e) {
						MylarStatusHandler.log(e, "Can't retrieve editable fields for " + taskKey);
					}
					
					// TODO attribute need to be persisted and cleaned up after any submission/update
					taskData.addAttributeValue(JiraAttributeFactory.ATTRIBUTE_EDITOR_SYNC, "true");
					
					// TODO update operations and editable fields
					// ArrayList<RepositoryOperation> oldOperations = new ArrayList<RepositoryOperation>(taskData.getOperations());
					// taskData.getOperations().clear();
					
					// move into status listener?
					new UIJob(getName()) {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							JiraTaskEditor.super.createAttributeLayout(attributesComposite);
							JiraTaskEditor.super.addRadioButtons(buttonComposite);
							JiraTaskEditor.super.addActionButtons(buttonComposite);
							
							((ScrolledForm) JiraTaskEditor.super.getControl()).reflow(true);

							return Status.OK_STATUS;
						}
					}.schedule();
					
					return Status.OK_STATUS;
				}
				
			}.schedule();
		}
	}

	private boolean isTaskParametersSynchronized() {
		return taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_EDITOR_SYNC) != null;
	}
	
	@Override
	protected void createCustomAttributeLayout(Composite composite) {
		Label label = toolkit.createLabel(composite, "Components:");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

		final List componentsList = new List(composite, SWT.MULTI | SWT.V_SCROLL);
		componentsList.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		componentsList.setFont(TEXT_FONT);
		GridData compTextData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		compTextData.horizontalSpan = 1;
		compTextData.widthHint = 125;
		compTextData.heightHint = 40;
		componentsList.setLayoutData(compTextData);
		RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
		if (attribute != null && !attribute.getOptions().isEmpty()) {
			componentsList.setItems(attribute.getOptions().toArray(new String[1]));
			for (String compStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_COMPONENTS)) {
				componentsList.select(componentsList.indexOf(compStr));
			}
			componentsList.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					RepositoryTaskAttribute attribute = taskData
							.getAttribute(JiraAttributeFactory.ATTRIBUTE_COMPONENTS);
					attribute.clearValues();
					attribute.setValues(Arrays.asList(componentsList.getSelection()));
					attributeChanged(attribute);
				}
			});
		}

		label = toolkit.createLabel(composite, "Fix Versions:");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
		final List versionsList = new List(composite, SWT.MULTI | SWT.V_SCROLL);
		versionsList.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		versionsList.setFont(TEXT_FONT);
		GridData versionsTextData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		versionsTextData.horizontalSpan = 1;
		versionsTextData.widthHint = 125;
		versionsTextData.heightHint = 40;
		versionsList.setLayoutData(versionsTextData);
		attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
		if (attribute != null && !attribute.getOptions().isEmpty()) {
			versionsList.setItems(attribute.getOptions().toArray(new String[1]));
			for (String versionStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS)) {
				versionsList.select(versionsList.indexOf(versionStr));
			}
			versionsList.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					RepositoryTaskAttribute attribute = taskData
							.getAttribute(JiraAttributeFactory.ATTRIBUTE_FIXVERSIONS);
					attribute.clearValues();
					attribute.setValues(Arrays.asList(versionsList.getSelection()));
					attributeChanged(attribute);
				}
			});
		}

		label = toolkit.createLabel(composite, "Affects Versions:");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
		final List affectsVersionsList = new List(composite, SWT.MULTI | SWT.V_SCROLL);
		affectsVersionsList.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		affectsVersionsList.setFont(TEXT_FONT);
		GridData affectsVersionsTextData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		affectsVersionsTextData.horizontalSpan = 1;
		affectsVersionsTextData.widthHint = 125;
		affectsVersionsTextData.heightHint = 40;
		affectsVersionsTextData.verticalIndent = 3;
		affectsVersionsList.setLayoutData(affectsVersionsTextData);
		attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
		if (attribute != null && !attribute.getOptions().isEmpty()) {
			affectsVersionsList.setItems(attribute.getOptions().toArray(new String[1]));
			for (String versionStr : taskData.getAttributeValues(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS)) {
				affectsVersionsList.select(affectsVersionsList.indexOf(versionStr));
			}
			affectsVersionsList.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					RepositoryTaskAttribute attribute = taskData
							.getAttribute(JiraAttributeFactory.ATTRIBUTE_AFFECTSVERSIONS);
					attribute.clearValues();
					attribute.setValues(Arrays.asList(affectsVersionsList.getSelection()));
					attributeChanged(attribute);
				}
			});
		}
	}

//	@Override
//	public void submitToRepository() {
//		if (isDirty()) {
//			this.doSave(new NullProgressMonitor());
//		}
//		updateTask();
//		submitButton.setEnabled(false);
//		showBusy(true);
//
//		final JiraServer jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
//		if (jiraServer == null) {
//			submitButton.setEnabled(true);
//			JiraTaskEditor.this.showBusy(false);
//			return;
//		}
//
////		// TODO: build a new issue object rather then retrieving from server
////		final Issue issue = jiraServer.getIssue(this.getRepositoryTaskData().getAttributeValue(
////				JiraAttributeFactory.ATTRIBUTE_ISSUE_KEY));
////		if (issue == null) {
////			MylarStatusHandler.log("Unable to retrieve issue from repository", this);
////			submitButton.setEnabled(true);
////			JiraTaskEditor.this.showBusy(false);
////			return;
////		}
//
////		final String comment = getNewCommentText();
//		final AbstractRepositoryTask task = (AbstractRepositoryTask) TasksUiPlugin.getTaskListManager().getTaskList()
//				.getTask(AbstractRepositoryTask.getHandle(repository.getUrl(), getRepositoryTaskData().getId()));
//		final boolean attachContext = false;
//
//		JobChangeAdapter listener = new JobChangeAdapter() {
//			@Override
//			public void done(final IJobChangeEvent event) {
//				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						if (event.getJob().getResult().isOK()) {
//							if (attachContext) {
//								// TODO check for task == null
//								// TODO should be done as part of job
//								try {
//									connector.attachContext(repository, task, "");
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							}
//							//close();
//						} else {
//							submitButton.setEnabled(true);
//							JiraTaskEditor.this.showBusy(false);
//						}
//					}
//				});
//			}
//		};
//
//		Job submitJob = new Job(SUBMIT_JOB_LABEL) {
//
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					Issue issue = JiraRepositoryConnector.buildJiraIssue(getRepositoryTaskData(), jiraServer);
//
//					RepositoryOperation operation = getRepositoryTaskData().getSelectedOperation();
//					if (operation != null) {
//						if ("leave".equals(operation.getKnobName())) {
//							jiraServer.updateIssue(issue, comment);
//						} else if (org.eclipse.mylar.internal.jira.ui.model.Status.RESOLVED_ID.equals(operation.getKnobName())) {
//							String value = operation.getOptionValue(operation.getOptionSelection());
//							jiraServer.resolveIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(),
//									comment, JiraServer.ASSIGNEE_CURRENT, repository.getUserName());
//						} else if (org.eclipse.mylar.internal.jira.ui.model.Status.OPEN_ID.equals(operation.getKnobName())) {
//							jiraServer.reopenIssue(issue, comment, JiraServer.ASSIGNEE_CURRENT, repository
//									.getUserName());
//						} else if (org.eclipse.mylar.internal.jira.ui.model.Status.CLOSED_ID.equals(operation.getKnobName())) {
//							String value = operation.getOptionValue(operation.getOptionSelection());
//							jiraServer.closeIssue(issue, jiraServer.getResolutionById(value), issue.getFixVersions(),
//									comment, JiraServer.ASSIGNEE_CURRENT, repository.getUserName());
//						}
//
//					}
//
//					if (task != null) {
//						// XXX: HACK TO AVOID OVERWRITE WARNING
//						task.setTaskData(null);
//						TasksUiPlugin.getSynchronizationManager().synchronize(connector, task, true, new JobChangeAdapter() {
//
//							@Override
//							public void done(IJobChangeEvent event) {
//								close();
//								TasksUiUtil.openEditor(task, false);
//							}
//						});
//					} else {
//						//TasksUiUtil.openRepositoryTask(...);
//						close();
//					}
//					return Status.OK_STATUS;
//				} catch (Exception e) {
//					MylarStatusHandler.fail(e, "Error submitting to JIRA server", true);
//					return Status.OK_STATUS;
//				}
//			}
//
//		};
//
//		submitJob.addJobChangeListener(listener);
//		submitJob.schedule();
//	}

	@Override
	protected void validateInput() {
		// TODO Auto-generated method stub
	}

	protected String getActivityUrl() {
		if (taskData != null) {
			String taskId = taskData.getId();
			String repositoryUrl = taskData.getRepositoryUrl();
			if (repositoryUrl != null && taskId != null) {
				ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
				if (task != null) {
					return task.getTaskUrl() + "?page=history";
				}
			}
		}

		return super.getActivityUrl();
	}
	
	@Override
	protected boolean hasContentAssist(RepositoryTaskAttribute attribute) {
		String id = attribute.getID();
		if(id.startsWith(JiraAttributeFactory.ATTRIBUTE_CUSTOM_PREFIX)) {
			String key = attribute.getMetaDataValue("key");
			// TODO need more robust detection
			if("com.atlassian.jira.plugin.system.customfieldtypes:userpicker".equals(key)) {
				return true;
			}
		}
		
		return super.hasContentAssist(attribute);
	}
	
	@Override
	protected boolean hasContentAssist(RepositoryOperation repositoryOperation) {
		if("assignee".equals(repositoryOperation.getInputName())) {
			return true;
		}
		return super.hasContentAssist(repositoryOperation);
	}
	
}
