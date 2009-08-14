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
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.Comparator;

/**
 * A UI part to represent a general comment in a review
 * 
 * @author Shawn Minto
 */
public class GeneralCommentPart extends CommentPart<GeneralComment, GeneralCommentPart> {

	private GeneralComment generalComment;

	private Composite composite;

	public GeneralCommentPart(GeneralComment comment, Review review, CrucibleReviewEditorPage editor) {
		super(comment, review, editor, null);
		this.generalComment = comment;
	}

	@Override
	protected String getSectionHeaderText() {
		return generalComment.getAuthor().getDisplayName()
				+ "   "
				+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
						generalComment.getCreateDate());
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		composite = super.createSectionContents(section, toolkit);

		updateChildren(composite, toolkit, false, generalComment.getReplies2());
		return composite;
	}

	public Comparator<GeneralComment> getComparator() {
		return new Comparator<GeneralComment>() {

			public int compare(GeneralComment o1, GeneralComment o2) {
				if (o1 != null && o2 != null) {
					return o1.getCreateDate().compareTo(o2.getCreateDate());
				}
				return 0;
			}

		};
	}

	public boolean represents(GeneralComment newComment) {
		return newComment.getPermId().equals(generalComment.getPermId());
	}

	@Override
	protected String getAnnotationText() {
		// TODO make the text be based on the numbers properly (e.g. s's)
		String text = super.getAnnotationText();
		if ((generalComment.isDefectRaised() || generalComment.isDefectApproved()) && !generalComment.isReply()) {
			text += "DEFECT ";
		}
		if (generalComment.getReplies().size() > 0) {
			text += "[" + generalComment.getReplies().size() + " replies]";
		}
		return text;
	}

	@Override
	protected ImageDescriptor getAnnotationImage() {
		if (generalComment.isDefectRaised() || generalComment.isDefectApproved()) {

			// TODO get an image for a bug
			return null;
		}
		return null;
	}

	@Override
	public Control update(Composite parentComposite, FormToolkit toolkit, GeneralComment newComment, Review newReview) {

		this.crucibleReview = newReview;
		// TODO update the text 
		if (!CrucibleUtil.areGeneralCommentsDeepEquals(newComment, generalComment)) {
			if (newComment instanceof GeneralComment) {
				this.generalComment = newComment;
			}
			this.comment = newComment;

			return createOrUpdateControl(parentComposite, toolkit);

		}
		return getSection();
	}

	// TODO handle changed highlighting properly

	protected final Control createOrUpdateControl(Composite parentComposite, FormToolkit toolkit) {
		Control createdControl = null;
		if (getSection() == null) {

			Control newControl = createControl(parentComposite, toolkit);

			setIncomming(true);
			decorate();

			createdControl = newControl;
		} else {

			if (commentTextComposite != null && !commentTextComposite.isDisposed()) {
				Composite parent = commentTextComposite.getParent();
				commentTextComposite.dispose();
				createCommentArea(toolkit, composite);
				if (parent.getChildren().length > 0) {
					commentTextComposite.moveAbove(parent.getChildren()[0]);
				}

			}
			updateChildren(composite, toolkit, true, generalComment.getReplies2());

			createdControl = getSection();
		}

		if (sectionClient != null && !sectionClient.isDisposed()) {
			sectionClient.clearCache();
		}
		getSection().layout(true, true);

		update();

		return createdControl;

	}

	@Override
	protected GeneralCommentPart createChildPart(GeneralComment comment, Review crucibleReview2,
			CrucibleReviewEditorPage crucibleEditor2) {
		// ignore
		return new GeneralCommentPart(comment, crucibleReview2, crucibleEditor2);
	}

	@Override
	protected boolean shouldHighlight(GeneralComment comment, CrucibleReviewEditorPage crucibleEditor2) {
		return !comment.getAuthor().getUsername().equals(crucibleEditor.getUsername());
	}
}
