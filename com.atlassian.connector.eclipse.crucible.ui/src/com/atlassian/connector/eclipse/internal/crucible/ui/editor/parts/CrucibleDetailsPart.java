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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.text.DateFormat;
import java.util.Collection;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleDetailsPart extends AbstractCrucibleEditorFormPart {

	private Review crucibleReview;

	private CrucibleReviewEditorPage crucibleEditor;

	private Composite parentComposite;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
		this.crucibleReview = review;
		this.crucibleEditor = editor;
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
		//CHECKSTYLE:MAGIC:OFF

		parentComposite = new Composite(parent, SWT.NONE);
		toolkit.adapt(parentComposite);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		parentComposite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parentComposite);

		updateControl(this.crucibleReview, parent, toolkit);

		return parentComposite;
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite parent, String value, String labelString,
			boolean isMultiline) {

		if (labelString != null) {
			createLabelControl(toolkit, parent, labelString);
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP;
		}
		Text text = new Text(parent, style);
		text.setFont(EditorUtil.TEXT_FONT);
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

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		this.crucibleReview = review;
		if (parentComposite == null) {
			createControl(parent, toolkit);
		}

		for (Control c : parentComposite.getChildren()) {
			c.dispose();
		}
		parentComposite.setMenu(null);

		Text nameText = createReadOnlyText(toolkit, parentComposite, crucibleReview.getName(), null, false);
		GridDataFactory.fillDefaults().span(4, 1).grab(true, false).applyTo(nameText);

		Text stateText = createReadOnlyText(toolkit, parentComposite, crucibleReview.getState().getDisplayName(),
				"State:", false);
		GridDataFactory.fillDefaults().applyTo(stateText);

		Text openSinceText = createReadOnlyText(toolkit, parentComposite, DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.SHORT).format(crucibleReview.getCreateDate()), "Open Since:", false);
		GridDataFactory.fillDefaults().applyTo(openSinceText);

		Text authorText = createReadOnlyText(toolkit, parentComposite, crucibleReview.getAuthor().getDisplayName(),
				"Author:", false);
		GridDataFactory.fillDefaults().applyTo(authorText);

		Text moderatorText = createReadOnlyText(toolkit, parentComposite, crucibleReview.getModerator()
				.getDisplayName(), "Moderator:", false);
		GridDataFactory.fillDefaults().applyTo(moderatorText);

		try {
			new CrucibleReviewersPart(crucibleReview.getReviewers()).createControl(toolkit, parentComposite);
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		GridDataFactory.fillDefaults().span(2, 1).applyTo(
				createLabelControl(toolkit, parentComposite, "Statement of Objectives:"));

		Text descriptionText = createReadOnlyText(toolkit, parentComposite, crucibleReview.getDescription(), null, true);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).span(4, 1).applyTo(descriptionText);
		//CHECKSTYLE:MAGIC:ON

		toolkit.paintBordersFor(parentComposite);
	}

}
