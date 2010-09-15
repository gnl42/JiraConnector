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

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleEditorConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.internal.wikitext.tasks.ui.editor.ConfluenceMarkupTaskEditorExtension;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
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

	private Composite sectionComposite;

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
	protected Control createObjectivesControl(final Composite parent, FormToolkit toolkit) {
		// we need to have the additional wrapping composite, as otherwise we could not dispose WikiText editor multiple
		// times (Control returned here is a client of the section (Section.setClient() which leads to SWTError after first
		// disposal
		sectionComposite = toolkit.createComposite(parent);
		sectionComposite.setLayout(GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(1).spacing(0, 0).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);

		updateControl(this.crucibleReview, null, toolkit);
		return sectionComposite;
	}

	@SuppressWarnings("restriction")
	@Override
	public void updateControl(Review review, Composite ignoreIt, final FormToolkit toolkit) {
		this.crucibleReview = review;

		TaskRepository repository = crucibleEditor.getEditor().getTaskEditorInput().getTaskRepository();

		// we recreate WikiText control each time to workaround its bugs
		if (editor != null) {
			editor.getControl().dispose();
		}

		final AbstractTaskEditorExtension extension = new ConfluenceMarkupTaskEditorExtension();
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		int style = SWT.FLAT | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP;

		editor = new RichTextEditor(repository, style, contextService, extension);
		// text must be set before createControl, otherwise it will not be rendered correctly
		editor.setText(crucibleReview.getDescription());
		editor.setReadOnly(true);
		editor.createControl(sectionComposite, toolkit);
		GridDataFactory.fillDefaults().grab(true, false).hint(CrucibleEditorConstants.MIN_WIDTH, SWT.DEFAULT)
				.applyTo(editor.getControl());

	}

	@Override
	public void setFocus() {
		if (editor != null) {
			editor.getControl().setFocus();
		}
	}
}
