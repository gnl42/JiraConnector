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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions.AddGeneralReviewCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.CommentPart;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.ExpandablePart;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.GeneralCommentPart;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The form part that displays the general comments for the review
 * 
 * @author Shawn Minto
 */
public class CrucibleGeneralCommentsPart extends AbstractCrucibleEditorFormPart {

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section commentsSection;

	private List<CommentPart> parts;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
		parts = new ArrayList<CommentPart>();
	}

	@Override
	public Collection<? extends ExpandablePart> getExpandableParts() {
		return parts;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(Composite parent, final FormToolkit toolkit) {

		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE;
		commentsSection = toolkit.createSection(parent, style);
		commentsSection.setText(getSectionTitle());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentsSection);

		setSection(toolkit, commentsSection);

		if (commentsSection.isExpanded()) {
			Composite composite = createCommentViewers(toolkit);
			commentsSection.setClient(composite);
		} else {
			commentsSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (commentsSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Composite composite = createCommentViewers(toolkit);
							commentsSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow();
					}
				}
			});
		}

		return commentsSection;
	}

	private String getSectionTitle() {
		String title = "General Comments";
		try {
			return title + " (" + crucibleReview.getNumberOfGeneralComments() + " comments)";
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;

	}

	private Composite createCommentViewers(FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		Composite composite = toolkit.createComposite(commentsSection);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		try {
			List<GeneralComment> generalComments = new ArrayList<GeneralComment>(crucibleReview.getGeneralComments());
			Collections.sort(generalComments, new Comparator<GeneralComment>() {

				public int compare(GeneralComment o1, GeneralComment o2) {
					if (o1 != null && o2 != null) {
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					return 0;
				}

			});

			for (GeneralComment comment : generalComments) {
				CommentPart generalCommentsComposite = new GeneralCommentPart(comment, crucibleReview, crucibleEditor);
				parts.add(generalCommentsComposite);
				Control commentControl = generalCommentsComposite.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
			}

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}

		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
		barManager.add(new AddGeneralReviewCommentAction(crucibleReview));
		super.fillToolBar(barManager);
	}
}
