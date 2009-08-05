/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.UpdateCommentRemoteOperation;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog shown to the user when they add a comment to a review
 * 
 * @author Wojciech Seliga
 */
public class CrucibleEditCommentDialog extends ProgressDialog {

	private class UpdateCommentRunnable implements IRunnableWithProgress {

		private final boolean shouldPostIfDraft;

		public UpdateCommentRunnable(boolean shouldPostIfDraft) {
			this.shouldPostIfDraft = shouldPostIfDraft;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				monitor.beginTask("Updating comment", IProgressMonitor.UNKNOWN);

				try {
					client.execute(new UpdateCommentRemoteOperation(taskRepository, review, prepareNewComment(comment,
							shouldPostIfDraft), monitor));
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Unable to update comment",
							e));
					throw e; //rethrow exception so dialog stays open and displays error message
				}
				client.getReview(getTaskRepository(), getTaskId(), true, monitor);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);

			}

		}
	}

	private final Review review;

	private final Comment comment;

	private final String shellTitle;

	private final TaskRepository taskRepository;

	private final String taskKey;

	private final String taskId;

	private final CrucibleClient client;

	private static final String UPDATE_LABEL = "&Update";

	private static final String DEFECT_LABEL = "Defect";

	private static final String DRAFT_LABEL = "Update && &Post";

	private final HashMap<CustomFieldDef, ComboViewer> customCombos;

	private final HashMap<String, CustomField> customFieldSelections;

//	private CommentPart commentPart;
	private FormToolkit toolkit;

	private boolean defect;

	private Text commentText;

	private String newComment;

	private Button defectButton;

	private Button updateButton;

	private Button saveDraftButton;

	public CrucibleEditCommentDialog(Shell parentShell, String shellTitle, Review review, Comment comment,
			String taskKey, String taskId, TaskRepository taskRepository, CrucibleClient client) {
		super(parentShell);
		this.shellTitle = shellTitle;
		this.review = review;
		if (comment == null) {
			throw new IllegalArgumentException("Comment must not be null");
		}
		this.comment = comment;
		this.taskKey = taskKey;
		this.taskId = taskId;
		this.taskRepository = taskRepository;
		this.client = client;
		this.defect = comment.isDefectRaised();
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	private Comment prepareNewComment(Comment oldComment, boolean shouldPostIfDraft) {
		final CommentBean commentBean;
		if (oldComment instanceof VersionedComment) {
			commentBean = new VersionedCommentBean((VersionedComment) oldComment);
		} else if (oldComment instanceof GeneralComment) {
			commentBean = new GeneralCommentBean((GeneralComment) oldComment);
		} else {
			throw new IllegalArgumentException("Unhandled type of comment class "
					+ oldComment.getClass().getSimpleName());
		}

		commentBean.setMessage(newComment);
		commentBean.getCustomFields().clear();
		commentBean.getCustomFields().putAll(customFieldSelections);
		commentBean.setAuthor(new User(client.getUserName()));
		commentBean.setDefectRaised(defect);
		if (commentBean.isDraft() && shouldPostIfDraft) {
			commentBean.setDraft(false);
		}
		return commentBean;
	}

	@Override
	protected Control createPageControls(Composite parent) {
		getShell().setText(shellTitle);
		setTitle(shellTitle);
		if (comment.isReply()) {
			setMessage("Update reply");
		} else {
			setMessage("Update comment");
		}

		//CHECKSTYLE:MAGIC:OFF
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		if (toolkit == null) {
			toolkit = new FormToolkit(getShell().getDisplay());
		}
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (toolkit != null) {
					toolkit.dispose();
				}
			}
		});

