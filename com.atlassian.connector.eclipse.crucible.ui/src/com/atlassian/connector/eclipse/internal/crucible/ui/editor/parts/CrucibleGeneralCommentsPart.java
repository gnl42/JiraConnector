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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.AddGeneralReviewCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.forms.ReflowRespectingSection;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
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

	private List<GeneralCommentPart> parts;

	private Composite parentComposite;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
		parts = new ArrayList<GeneralCommentPart>();
	}

	@Override
	public Collection<? extends ExpandablePart<?, ?>> getExpandableParts() {
		return parts;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(final Composite parent, final FormToolkit toolkit) {

		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE;
		commentsSection = new ReflowRespectingSection(toolkit, parent, style, crucibleEditor);
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
		String title = "General Comments";
		try {
			return NLS.bind("{0}   ({1} comments)", title, crucibleReview.getNumberOfGeneralComments());
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;

	}

	private Composite createCommentViewers(Composite parent, FormToolkit toolkit) {
		parentComposite = toolkit.createComposite(commentsSection);
		parentComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		updateControl(crucibleReview, parent, toolkit, false);

		return parentComposite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
		if (CrucibleUtil.canAddCommentToReview(crucibleReview)) {
			AddGeneralReviewCommentAction action = new AddGeneralReviewCommentAction(crucibleReview);
			barManager.add(action);
		}
		super.fillToolBar(barManager);
	}

	@Override
	public void updateControl(Review review, Composite parent, FormToolkit toolkit) {
		updateControl(review, parent, toolkit, true);
	}

	public void updateControl(Review review, Composite parent, FormToolkit toolkit, boolean shouldHighlight) {
		this.crucibleReview = review;

		commentsSection.setText(getSectionTitle());

		if (parentComposite == null) {
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
			return;
		}

		parentComposite.setMenu(null);

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

			// The following code is almost duplicated in the crucible review files part
			List<GeneralCommentPart> newParts = new ArrayList<GeneralCommentPart>();

			Control prevControl = null;

			for (int i = 0; i < generalComments.size(); i++) {
				GeneralComment comment = generalComments.get(i);

				GeneralCommentPart oldPart = findPart(comment);

				if (oldPart != null) {
					Control commentControl = oldPart.update(parentComposite, toolkit, comment, crucibleReview);
					if (commentControl != null && !commentControl.isDisposed()) {

						GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);

						if (prevControl != null) {
							commentControl.moveBelow(prevControl);
						} else if (parentComposite.getChildren().length > 0) {
							commentControl.moveAbove(parentComposite.getChildren()[0]);
						}
						prevControl = commentControl;
					}

					newParts.add(oldPart);
				} else {
					GeneralCommentPart commentPart = new GeneralCommentPart(comment, crucibleReview, crucibleEditor);
					newParts.add(commentPart);
					Control commentControl = commentPart.createControl(parentComposite, toolkit);

					if (shouldHighlight && !comment.getAuthor().getUserName().equals(crucibleEditor.getUserName())) {
						commentPart.setIncomming(true);
						commentPart.decorate();
					}

					GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
					if (prevControl != null) {
						commentControl.moveBelow(prevControl);
					} else if (parentComposite.getChildren().length > 0) {
						commentControl.moveAbove(parentComposite.getChildren()[0]);
					}
					prevControl = commentControl;
				}
			}

			List<GeneralCommentPart> toRemove = new ArrayList<GeneralCommentPart>();

			for (GeneralCommentPart part : parts) {
				if (!newParts.contains(part)) {
					toRemove.add(part);
				}
			}

			for (GeneralCommentPart part : toRemove) {
				part.dispose();
			}

			parts.clear();
			parts.addAll(newParts);

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	private GeneralCommentPart findPart(GeneralComment comment) {

		for (GeneralCommentPart part : parts) {
			if (part.represents(comment)) {
				return part;
			}
		}

		return null;
	}

}
