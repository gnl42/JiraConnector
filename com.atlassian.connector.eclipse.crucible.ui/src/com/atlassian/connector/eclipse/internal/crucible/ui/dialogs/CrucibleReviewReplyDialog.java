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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.AddCommentPart;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.AddCommentPart.IAddCommentPartListener;
import com.atlassian.connector.eclipse.ui.team.CrucibleFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;

/**
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewReplyDialog extends Dialog implements IAddCommentPartListener {

	private final Review review;

	private final Comment replyToComment;

	/**
	 * Add new general comment
	 * 
	 * @param parentShell
	 * @param review
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review) {
		this(parentShell, "Add general comment " + review.getName(), review, null);
	}

	/**
	 * Add new general comment reply
	 * 
	 * @param parentShell
	 * @param review
	 * @param comment
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, Comment replyToComment) {
		this(parentShell, "Add a reply to: ", review, replyToComment);
	}

	/**
	 * Add new versioned comment on a file
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file) {
		this(parentShell, "Add versioned comment on file: "
				+ file.getCrucibleFileInfo().getFileDescriptor().getAbsoluteUrl(), review, null);
	}

	/**
	 * Add new versioned comment reply on a file
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param comment
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, Comment replyToComment) {
		this(parentShell, "Reply to: ", review, replyToComment);
	}

	/**
	 * Add new versioned comment on a specific line of code
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param replyToComment
	 * @param lineRange
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, LineRange lineRange) {
		this(parentShell, "Add new versioned comment on line(s) " + String.valueOf(lineRange.getStartLine()) + "-"
				+ String.valueOf(lineRange.getStartLine() + lineRange.getNumberOfLines()), review, null);
	}

	/**
	 * Add new versioned comment reply on a specific line of code
	 * 
	 * @param parentShell
	 * @param review
	 * @param file
	 * @param comment
	 * @param lineRange
	 */
	public CrucibleReviewReplyDialog(Shell parentShell, Review review, CrucibleFile file, Comment replyToComment,
			LineRange lineRange) {
		this(parentShell, "Reply to: ", review, replyToComment);
	}

	private CrucibleReviewReplyDialog(Shell parentShell, String dialogMessage, Review review, Comment replyToComment) {
		super(parentShell);
		this.review = review;
		this.replyToComment = replyToComment;
	}

	private AddCommentPart part;

	@Override
	protected Control createContents(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF
		part = new AddCommentPart(review, replyToComment);
		Composite composite = part.createControl(parent);
		part.setListener(this);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).applyTo(composite);
		return composite;
		//CHECKSTYLE:MAGIC:ON
	}

	public void addComment() {
		setReturnCode(Window.OK);
		close();
	}

	public void cancelAddComment() {
		setReturnCode(Window.CANCEL);
		close();
	}

	public String getValue() {
		return part.getValue();
	}

	public boolean isDraft() {
		return part.isDraft();
	}

	public boolean isDefect() {
		return part.isDefect();
	}

	public HashMap<String, CustomField> getCustomFieldSelections() {
		return part.getCustomFieldSelections();
	}
}
