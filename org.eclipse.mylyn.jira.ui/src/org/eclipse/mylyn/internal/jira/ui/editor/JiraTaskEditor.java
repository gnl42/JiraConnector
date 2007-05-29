/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraFieldType;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	private Composite attributesComposite;
	private Composite buttonComposite;

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
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
		// removing common attributes section

		// XXX remove this when patch for AbstractRepositoryTaskEditor is committed 
		GridLayout attributesLayout = new GridLayout();
		attributesLayout.numColumns = 4;
		attributesLayout.horizontalSpacing = 5;
		attributesLayout.verticalSpacing = 4;
		attributesComposite.setLayout(attributesLayout);
		GridData attributesData = new GridData(GridData.FILL_BOTH);
		attributesData.horizontalSpan = 1;
		attributesData.grabExcessVerticalSpace = false;
		attributesComposite.setLayoutData(attributesData);
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
					
					Set<RepositoryTaskAttribute> updated = new HashSet<RepositoryTaskAttribute>();
					
					try {
						HashSet<String> editableKeys = new HashSet<String>();
						RepositoryTaskAttribute[] editableAttributes = server.getEditableAttributes(taskKey);
						if(editableAttributes!=null) {
							for (RepositoryTaskAttribute attribute : editableAttributes) {
								editableKeys.add(attributeFactory.mapCommonAttributeKey(attribute.getID()));
							}
						}

						System.err.println(taskData.getTaskKey());
						for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
							boolean editable = editableKeys.contains(attribute.getID().toLowerCase());
							System.err.println("  " + attribute.getID() + " : " + attribute.getName() + " : " + editable);
							attribute.setReadOnly(!editable);
							if (editable && !attributeFactory.getIsHidden(attribute.getID())) {
								attribute.setHidden(false);
							}
							
							// make attributes read-only if can't find editing options
							JiraFieldType type = JiraFieldType.valueByKey(attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY));
							Collection<String> options = attribute.getOptions();
							if (type.equals(JiraFieldType.SELECT)
									&& (options == null || options.isEmpty() || attribute.isReadOnly())) {
								attribute.setReadOnly(true);
							} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
								attribute.setReadOnly(true);
							}
							
							updated.add(attribute);
						}
						
					} catch (JiraException e) {
						MylarStatusHandler.log(e, "Can't retrieve editable fields for " + taskKey);
					}
					
					
					RepositoryTaskAttribute editorSyncAttribute = new RepositoryTaskAttribute( //
							JiraAttributeFactory.ATTRIBUTE_EDITOR_SYNC, "editor sync", true);
					editorSyncAttribute.setValue("true");
					taskData.addAttribute(JiraAttributeFactory.ATTRIBUTE_EDITOR_SYNC, editorSyncAttribute);
					updated.add(editorSyncAttribute);

					// TODO attribute need to be persisted and cleaned up after any submission/update
					
					// this wont work because it change synchronization state and mark attributes as changed
					// TasksUiPlugin.getSynchronizationManager().saveOutgoing(repositoryTask, changedAttributes);

					// this doesn't work either because it mark all attributes as changed
					// if (getRepositoryTask() != null) {
					// 	TasksUiPlugin.getDefault().getTaskDataManager().saveEdits(getRepositoryTask().getHandleIdentifier(), updated);
					// }
					
					// TODO update operations and editable fields
					// ArrayList<RepositoryOperation> oldOperations = new ArrayList<RepositoryOperation>(taskData.getOperations());
					// taskData.getOperations().clear();
					
					// move into status listener?
					new UIJob(getName()) {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							// createAttributeLayout();
							createCustomAttributeLayout();
							
							createActionsLayout();
							
							((ScrolledForm) JiraTaskEditor.super.getControl()).reflow(true);

							return Status.OK_STATUS;
						}

						private void createActionsLayout() {
							JiraTaskEditor.super.addRadioButtons(buttonComposite);
							JiraTaskEditor.super.addActionButtons(buttonComposite);
						}

