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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Set;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleTitleAndStatePart extends AbstractCrucibleEditorFormPart {

	private Review crucibleReview;

	private CrucibleReviewEditorPage crucibleEditor;

	private Composite parentComposite;

	private boolean newReview;

	private Text reviewTitleText;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleReview = review;
		this.crucibleEditor = editor;
		this.newReview = isNewReview;
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return null;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		// CHECKSTYLE:MAGIC:OFF

		parentComposite = new Composite(parent, SWT.NONE);
		toolkit.adapt(parentComposite);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults()
				.spacing(10, 10)
				.equalWidth(true)
				.numColumns(2)
				.create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentComposite);

		updateControl(this.crucibleReview, parent, toolkit);

		return parentComposite;
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite parent, String value, String labelString,
			boolean isMultiline) {
		return createText(toolkit, parent, value, labelString, isMultiline, true);
	}

	private Text createText(FormToolkit toolkit, Composite parent, String value, String labelString,
			boolean isMultiline, boolean isReadOnly) {

		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}
		int style = SWT.FLAT;
		if (isReadOnly) {
			style |= SWT.READ_ONLY;
		} else {
			style |= SWT.BORDER;
		}
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL;
		}
		Text text = new Text(parent, style);
		text.setFont(JFaceResources.getDefaultFont());
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		return text;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite parent, String labelString) {
		Label labelControl = toolkit.createLabel(parent, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

	private Control createUserComboControl(FormToolkit toolkit, Composite parent, String labelString,
			final User selectedUser, boolean readOnly, final ReviewAttributeType reviewAttributeType) {
		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}

		Set<User> users = CrucibleUiUtil.getCachedUsers(crucibleReview);

		Control control;
		if (readOnly) {
			final Text text = new Text(parent, SWT.READ_ONLY);
			text.setFont(JFaceResources.getTextFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setText(selectedUser.getDisplayName());
			text.setData(selectedUser);
			text.setEditable(false);
			control = text;
			toolkit.adapt(text, true, true);
		} else {
			final CCombo combo = new CCombo(parent, SWT.BORDER);
			combo.setEditable(false);
			ComboViewer comboViewer = new ComboViewer(combo);
			comboViewer.setLabelProvider(new CrucibleUserLabelProvider());
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setInput(users);
			comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection selection = event.getSelection();
					if (selection instanceof IStructuredSelection) {
						User user = ((User) ((IStructuredSelection) selection).getFirstElement());
						if (user.getUsername().equals(selectedUser.getUsername())) {
							changedAttributes.remove(reviewAttributeType);
						} else {
							changedAttributes.put(reviewAttributeType, user);
						}
						crucibleEditor.attributesModified();
					}
				}
			});

			comboViewer.setSelection(new StructuredSelection(selectedUser));
			control = combo;
		}
		control.setFont(JFaceResources.getDefaultFont());
		control.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		toolkit.adapt(control, true, true);
		return control;
	}

	@Override
	public void updateControl(Review review, Composite parent, final FormToolkit toolkit) {
		this.crucibleReview = review;
		if (parentComposite == null) {
			createControl(parent, toolkit);
		}

		for (Control c : parentComposite.getChildren()) {
			c.dispose();
			// TODO disposing not necessary, simply updating labels and a re-layout should be sufficient; low priority though
		}
		parentComposite.setMenu(null);

		reviewTitleText = createText(toolkit, parentComposite, crucibleReview.getName(), null, false, !newReview);
		reviewTitleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String modifiedName = ((Text) e.widget).getText();
				if (modifiedName.equals(crucibleReview.getName())) {
					changedAttributes.remove(ReviewAttributeType.TITLE);
				} else {
					changedAttributes.put(ReviewAttributeType.TITLE, modifiedName);
				}
				crucibleEditor.attributesModified();
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(reviewTitleText);

		Composite statusComp = toolkit.createComposite(parentComposite);
		final DateTime dueDate = crucibleReview.getDueDate();
		final int numColumns = 2 * (dueDate != null ? 3 : 2);
		statusComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(numColumns).spacing(10, 0).create());
		Text stateText = createReadOnlyText(toolkit, statusComp, crucibleReview.getState().getDisplayName(), "State: ",
				false);
		GridDataFactory.fillDefaults().applyTo(stateText);

		Text openSinceText = createReadOnlyText(toolkit, statusComp, DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(crucibleReview.getCreateDate()), "Open Since: ", false);
		GridDataFactory.fillDefaults().applyTo(openSinceText);

		if (dueDate != null) {
			final Text dueDateText = createReadOnlyText(toolkit, statusComp, DateFormat.getDateTimeInstance(
					DateFormat.MEDIUM, DateFormat.SHORT).format(crucibleReview.getDueDate().toDate()), "Due Date: ",
					false);
			GridDataFactory.fillDefaults().applyTo(dueDateText);
		}

		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(statusComp);

		// CHECKSTYLE:MAGIC:ON

		toolkit.paintBordersFor(parentComposite);
	}

	@Override
	public void setFocus() {
		reviewTitleText.setFocus();
	}
}