//		if (commentPart != null) {
//			commentPart.disableToolbar();
//
//			ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
//			scrolledComposite.setExpandHorizontal(true);
//
//			scrolledComposite.setBackground(toolkit.getColors().getBackground());
//			GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 100).applyTo(scrolledComposite);
//
//			Composite commentComposite = toolkit.createComposite(scrolledComposite, SWT.NONE);
//			commentComposite.setLayout(new GridLayout());
//			scrolledComposite.setContent(commentComposite);
//
//			Control commentControl = commentPart.createControl(commentComposite, toolkit);
//			commentComposite.setSize(commentControl.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//
//		}

		commentText = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		commentText.setText(comment.getMessage());
		commentText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateButtonsState();
			}

		});
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 100;
		textGridData.widthHint = 500;
		commentText.setLayoutData(textGridData);
		commentText.forceFocus();

		if (!comment.isReply()) {

			((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
			// create buttons according to (implicit) reply type
			int nrOfCustomFields = 0;
			Composite compositeCustomFields = new Composite(composite, SWT.NONE);
			compositeCustomFields.setLayout(new GridLayout(1, false));
			createDefectButton(compositeCustomFields);
			nrOfCustomFields = addCustomFields(compositeCustomFields);
			GridDataFactory.fillDefaults().grab(true, false).span(nrOfCustomFields + 1, 1).applyTo(
					compositeCustomFields);
		}
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(composite);
		return composite;
	}

	//CHECKSTYLE:MAGIC:ON

	private void updateButtonsState() {
		processFields();
		boolean areMetricsModified = !comment.isReply()
				&& (defect != comment.isDefectRaised() || !customFieldSelections.equals(comment.getCustomFields()));
		boolean isModified = !commentText.getText().equals(comment.getMessage()) || areMetricsModified;
		if (updateButton != null && !updateButton.isDisposed()) {
			updateButton.setEnabled(isModified);
		}

		if (saveDraftButton != null && !saveDraftButton.isDisposed()) {
			saveDraftButton.setEnabled(isModified);
		}
	}

	@Override
	protected Collection<? extends Control> getDisableableControls() {
		Set<Control> controls = new HashSet<Control>(super.getDisableableControls());
		if (customCombos.size() > 0) {
			for (ComboViewer viewer : customCombos.values()) {
				controls.add(viewer.getControl());
			}
		}

		if (defectButton != null) {
			controls.add(defectButton);
		}

		return controls;
	}

	protected void processFields() {
		newComment = commentText.getText();
		customFieldSelections.clear();
		if (defect) { //process custom field selection only when defect is selected
			for (CustomFieldDef field : customCombos.keySet()) {
				CustomFieldValue customValue = (CustomFieldValue) customCombos.get(field).getElementAt(
						customCombos.get(field).getCombo().getSelectionIndex());
				if (customValue != null/* && customValue != EMPTY_CUSTOM_FIELD_VALUE*/) {
					CustomFieldBean bean = new CustomFieldBean();
					bean.setConfigVersion(field.getConfigVersion());
					bean.setValue(customValue.getName());
					customFieldSelections.put(field.getName(), bean);
				}
			}
		}
	}

	private int addCustomFields(Composite parent) {
		if (review == null) {
			return 0;
		}
		List<CustomFieldDef> customFields = CrucibleCorePlugin.getDefault().getReviewCache().getMetrics(
				review.getMetricsVersion());
		if (customFields == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					"Metrics are for review version are not cached: " + review.getMetricsVersion() + " "
							+ review.getName(), null));
			return 0;
		} else {
			for (CustomFieldDef customField : customFields) {
				createCombo(parent, customField);
			}
			updateComboEnablement();
			return customFields.size();
		}
	}

	protected Button createDefectButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		defectButton = new Button(parent, SWT.CHECK);
		defectButton.setText(DEFECT_LABEL);
		defectButton.setFont(JFaceResources.getDialogFont());
		defectButton.setSelection(defect);
		defectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				defect = !defect;
				//toggle combos
				updateComboEnablement();
				updateButtonsState();
			}

		});
		return defectButton;
	}

	private void updateComboEnablement() {
		for (CustomFieldDef field : customCombos.keySet()) {
			customCombos.get(field).getCombo().setEnabled(defect);
		}
	}

	private static final CustomFieldValue EMPTY_CUSTOM_FIELD_VALUE = new CustomFieldValue("", null);

	protected void createCombo(Composite parent, final CustomFieldDef customField) {
		((GridLayout) parent.getLayout()).numColumns++;
		Label label = new Label(parent, SWT.NONE);
		label.setText("Select " + customField.getName());
		((GridLayout) parent.getLayout()).numColumns++;
		ComboViewer comboViewer = new ComboViewer(parent);
		comboViewer.setContentProvider(new ArrayContentProvider());

		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				CustomFieldValue fieldValue = (CustomFieldValue) element;
				return fieldValue.getName();
			}
		});
		final ArrayList<CustomFieldValue> values = MiscUtil.buildArrayList(EMPTY_CUSTOM_FIELD_VALUE);
		values.addAll(customField.getValues());
		comboViewer.setInput(values);
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsState();
			}
		});

		// setting default values for combo 
		final CustomField commentCustomField = comment.getCustomFields().get(customField.getName());
		if (commentCustomField != null) {
			for (CustomFieldValue value : customField.getValues()) {
				if (value.getName().equals(commentCustomField.getValue())) {
					ISelection selection = new StructuredSelection(MiscUtil.buildArrayList(value));
					comboViewer.setSelection(selection, true);
					break;
				}
			}
		} else {
			comboViewer.setSelection(new StructuredSelection(MiscUtil.buildArrayList(EMPTY_CUSTOM_FIELD_VALUE)), true);
		}

		customCombos.put(customField, comboViewer);
	}

	public void updateComment(boolean shouldPostIfDraft) {

		try {
			processFields();
			setMessage("");
			run(true, false, new UpdateCommentRunnable(shouldPostIfDraft));
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to update the comment");
			return;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to update the comment");
			return;
		}

		setReturnCode(Window.OK);
		close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		updateButton = createButton(parent, IDialogConstants.CLIENT_ID + 2, UPDATE_LABEL, false);
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComment(false);
			}
		});
		updateButton.setEnabled(false);
		if (comment.isDraft()) {
			saveDraftButton = createButton(parent, IDialogConstants.CLIENT_ID + 2, DRAFT_LABEL, false);
			saveDraftButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateComment(true);
				}
			});
			saveDraftButton.setEnabled(false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						cancelPressed();
					}
				});
	}

	public void cancelUpdateComment() {
		setReturnCode(Window.CANCEL);
		close();
	}

	public String getTaskKey() {
		return taskKey;
	}

	public String getTaskId() {
		return taskId;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}
}