//						private void createAttributeLayout() {
//							JiraTaskEditor.super.createAttributeLayout(attributesComposite);
//						}

						private void createCustomAttributeLayout() {
							JiraTaskEditor.this.createCustomAttributeLayout(attributesComposite);
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
	protected void createCustomAttributeLayout(Composite attributesComposite) {
		if(!isTaskParametersSynchronized()) {
			return;
		}
		
		int numColumns = ((GridLayout) attributesComposite.getLayout()).numColumns;
		int currentCol = 1;
		
		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()) {
				continue;
			}
			
			JiraFieldType type = JiraFieldType.valueByKey(attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY));
			Collection<String> options = attribute.getOptions();
			if (type.equals(JiraFieldType.SELECT) && (options == null || options.isEmpty() || attribute.isReadOnly())) {
				type = JiraFieldType.TEXTFIELD;
			} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
				type = JiraFieldType.TEXTFIELD;
			}
			
			switch (type) {
			    case TEXTAREA:
			    	// all text areas go to the bottom
					break;
				case SELECT: 
				{
					Label label = createLabel(attributesComposite, attribute);
					GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
					
					CCombo combo = new CCombo(attributesComposite, SWT.FLAT | SWT.READ_ONLY);
					toolkit.adapt(combo, true, true);
					combo.setFont(TEXT_FONT);
					combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

					GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
					data.horizontalSpan = 1;
					data.widthHint = 140;
					data.horizontalIndent = HORZ_INDENT;
					combo.setLayoutData(data);

					if (attribute.getOptions() != null) {
						for (String val : attribute.getOptions()) {
							combo.add(val);
						}
					}

					String value = checkText(attribute.getValue());
					if (combo.indexOf(value) != -1) {
						combo.select(combo.indexOf(value));
					}
					combo.addSelectionListener(new ComboSelectionListener(combo));
					comboListenerMap.put(combo, attribute);

					if (hasChanged(attribute)) {
						combo.setBackground(backgroundIncoming);
					}
					
					currentCol += 2;
					break;
				}
				case MULTISELECT: 
				{
					Label label = createLabel(attributesComposite, attribute);
					GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

					final List list = new List(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL);
					toolkit.adapt(list, true, true);
					list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
					list.setFont(TEXT_FONT);

					GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
					data.horizontalSpan = 1;
					data.widthHint = 125;
					data.heightHint = 45;
					data.horizontalIndent = HORZ_INDENT;
					list.setLayoutData(data);

					if (!attribute.getOptions().isEmpty()) {
						list.setItems(attribute.getOptions().toArray(new String[1]));
						for (String value : attribute.getValues()) {
							list.select(list.indexOf(value));
						}
						final RepositoryTaskAttribute attr = attribute;
						list.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								attr.clearValues();
								attr.setValues(Arrays.asList(list.getSelection()));
								attributeChanged(attr);
							}
						});
						list.showSelection();
					}
					
					if (hasChanged(attribute)) {
						list.setBackground(backgroundIncoming);
					}
					
					currentCol += 2;
					break;
				}

				// TEXTFIELD and everything else 
				default:
				{
					Label label = createLabel(attributesComposite, attribute);
					GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
					
					int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;
					Text text = createTextField(attributesComposite, attribute, SWT.FLAT | style);

					GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
					data.horizontalSpan = 1;
					data.widthHint = 135;
					data.horizontalIndent = HORZ_INDENT;
					text.setLayoutData(data);
					
					if (hasContentAssist(attribute)) {
						ContentAssistCommandAdapter adapter = applyContentAssist(text,
								createContentProposalProvider(attribute));
						
						ILabelProvider propsalLabelProvider = createProposalLabelProvider(attribute);
						if(propsalLabelProvider != null){
							adapter.setLabelProvider(propsalLabelProvider);
						}
						adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
					}
					
					if (hasChanged(attribute)) {
						text.setBackground(backgroundIncoming);
					}

					currentCol += 2;
				}
			}
			
			if (currentCol > numColumns) {
				currentCol -= numColumns;
			}
		}

		if (currentCol > 1) {
			while (currentCol <= numColumns) {
				toolkit.createLabel(attributesComposite, "");
				currentCol++;
			}
		}

		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden() || !JiraFieldType.TEXTAREA.getKey().equals(attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY))) {
				continue;
			}

			Label label = createLabel(attributesComposite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

			int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;

			// TextViewer viewer = addTextEditor(repository, attributesComposite, attribute.getValue(), true, SWT.FLAT | SWT.BORDER | SWT.MULTI | SWT.WRAP | style);
			TextViewer viewer = new TextViewer(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | style);
			viewer.setDocument(new Document(attribute.getValue()));
			
			final StyledText text = viewer.getTextWidget();
			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

			// GridDataFactory.fillDefaults().span(3, 1).hint(300, 40).applyTo(text);
			GridData data = new GridData(SWT.LEFT, SWT.TOP, true, false);
			data.horizontalSpan = 3;
			data.widthHint = 380;
			data.heightHint = 55;
			data.horizontalIndent = HORZ_INDENT;
			text.setLayoutData(data);
			
			toolkit.adapt(text, true, true);
			
			if(attribute.isReadOnly()) {
				viewer.setEditable(false);
			} else {
				viewer.setEditable(true);
				text.setData(attribute);
				text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String newValue = text.getText();
						RepositoryTaskAttribute attribute = (RepositoryTaskAttribute) text.getData();
						attribute.setValue(newValue);
						attributeChanged(attribute);
					}
				});				
			}
			
			if (hasChanged(attribute)) {
				text.setBackground(backgroundIncoming);
			}
		}		
		
		toolkit.paintBordersFor(attributesComposite);
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
			String key = attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY);
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
