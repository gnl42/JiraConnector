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

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
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

	private final List<IReviewAction> customActions;

	public VersionedCommentPart(VersionedComment comment, Review review, CrucibleFileInfo crucibleFileInfo,
			CrucibleReviewEditorPage editor) {
		super(comment, review, editor, new CrucibleFile(crucibleFileInfo, false));
		this.versionedComment = comment;
		this.crucibleFileInfo = crucibleFileInfo;
		customActions = new ArrayList<IReviewAction>();
	}

	@Override
	protected String getSectionHeaderText() {
		String headerText = versionedComment.getAuthor().getDisplayName() + "   ";
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
		StringBuilder builder = new StringBuilder();
		if (versionedComment.getToEndLine() != 0) {
			builder.append("[Lines: ");
			builder.append(versionedComment.getToEndLine());
			builder.append(" - ");
			builder.append(versionedComment.getToStartLine());
			builder.append("]");
		} else if (versionedComment.getFromEndLine() != 0) {
			builder.append("[Lines: ");
			builder.append(versionedComment.getFromEndLine());
			builder.append(" - ");
			builder.append(versionedComment.getFromStartLine());
			builder.append("]");
		} else if (versionedComment.getToStartLine() != 0) {
			builder.append("[Lines: ");
			builder.append(versionedComment.getToStartLine());
			builder.append("]");
		} else if (versionedComment.getFromStartLine() != 0) {
			builder.append("[Lines: ");
			builder.append(versionedComment.getFromStartLine());
			builder.append("]");
		} else {
			builder.append("[General File]");
		}
		return builder.toString();
	}

	@Override
	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {
		if (getCrucibleEditor() != null && !comment.isReply()) {

			//if both revisions are availabe (--> commitType neither added nor deleted), use compareAction
			if (crucibleFileInfo.getCommitType() != CommitType.Deleted
					&& crucibleFileInfo.getCommitType() != CommitType.Added) {
				CompareVersionedVirtualFileAction compareAction = new CompareVersionedVirtualFileAction(
						crucibleFileInfo, versionedComment, crucibleReview);
				compareAction.setToolTipText("Open the file to the comment in the compare editor");
				compareAction.setText(getLineNumberText());
				// TODO set the image descriptor
				createActionHyperlink(toolbarComposite, toolkit, compareAction);
			} else {
				// if fromLineComment --> oldFile
				CrucibleFile crucibleFile = new CrucibleFile(crucibleFileInfo, versionedComment.isFromLineInfo());
				OpenVersionedVirtualFileAction openVersionedVirtualFileAction = new OpenVersionedVirtualFileAction(
						getCrucibleEditor().getTask(), crucibleFile, versionedComment, crucibleReview);
				openVersionedVirtualFileAction.setText(getLineNumberText());
				openVersionedVirtualFileAction.setToolTipText("Open the file to the comment");
				createActionHyperlink(toolbarComposite, toolkit, openVersionedVirtualFileAction);
			}
		}

		for (IReviewAction customAction : customActions) {
			ImageHyperlink textHyperlink = toolkit.createImageHyperlink(toolbarComposite, SWT.NONE);
			textHyperlink.setText(" ");
			textHyperlink.setEnabled(false);
			textHyperlink.setUnderlined(false);

			createActionHyperlink(toolbarComposite, toolkit, customAction);
		}
	}

	@Override
	protected List<IReviewAction> getToolbarActions(boolean isExpanded) {
		List<IReviewAction> actions = new ArrayList<IReviewAction>();
		actions.addAll(super.getToolbarActions(isExpanded));

		return actions;
	}

	public void addCustomAction(IReviewAction action) {
		customActions.add(action);
	}

	public boolean isComment(VersionedComment commentToReveal) {
		return commentToReveal.getPermId().equals(comment.getPermId());
	}
}
