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
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

	private static int idIncrementor = 1;

	private static final int DEFECT_ID = IDialogConstants.CLIENT_ID + idIncrementor++;

	private static final int DRAFT_ID = IDialogConstants.CLIENT_ID + idIncrementor++;

	private final Review review;

	private final Comment replyToComment;

	private final boolean edit; //temporary flag for marking if a comment is edited or a new one created

	private final HashMap<Integer, Button> customButtons;

	private final HashMap<CustomFieldDef, ComboViewer> customCombos;

	private final HashMap<String, CustomField> customFieldSelections;

	private boolean draft = false;

	private boolean defect = false;

	private Text commentText;

	private String newComment;

	public AddCommentPart(Review review, Comment replyToComment) {
		this.review = review;
		this.replyToComment = replyToComment;
		this.edit = false;
		customButtons = new HashMap<Integer, Button>();
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	public Composite createControl(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF

		// create composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		// create message
		if (review != null) {
			Label label = new Label(composite, SWT.WRAP);
			if (replyToComment == null) {
				label.setText("Create comment:");
			} else {
				label.setText("Reply to:\n" + replyToComment.getMessage());
			}
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
//			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
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

	protected void buttonPressed(int buttonId) {
		if (buttonId == DRAFT_ID) {
			draft = true;
			buttonId = Window.OK;
		} else if (buttonId == DEFECT_ID) {
			defect = !defect;
			//toggle combos
			for (CustomFieldDef field : customCombos.keySet()) {
				customCombos.get(field).getCombo().setEnabled(defect);
			}
		} else if (buttonId == Window.CANCEL) {
			newComment = "";
		}

		if (buttonId == Window.OK) {
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
			notifyOk();
		} else if (buttonId == Window.CANCEL) {
			newComment = "";
			notifyCanceled();
		}

	}

	private IAddCommentPartListener listener;

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

			createButton(composite, SWT.CHECK, DEFECT_ID, "Defect", false);
			//add custom fields
			addCustomFields(composite);
		}
		createButton(parent, IDialogConstants.OK_ID, SAVE_LABEL, true);
		if (!edit) { //if it is a new reply, saving as draft is possible
			createButton(parent, DRAFT_ID, DRAFT_LABEL, false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		((GridLayout) parent.getLayout()).numColumns = 3;
		//CHECKSTYLE:MAGIC:ON
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
//		buttons.put(new Integer(id), button);
//		setButtonLayoutData(button);
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
				int customFieldID = idIncrementor++;
				createCombo(parent, customFieldID, customField, 0);
			}
		}
	}

	protected Button createButton(Composite parent, int style, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, style);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		customButtons.put(new Integer(id), button);

		return button;
	}

	protected void createCombo(Composite parent, final int id, final CustomFieldDef customField, int selection) {
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
