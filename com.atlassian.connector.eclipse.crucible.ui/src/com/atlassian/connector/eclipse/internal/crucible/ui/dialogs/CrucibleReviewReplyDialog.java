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
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.HashMap;
import java.util.List;

/**
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewReplyDialog extends Dialog {

	private static final String SAVE_LABEL = "Save";

	private static final String DRAFT_LABEL = "Save as Draft";

	private static int idIncrementor = 1;

	private static final int DEFECT_ID = IDialogConstants.CLIENT_ID + idIncrementor++;

	private static final int DRAFT_ID = IDialogConstants.CLIENT_ID + idIncrementor++;

	protected final Review review;

	protected final Comment replyToComment;

	protected final CrucibleFile file;

	protected final LineRange lineRange;

	protected final boolean edit; //temporary flag for marking if a comment is edited or a new one created

	private final HashMap<Integer, Button> customButtons;

	private final HashMap<CustomFieldDef, ComboViewer> customCombos;

	protected HashMap<String, CustomField> customFieldSelections;

	protected boolean draft = false;

	protected boolean defect = false;

	protected String value;

	protected Text text;

	/**
	 * Add new general comment
	 * 
	 * @param parentShell
	 * @param review
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review) {
		this(parentShell, "Add general comment " + review.getName(), review, null, null, null);
	}

	/**
	 * Add new general comment reply
	 * 
	 * @param parentShell
	 * @param review
	 * @param comment
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, Comment replyToComment) {
		this(parentShell, "Add a reply to: ", review, null, replyToComment, null);
	}

	/**
	 * Add new versioned comment on a file
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file) {
		this(parentShell, "Add versioned comment on file: "
				+ file.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl(), review, file, null, null);
	}

	/**
	 * Add new versioned comment reply on a file
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param comment
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, Comment replyToComment) {
		this(parentShell, "Reply to: ", review, file, replyToComment, null);
	}

	/**
	 * Add new versioned comment on a specific line of code
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param replyToComment
	 * @param lineRange
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, LineRange lineRange) {
		this(parentShell, "Add new versioned comment on line(s) " + String.valueOf(lineRange.getStartLine()) + "-"
				+ String.valueOf(lineRange.getStartLine() + lineRange.getNumberOfLines()), review, file, null,
				lineRange);
	}

	/**
	 * Add new versioned comment reply on a specific line of code
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param comment
	 * @param lineRange
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, Comment replyToComment,
			LineRange lineRange) {
		this(parentShell, "Reply to: ", review, file, replyToComment, lineRange);
	}

	private CrucibleReviewReplyDialog(Shell parentShell, String dialogMessage, Review review, CrucibleFile file,
			Comment replyToComment, LineRange lineRange) {
		super(parentShell);
		this.review = review;
		this.replyToComment = replyToComment;
		this.file = file;
		this.lineRange = lineRange;
		this.edit = false;
		customButtons = new HashMap<Integer, Button>();
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (review != null && review.getName() != null) {
			shell.setText(review.getName());
		}
	}

	protected int getInputTextStyle() {
		return SWT.MULTI | SWT.BORDER;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
		// create buttons according to (implicit) reply type
		if (replyToComment == null) { //"defect button" needed if new comment
			createButton(parent, SWT.CHECK, CrucibleReviewReplyDialog.DEFECT_ID, "Defect", false);
			//add custom fields
			addCustomFields(parent);
		}
		createButton(parent, IDialogConstants.OK_ID, SAVE_LABEL, true);
		if (!edit) { //if it is a new reply, saving as draft is possible
			createButton(parent, CrucibleReviewReplyDialog.DRAFT_ID, DRAFT_LABEL, false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
				int customFieldID = CrucibleReviewReplyDialog.idIncrementor++;
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
		setButtonLayoutData(button);
		return button;
	}

	protected void createCombo(Composite parent, final int id, final CustomFieldDef customField, int selection) {
		((GridLayout) parent.getLayout()).numColumns++;
		Label label = new Label(parent, SWT.NONE);
		label.setText("Select " + customField.getName());

		((GridLayout) parent.getLayout()).numColumns++;
		ComboViewer comboViewer = new ComboViewer(parent);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new ILabelProvider() {

			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				CustomFieldValue fieldValue = (CustomFieldValue) element;
				return fieldValue.getName();
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
		comboViewer.setInput(customField.getValues());
		comboViewer.getCombo().setEnabled(false);

		customCombos.put(customField, comboViewer);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CrucibleReviewReplyDialog.DRAFT_ID) {
			draft = true;
			setReturnCode(Window.OK);
			close();
		} else if (buttonId == CrucibleReviewReplyDialog.DEFECT_ID) {
			defect = !defect;
			//toggle combos
			for (CustomFieldDef field : customCombos.keySet()) {
				customCombos.get(field).getCombo().setEnabled(defect);
			}
		} else if (buttonId == Window.CANCEL) {
			value = "";
		}

		if (buttonId == Window.OK) {
			value = text.getText();
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
		} else if (buttonId == Window.CANCEL) {
			value = "";
		}
		super.buttonPressed(buttonId);
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

	public String getValue() {
		return value;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);
		// create message
		if (review != null) {
			Label label = new Label(composite, SWT.WRAP);
			if (replyToComment == null) {
				label.setText("Create comment:");
			} else {
				label.setText("Reply to:\n" + replyToComment.getMessage());
			}
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}
		text = new Text(composite, getInputTextStyle());
		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 80;
		text.setLayoutData(textGridData);
		applyDialogFont(composite);
		return composite;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}
