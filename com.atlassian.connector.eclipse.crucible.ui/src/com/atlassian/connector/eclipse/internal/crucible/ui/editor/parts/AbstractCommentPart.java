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
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages.AvatarSize;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.EditCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.PostDraftCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.RemoveCommentAction;
import com.atlassian.connector.eclipse.internal.crucible.ui.actions.ReplyToCommentAction;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.connector.eclipse.ui.forms.SizeCachingComposite;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A UI part to represent a comment in a review
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
@SuppressWarnings("restriction")
public abstract class AbstractCommentPart<V extends ExpandablePart<Comment, V>> extends ExpandablePart<Comment, V> {

	protected Comment comment;

	// protected final CrucibleFile crucibleFile;

	protected Control commentTextComposite;

	protected SizeCachingComposite sectionClient;

	public AbstractCommentPart(Comment comment, Review crucibleReview) {
		super(crucibleReview);
		this.comment = comment;
		// this.crucibleFile = crucibleFile;
	}

	@Override
	protected String getSectionHeaderText() {
		String headerText = comment.getAuthor().getDisplayName() + "   ";
		headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
				comment.getCreateDate());
		return headerText;
	}

	@Override
	protected Comparator<Comment> getComparator() {
		return new Comparator<Comment>() {

			public int compare(Comment o1, Comment o2) {
				if (o1 != null && o2 != null) {
					return o1.getCreateDate().compareTo(o2.getCreateDate());
				}
				return 0;
			}

		};
	}

	// TODO handle changed highlighting properly

	protected final Control createOrUpdateControl(Composite parentComposite, FormToolkit toolkit) {
		Control createdControl = null;
		if (getSection() == null) {

			Control newControl = createControl(parentComposite, toolkit);

			setIncomming(true);

			createdControl = newControl;
		} else {

			if (commentTextComposite != null && !commentTextComposite.isDisposed()) {
				Composite parent = commentTextComposite.getParent();
				commentTextComposite.dispose();
				createCommentArea(toolkit, sectionClient);
				if (parent.getChildren().length > 0) {
					commentTextComposite.moveAbove(parent.getChildren()[0]);
				}

			}
			updateChildren(sectionClient, toolkit, true, comment.getReplies());

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
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		// CHECKSTYLE:MAGIC:OFF
		section.clientVerticalSpacing = 0;

		sectionClient = new SizeCachingComposite(section, SWT.NONE);
		toolkit.adapt(sectionClient);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 0;
		layout.marginLeft = 9;
		sectionClient.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionClient);

		createCommentArea(toolkit, sectionClient);

		updateChildren(sectionClient, toolkit, false, comment.getReplies());

		// CHECKSTYLE:MAGIC:ON
		return sectionClient;
	}

	protected void createCommentArea(FormToolkit toolkit, Composite parentComposite) {
		final Composite twoColumnComposite = new Composite(parentComposite, SWT.NONE);
		twoColumnComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().hint(500, SWT.DEFAULT).applyTo(twoColumnComposite);
		toolkit.adapt(twoColumnComposite);

		final Label avatarLabel = new Label(twoColumnComposite, SWT.NONE);
		toolkit.adapt(avatarLabel, false, false);
		avatarLabel.setImage(CrucibleUiPlugin.getDefault().getAvatarsCache().getAvatarOrDefaultImage(
				comment.getAuthor(), AvatarSize.LARGE));

		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(avatarLabel);

		commentTextComposite = createReadOnlyText(toolkit, twoColumnComposite, getCommentText());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(commentTextComposite);
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

	private Control createReadOnlyText(FormToolkit toolkit, Composite composite, String value) {

		int style = SWT.FLAT | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP;

		ITask task = CrucibleUiUtil.getCrucibleTask(crucibleReview);

		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		TaskEditorExtensions.setTaskEditorExtensionId(repository, AtlassianUiUtil.CONFLUENCE_WIKI_TASK_EDITOR_EXTENSION);
		AbstractTaskEditorExtension extension = TaskEditorExtensions.getTaskEditorExtension(repository);

		final RichTextEditor editor = new RichTextEditor(repository, style, null, extension);
		editor.setReadOnly(true);
		editor.setText(value);
		editor.createControl(composite, toolkit);

		// HACK: this is to make sure that we can't have multiple things highlighted
		editor.getViewer().getTextWidget().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				editor.getViewer().getTextWidget().setSelection(0);
			}
		});

		return editor.getControl();
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

				if (CrucibleUtil.canPublishDraft(comment)) {
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