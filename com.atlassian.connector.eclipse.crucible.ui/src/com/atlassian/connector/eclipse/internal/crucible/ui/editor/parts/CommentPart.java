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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.forms.SizeCachingComposite;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A UI part to represent a comment in a review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public abstract class CommentPart<T, V extends ExpandablePart<T, V>> extends ExpandablePart<T, V> {

	protected Comment comment;

	protected final CrucibleFile crucibleFile;

	protected Text commentTextComposite;

	protected SizeCachingComposite sectionClient;

	public CommentPart(Comment comment, Review crucibleReview, CrucibleFile crucibleFile) {
		super(crucibleReview);
		this.comment = comment;
		this.crucibleFile = crucibleFile;
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		section.clientVerticalSpacing = 0;

		sectionClient = new SizeCachingComposite(section, SWT.NONE);
		toolkit.adapt(sectionClient);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 0;
		layout.marginLeft = 9;
		sectionClient.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionClient);

		createCommentArea(toolkit, sectionClient);

		//CHECKSTYLE:MAGIC:ON
		return sectionClient;
	}

	protected void createCommentArea(FormToolkit toolkit, Composite parentComposite) {
		commentTextComposite = createReadOnlyText(toolkit, parentComposite, getCommentText());
		GridDataFactory.fillDefaults().hint(500, SWT.DEFAULT).applyTo(commentTextComposite);
	}

	// TODO could be moved to a util method
	private String getCommentText() {
		String commentText = comment.getMessage();

		String customFieldsString = "";
		if (comment.getCustomFields() != null && comment.getCustomFields().size() > 0) {

			Map<String, CustomField> customFields = comment.getCustomFields();
			CustomField classificationField = customFields.get(CrucibleConstants.CLASSIFICATION_CUSTOM_FIELD_KEY);
			CustomField rankField = customFields.get(CrucibleConstants.RANK_CUSTOM_FIELD_KEY);

			String classification = null;
			if (classificationField != null) {
				classification = classificationField.getValue();
			}

			String rank = null;
			if (rankField != null) {
				rank = rankField.getValue();
			}

			if (rank != null || classification != null) {
				customFieldsString = "(";

				if (comment.isDefectApproved() || comment.isDefectRaised()) {
					customFieldsString += "Defect, ";
				}
			}

			if (classification != null) {
				customFieldsString += "Classification:" + classification;
				if (rank != null) {
					customFieldsString += ", ";
				}
			}

			if (rank != null) {
				customFieldsString += "Rank:" + rank;
			}

			if (customFieldsString.length() > 0) {
				customFieldsString += ")";
			}

		}
		if (customFieldsString.length() > 0) {
			commentText += "  " + customFieldsString;
		}
		return commentText;
	}

	@Override
	protected String getAnnotationText() {
		String text = "";
		if (comment.isDraft()) {
			text = "DRAFT ";
		}
		return text;
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value) {

		int style = SWT.FLAT | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP;

		final Text text = new Text(composite, style);
		text.setFont(JFaceResources.getDefaultFont());
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);
		toolkit.adapt(text, true, true);

		// HACK: this is to make sure that we can't have multiple things highlighted
		text.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// ignore

			}

			public void focusLost(FocusEvent e) {
				text.setSelection(0);
			}

		});

		return text;
	}

	@Override
	protected boolean canExpand() {
		return !comment.isReply();
	}

	@Override
	protected boolean hasContents() {
		return true;
	}

	@Override
	protected ImageDescriptor getAnnotationImage() {
		if (comment.isDefectRaised() || comment.isDefectApproved()) {

			// TODO get an image for a bug
			return null;
		}
		return null;
	}

	@Override
	protected List<IReviewAction> getToolbarActions(boolean isExpanded) {
		List<IReviewAction> actions = new ArrayList<IReviewAction>();
		if (isExpanded) {
			if (!comment.isReply() && CrucibleUtil.canAddCommentToReview(crucibleReview)) {
				ReplyToCommentAction action = new ReplyToCommentAction();
				action.selectionChanged(new StructuredSelection(comment));
				actions.add(action);
			}

			if (CrucibleUiUtil.canModifyComment(crucibleReview, comment)) {
				EditCommentAction action = new EditCommentAction();
				action.selectionChanged(new StructuredSelection(comment));
				actions.add(action);

				if (!comment.isReply() && comment.getReplies().size() > 0) {
					actions.add(new CannotRemoveCommentAction("Remove Comment", CrucibleImages.COMMENT_DELETE));
				} else {
					RemoveCommentAction action1 = new RemoveCommentAction();
					action1.selectionChanged(new StructuredSelection(comment));
					actions.add(action1);
				}

				if (comment.isDraft()) {
					PostDraftCommentAction action1 = new PostDraftCommentAction();
					action1.selectionChanged(new StructuredSelection(comment));
					actions.add(action1);
				}
			}
		}
		return actions;
	}

	private final class CannotRemoveCommentAction extends Action implements IReviewAction {
		public CannotRemoveCommentAction(String text, ImageDescriptor icon) {
			super(text);
			setImageDescriptor(icon);
		}

		public void setActionListener(IReviewActionListener listner) {
		}

		@Override
		public void run() {
			MessageDialog.openInformation(getSection().getShell(), "Delete",
					"Cannot delete comment with replies. You must delete replies first.");
		}

	}
}