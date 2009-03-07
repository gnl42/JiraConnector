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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooBuildEditorPage;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.text.DateFormat;

/**
 * Summary row of a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooSummaryPart extends AbstractBambooEditorFormPart {

	public BambooSummaryPart(BambooBuild build, TaskRepository repository) {
		super(build, repository);
	}

	@Override
	public BambooBuildEditorPage getBuildEditor() {
		// ignore
		return null;
	}

	@Override
	public void initialize(BambooBuildEditorPage editor, BambooBuild bambooBuild) {
		// ignore

	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(8, false);
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		createReadOnlyText(toolkit, composite, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
				.format(bambooBuild.getBuildCompletedDate()), "     Completed:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getBuildDurationDescription(), "     Build took:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getBuildReason(), "     Build Reason:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getProjectName(), "     Project:", false);

		toolkit.paintBordersFor(composite);

		control = composite;

		return control;
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline) {

		if (labelString != null) {
			createLabelControl(toolkit, composite, labelString);
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP;
		}
		Text text = new Text(composite, style);
		text.setFont(EditorUtil.TEXT_FONT);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		return text;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite composite, String labelString) {
		Label labelControl = toolkit.createLabel(composite, labelString);
		labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return labelControl;
	}

/*
	Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		Text nameText = createReadOnlyText(toolkit, composite, crucibleReview.getName(), null, false);
		GridDataFactory.fillDefaults().span(4, 1).grab(true, false).applyTo(nameText);

		Text stateText = createReadOnlyText(toolkit, composite, crucibleReview.getState().getDisplayName(), "State:",
				false);
		GridDataFactory.fillDefaults().applyTo(stateText);

		Text openSinceText = createReadOnlyText(toolkit, composite, DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(crucibleReview.getCreateDate()), "Open Since:", false);
		GridDataFactory.fillDefaults().applyTo(openSinceText);

		Text authorText = createReadOnlyText(toolkit, composite, crucibleReview.getAuthor().getDisplayName(),
				"Author:", false);
		GridDataFactory.fillDefaults().applyTo(authorText);

		Text moderatorText = createReadOnlyText(toolkit, composite, crucibleReview.getModerator().getDisplayName(),
				"Moderator:", false);
		GridDataFactory.fillDefaults().applyTo(moderatorText);

		try {
			new CrucibleReviewersPart(crucibleReview.getReviewers()).createControl(toolkit, composite);
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		GridDataFactory.fillDefaults().span(2, 1).applyTo(
				createLabelControl(toolkit, composite, "Statement of Objectives:"));

		Text descriptionText = createReadOnlyText(toolkit, composite, crucibleReview.getDescription(), null, true);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).span(4, 1).applyTo(descriptionText);
		//CHECKSTYLE:MAGIC:ON

		toolkit.paintBordersFor(composite);

		return composite;
 */

}
