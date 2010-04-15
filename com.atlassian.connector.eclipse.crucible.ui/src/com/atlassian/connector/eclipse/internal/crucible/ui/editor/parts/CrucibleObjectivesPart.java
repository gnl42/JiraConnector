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

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Collection;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleObjectivesPart extends AbstractCrucibleEditorFormPart {

	private Review crucibleReview;

	private CrucibleReviewEditorPage crucibleEditor;

	private RichTextEditor editor;

	private Section objectivesSection;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
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
	public Control createControl(final Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE;
		objectivesSection = toolkit.createSection(parent, style);
		objectivesSection.setText("Statement of Objectives");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(objectivesSection);

		//setSection(toolkit, objectivesSection);

		if (objectivesSection.isExpanded()) {
			Control composite = createObjectivesControl(objectivesSection, toolkit);
			objectivesSection.setClient(composite);
		} else {
			objectivesSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (objectivesSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Control composite = createObjectivesControl(objectivesSection, toolkit);
							objectivesSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow(false);
					}
				}
			});
		}

		return objectivesSection;
	}

	@SuppressWarnings("restriction")
	protected Control createObjectivesControl(Composite parent, FormToolkit toolkit) {
		TaskRepository repository = crucibleEditor.getEditor().getTaskEditorInput().getTaskRepository();

		Composite sectionComposite = toolkit.createComposite(parent);
		sectionComposite.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(1).spacing(0, 0).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);

		TaskEditorExtensions.setTaskEditorExtensionId(repository, AtlassianUiUtil.CONFLUENCE_WIKI_TASK_EDITOR_EXTENSION);
		AbstractTaskEditorExtension extension = TaskEditorExtensions.getTaskEditorExtension(repository);
		editor = new RichTextEditor(repository, SWT.MULTI, null, extension);
		editor.setReadOnly(true);
		editor.createControl(sectionComposite, toolkit);
		GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(editor.getControl());

		updateControl(this.crucibleReview, sectionComposite, toolkit);

		return sectionComposite;
	}

	@Override
	public void updateControl(Review review, Composite parent, final FormToolkit toolkit) {
		this.crucibleReview = review;
		editor.setText(crucibleReview.getDescription());
	}

	@Override
	public void setFocus() {
		editor.getControl().setFocus();
	}
}
