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

package com.atlassian.connector.eclipse.internal.crucible.ui.util;

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.internal.wikitext.tasks.ui.editor.ConfluenceMarkupTaskEditorExtension;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Wojciech Seliga
 */
public final class CommentUiUtil {

	private CommentUiUtil() {

	}

	public static String getCommentInfoHeaderText(Comment comment) {
		StringBuilder headerText = new StringBuilder();
		headerText.append(comment.getAuthor().getDisplayName());
		headerText.append("\n");
		headerText.append(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
				.format(comment.getCreateDate()));

		if (comment.getReadState() != null) {
			if (comment.getReadState().equals(Comment.ReadState.READ)) {
				headerText.append(", Read");
			} else if (comment.getReadState().equals(Comment.ReadState.UNREAD)
					|| comment.getReadState().equals(Comment.ReadState.LEAVE_UNREAD)) {
				headerText.append(", Unread");
			}
		}

		if (comment.isDraft()) {
			headerText.append(", ");
			headerText.append("Draft");
		}

		if (comment.isDefectRaised()) {
			headerText.append(", ");
			headerText.append("Defect");

			Map<String, CustomField> fields = comment.getCustomFields();
			if (fields != null) {
				boolean shouldCloseBracket = false;
				if (fields.containsKey(CrucibleConstants.RANK_CUSTOM_FIELD_KEY)) {
					headerText.append(" (");
					shouldCloseBracket = true;
					headerText.append(fields.get(CrucibleConstants.RANK_CUSTOM_FIELD_KEY).getValue());
				}

				if (fields.containsKey(CrucibleConstants.CLASSIFICATION_CUSTOM_FIELD_KEY)) {
					if (shouldCloseBracket) {
						headerText.append(",");
					}
					headerText.append(" ");
					headerText.append(fields.get(CrucibleConstants.CLASSIFICATION_CUSTOM_FIELD_KEY)
							.getValue());
				}
				if (shouldCloseBracket) {
					headerText.append(")");
				}

			}
		}
		return headerText.toString();
	}

	public static boolean isSimpleInfoEnough(Map<String, IntRanges> ranges) {
		if (ranges.size() <= 1) {
			return true;
		}
		final Iterator<Entry<String, IntRanges>> it = ranges.entrySet().iterator();
		final IntRanges lines = it.next().getValue();
		while (it.hasNext()) {
			if (!lines.equals(it.next().getValue())) {
				return false;
			}
		}
		return true;
	}

	public static String getCompactedLineInfoText(Map<String, IntRanges> ranges) {

		if (isSimpleInfoEnough(ranges)) {
			final StringBuilder infoText = new StringBuilder("File comment for ");
			final Iterator<Entry<String, IntRanges>> it = ranges.entrySet().iterator();
			Entry<String, IntRanges> curEntry = it.next();
			IntRanges lines = curEntry.getValue();
			infoText.append(getLineInfo(lines));
			if (it.hasNext()) {
				infoText.append(" in revisions: ");
			} else {
				infoText.append(" in revision: ");
			}

			do {
				infoText.append(curEntry.getKey());
				if (it.hasNext()) {
					infoText.append(", ");
				} else {
					break;
				}
				curEntry = it.next();
			} while (true);
			return infoText.toString();
		} else {
			final StringBuilder infoText = new StringBuilder("File comment for:\n");
			for (Map.Entry<String, IntRanges> range : ranges.entrySet()) {
				infoText.append("- ");
				infoText.append(getLineInfo(range.getValue()));
				infoText.append(" in revision: ");
				infoText.append(range.getKey());
				infoText.append("\n");
			}
			return infoText.toString();
		}
	}

	public static String getLineInfo(IntRanges intRanges) {
		if (intRanges.getTotalMin() == intRanges.getTotalMax()) {
			return "line " + intRanges.getTotalMin();
		} else {
			return "lines " + intRanges.toNiceString();
		}
	}

	@SuppressWarnings("restriction")
	public static RichTextEditor createWikiTextControl(FormToolkit toolkit, Composite parent, Comment comment) {

		int style = SWT.FLAT | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP;

		ITask task = CrucibleUiUtil.getCrucibleTask(comment.getReview());

		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(),
				task.getRepositoryUrl());

		final AbstractTaskEditorExtension extension = new ConfluenceMarkupTaskEditorExtension();
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);

		final RichTextEditor editor = new RichTextEditor(repository, style, contextService, extension);

		editor.setReadOnly(true);
		editor.setText(comment.getMessage());
		editor.createControl(parent, toolkit);

		// HACK: this is to make sure that we can't have multiple things highlighted
		editor.getViewer().getTextWidget().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				editor.getViewer().getTextWidget().setSelection(0);
			}
		});

		return editor;
	}

}
