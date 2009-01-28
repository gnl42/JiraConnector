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
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A UI part to represent a general comment in a review
 * 
 * @author Shawn Minto
 */
public class VersionedCommentPart extends CommentPart {

	private final VersionedComment versionedComment;

	private final CrucibleFileInfo crucibleFileInfo;

	public VersionedCommentPart(VersionedComment comment, Review review, CrucibleFileInfo crucibleFileInfo,
			CrucibleReviewEditorPage editor) {
		super(comment, review, editor, new CrucibleFile(crucibleFileInfo, false));
		this.versionedComment = comment;
		this.crucibleFileInfo = crucibleFileInfo;
	}

	@Override
	protected String getSectionHeaderText() {
		String headerText = versionedComment.getAuthor().getDisplayName() + " ";
		headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
				versionedComment.getCreateDate());
		return headerText;
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		Composite composite = super.createSectionContents(section, toolkit);

		if (versionedComment.getReplies().size() > 0) {
			List<VersionedComment> generalComments = new ArrayList<VersionedComment>(versionedComment.getReplies());
			Collections.sort(generalComments, new Comparator<VersionedComment>() {

				public int compare(VersionedComment o1, VersionedComment o2) {
					if (o1 != null && o2 != null) {
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					return 0;
				}

			});

			for (VersionedComment comment : generalComments) {
				CommentPart generalCommentsComposite = new VersionedCommentPart(comment, crucibleReview,
						crucibleFileInfo, crucibleEditor);
				addChildPart(generalCommentsComposite);
				Control commentControl = generalCommentsComposite.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
			}

		}
		return composite;
	}

	@Override
	protected String getAnnotationText() {

		String text = super.getAnnotationText();
		if ((comment.isDefectRaised() || comment.isDefectApproved()) && !comment.isReply()) {
			text += "DEFECT ";
		}

		if (getCrucibleEditor() == null && !comment.isReply()) {
			text += getLineNumberText();
		}
		return text;
	}

	private String getLineNumberText() {
		String text = "";
		if (versionedComment.getToEndLine() != 0) {
			text += "[Lines: " + versionedComment.getToStartLine() + " - " + versionedComment.getToEndLine() + "]";
		} else if (versionedComment.getToStartLine() != 0) {
			text += "[Line: " + versionedComment.getToStartLine() + "]";
		} else {
			text += "[General File]";
		}
		return text;
	}

	@Override
	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {
		if (getCrucibleEditor() != null && !comment.isReply()) {
			OpenVersionedVirtualFileAction openVersionedVirtualFileAction = new OpenVersionedVirtualFileAction(
					getCrucibleEditor().getTask(), new CrucibleFile(crucibleFileInfo, false), versionedComment,
					crucibleReview);
			openVersionedVirtualFileAction.setText(getLineNumberText());
			openVersionedVirtualFileAction.setToolTipText("Open the file to the comment");
			createActionHyperlink(toolbarComposite, toolkit, openVersionedVirtualFileAction);
		}
	}

	@Override
	protected List<IAction> getToolbarActions(boolean isExpanded) {
		List<IAction> actions = new ArrayList<IAction>();
		actions.addAll(super.getToolbarActions(isExpanded));

		return actions;
	}

}
