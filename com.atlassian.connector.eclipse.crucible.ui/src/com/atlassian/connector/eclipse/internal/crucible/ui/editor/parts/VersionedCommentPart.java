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

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.CompareVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.OpenVersionedVirtualFileAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A UI part to represent a general comment in a review
 * 
 * @author Shawn Minto
 */
public class VersionedCommentPart extends CommentPart<VersionedComment, VersionedCommentPart> {

	private VersionedComment versionedComment;

	private final CrucibleFileInfo crucibleFileInfo;

	private final List<IReviewAction> customActions;

	private Composite composite;

	private CompareVersionedVirtualFileAction compareAction;

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
		composite = super.createSectionContents(section, toolkit);

		updateChildren(composite, toolkit, false, versionedComment.getReplies2());
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

	private String getLineInfo(IntRanges intRanges) {
		if (intRanges.getTotalMin() == intRanges.getTotalMax()) {
			return "[Line: " + intRanges.getTotalMin() + "]";
		} else {
			return "[Lines: " + intRanges.toNiceString() + "]";
		}
	}

	private String getLineNumberText() {
		if (versionedComment.isToLineInfo()) {
			return getLineInfo(versionedComment.getToLineRanges());
		} else if (versionedComment.isFromLineInfo()) {
			return getLineInfo(versionedComment.getFromLineRanges());
		} else {
			return "[General File]";
		}
	}

	@Override
	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {
		if (getCrucibleEditor() != null && !comment.isReply()) {

			//if both revisions are availabe (--> commitType neither added nor deleted), use compareAction
			if (crucibleFileInfo.getCommitType() != CommitType.Deleted
					&& crucibleFileInfo.getCommitType() != CommitType.Added && canOpenCompare()) {
				compareAction = new CompareVersionedVirtualFileAction(crucibleFileInfo, versionedComment,
						crucibleReview);
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

	private boolean canOpenCompare() {
		if (crucibleFileInfo != null) {
			VersionedVirtualFile oldFileDescriptor = crucibleFileInfo.getOldFileDescriptor();
			VersionedVirtualFile newFileDescriptor = crucibleFileInfo.getFileDescriptor();
			if (oldFileDescriptor == null || oldFileDescriptor.getRevision() == null
					|| oldFileDescriptor.getRevision().length() == 0 || newFileDescriptor == null
					|| newFileDescriptor.getRevision() == null || newFileDescriptor.getRevision().length() == 0) {
				return false;
			}
			return true;
		}
		return false;
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

	@Override
	protected Control update(Composite parentComposite, FormToolkit toolkit, VersionedComment newComment,
			Review newReview) {
		this.crucibleReview = newReview;
		if (compareAction != null) {
			this.compareAction.updateReview(newReview, crucibleFileInfo, newComment);
		}
		// TODO update the text 
		if (newComment instanceof VersionedComment
				&& !CrucibleUtil.areVersionedCommentsDeepEquals(newComment, versionedComment)) {
			if (newComment instanceof VersionedComment) {
				this.versionedComment = newComment;
			}
			this.comment = newComment;

			Control createControl = createOrUpdateControl(parentComposite, toolkit);

			return createControl;

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
			updateChildren(composite, toolkit, true, versionedComment.getReplies2());

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
	protected VersionedCommentPart createChildPart(VersionedComment comment, Review crucibleReview2,
			CrucibleReviewEditorPage crucibleEditor2) {
		return new VersionedCommentPart(comment, crucibleReview2, crucibleFileInfo, crucibleEditor2);
	}

	@Override
	protected Comparator<VersionedComment> getComparator() {
		return new Comparator<VersionedComment>() {

			public int compare(VersionedComment o1, VersionedComment o2) {
				if (o1 != null && o2 != null) {
					return o1.getCreateDate().compareTo(o2.getCreateDate());
				}
				return 0;
			}

		};
	}

	@Override
	protected boolean represents(VersionedComment comment) {
		return versionedComment.getPermId().equals(comment.getPermId());
	}

	@Override
	protected boolean shouldHighlight(VersionedComment comment, CrucibleReviewEditorPage crucibleEditor2) {
		return !comment.getAuthor().getUsername().equals(crucibleEditor.getUsername());
	}
}
