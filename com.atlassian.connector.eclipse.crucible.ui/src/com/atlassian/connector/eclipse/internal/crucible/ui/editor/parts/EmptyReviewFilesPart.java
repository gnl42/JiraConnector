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
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Collection;

/**
 * The form part that displays the general comments for the review
 * 
 * @author Shawn Minto
 * @author Pawel Niewiadomski
 */
public class EmptyReviewFilesPart extends AbstractCrucibleEditorFormPart {

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section commentsSection;

	private Composite parentComposite;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(final Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE;
		commentsSection = toolkit.createSection(parent, style);
		commentsSection.setText(getSectionTitle());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentsSection);

		setSection(toolkit, commentsSection);

		if (commentsSection.isExpanded()) {
			Composite composite = createCommentViewers(parent, toolkit);
			commentsSection.setClient(composite);
		} else {
			commentsSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (commentsSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Composite composite = createCommentViewers(parent, toolkit);
							commentsSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow(false);
					}
				}
			});
		}

		return commentsSection;
	}

	private String getSectionTitle() {
		return NLS.bind("Review files ({0} files, {1} comments)", new Object[] { crucibleReview.getFiles().size(),
				crucibleReview.getNumberOfVersionedComments() });
	}

	private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
		parentComposite = toolkit.createComposite(commentsSection);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		Label t = toolkit.createLabel(parentComposite,
				"You need to activate this task to see review files and comments. "
						+ "You will be automatically switched to Crucible Review Perspective. "
						+ "Don't worry though when you deactivate the task you'll be right back in this perspective.",
				SWT.WRAP);

		GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(t);

		return parentComposite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
	}

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		this.crucibleReview = review;

		commentsSection.setText(getSectionTitle());
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return MiscUtil.buildArrayList();
	}
}
