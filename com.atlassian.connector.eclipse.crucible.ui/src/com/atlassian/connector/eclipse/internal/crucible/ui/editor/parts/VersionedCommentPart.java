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
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;

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

		updateChildren(composite, toolkit, false, versionedComment.getReplies());
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

	@Override
	protected Control update(Composite parentComposite, FormToolkit toolkit, VersionedComment newComment,
			Review newReview) {

		this.crucibleReview = newReview;
		// TODO update the text 
		if (newComment instanceof VersionedCommentBean && !deepEquals(((VersionedCommentBean) newComment), comment)) {
			if (newComment instanceof VersionedComment) {
				this.versionedComment = newComment;
			}
			this.comment = newComment;

			Control createControl = createOrUpdateControl(parentComposite, toolkit);

			return createControl;

		}
		return getSection();
	}

	/**
	 * Temporarily Copied from VersionedCommentBean
	 */
	public boolean deepEquals(VersionedCommentBean c1, Object o) {
		if (c1 == o) {
			return true;
		}
		if (!(o instanceof VersionedCommentBean)) {
			return false;
		}
		if (!c1.equals(o)) {
			return false;
		}

		VersionedCommentBean that = (VersionedCommentBean) o;

		if (c1.getFromEndLine() != that.getFromEndLine()) {
			return false;
		}
		if (c1.isFromLineInfo() != that.isFromLineInfo()) {
			return false;
		}
		if (c1.getFromStartLine() != that.getFromStartLine()) {
			return false;
		}
		if (c1.getToEndLine() != that.getToEndLine()) {
			return false;
		}
		if (c1.isToLineInfo() != that.isToLineInfo()) {
			return false;
		}
		if (c1.getToStartLine() != that.getToStartLine()) {
			return false;
		}
		if (c1.getReplies() != null ? !c1.getReplies().equals(that.getReplies()) : that.getReplies() != null) {
			return false;
		}

		if (c1.getReplies().size() != that.getReplies().size()) {
			return false;
		}

		for (VersionedComment vc : c1.getReplies()) {
			boolean found = false;
			for (VersionedComment tvc : that.getReplies()) {
				if (vc.getPermId() == tvc.getPermId() && ((VersionedCommentBean) vc).deepEquals(vc)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
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
			updateChildren(composite, toolkit, true, versionedComment.getReplies());

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
		return !comment.getAuthor().getUserName().equals(crucibleEditor.getUserName());
	}
}
