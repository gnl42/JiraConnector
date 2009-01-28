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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.HashMap;
import java.util.List;

/**
 * Generic part for use when adding a comment to a review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class AddCommentPart {

	public interface IAddCommentPartListener {
		void cancelAddComment();

		void addComment();
	}

	private static final String SAVE_LABEL = "Save";

	private static final String DRAFT_LABEL = "Save as Draft";

	private final Review review;

	private final Comment replyToComment;

	private final boolean edit; //temporary flag for marking if a comment is edited or a new one created

	private final HashMap<CustomFieldDef, ComboViewer> customCombos;

	private final HashMap<String, CustomField> customFieldSelections;

	private boolean draft = false;

	private boolean defect = false;

	private Text commentText;

	private String newComment;

	private IAddCommentPartListener listener;

	public AddCommentPart(Review review, Comment replyToComment) {
		this.review = review;
		this.replyToComment = replyToComment;
		this.edit = false;
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	public Composite createControl(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		if (review != null) {
			Label label = new Label(composite, SWT.WRAP);
			if (replyToComment == null) {
				label.setText("Create comment:");
			} else {
				label.setText("Reply to:\n" + replyToComment.getMessage());
			}
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}
		commentText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 80;
		commentText.setLayoutData(textGridData);
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, true).applyTo(buttonComposite);
		createButtonsForButtonBar(buttonComposite);
		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

	public void setListener(IAddCommentPartListener listener) {
		this.listener = listener;
	}

	private void notifyCanceled() {
		listener.cancelAddComment();
	}

	private void notifyOk() {
		listener.addComment();

	}

	protected void createButtonsForButtonBar(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF
		((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
		// create buttons according to (implicit) reply type
		if (replyToComment == null) { //"defect button" needed if new comment
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(composite);
			createDefectButton(composite);
			addCustomFields(composite);
		}
		createButton(parent, SAVE_LABEL, true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				processFields();
				notifyOk();
			}
		});
		if (!edit) { //if it is a new reply, saving as draft is possible
			createButton(parent, DRAFT_LABEL, false, new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					draft = true;
					processFields();
					notifyOk();
				}
			});
		}
		createButton(parent, IDialogConstants.CANCEL_LABEL, false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyCanceled();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 3;
		//CHECKSTYLE:MAGIC:ON
	}

	protected void processFields() {
		newComment = commentText.getText();
		if (defect) { //process custom field selection only when defect is selected
			for (CustomFieldDef field : customCombos.keySet()) {
				CustomFieldValue customValue = (CustomFieldValue) customCombos.get(field).getElementAt(
						customCombos.get(field).getCombo().getSelectionIndex());
				if (customValue != null) {
					CustomFieldBean bean = new CustomFieldBean();
					bean.setConfigVersion(field.getConfigVersion());
					bean.setValue(customValue.getName());
					customFieldSelections.put(field.getName(), bean);
				}
			}
		}
	}

	protected Button createButton(Composite parent, String label, boolean defaultButton,
			SelectionListener buttonListener) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(buttonListener);
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		return button;
	}

	public HashMap<String, CustomField> getCustomFieldSelections() {
		return customFieldSelections;
	}

	public boolean isDraft() {
		return draft;
	}

	public boolean isDefect() {
		return defect;
	}

	private void addCustomFields(Composite parent) {
		if (review == null) {
			return;
		}
		List<CustomFieldDef> customFields = CrucibleCorePlugin.getDefault().getReviewCache().getMetrics(
				review.getMetricsVersion());
		if (customFields == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					"Could not retrieve custom fields for review: " + review.getName()) {
			});
		} else {
			for (CustomFieldDef customField : customFields) {
				createCombo(parent, customField, 0);
			}
		}
	}

	protected Button createDefectButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.CHECK);
		button.setText("Defect");
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				defect = !defect;
				//toggle combos
				for (CustomFieldDef field : customCombos.keySet()) {
					customCombos.get(field).getCombo().setEnabled(defect);
				}
			}
		});
		return button;
	}

	protected void createCombo(Composite parent, final CustomFieldDef customField, int selection) {
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
		comboViewer.setInput(customField.getValues());
		comboViewer.getCombo().setEnabled(false);
		customCombos.put(customField, comboViewer);
	}

	public String getValue() {
		return newComment;
	}

}
