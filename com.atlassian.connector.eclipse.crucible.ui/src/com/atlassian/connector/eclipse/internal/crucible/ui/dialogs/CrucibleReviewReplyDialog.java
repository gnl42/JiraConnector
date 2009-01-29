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

	private AddCommentPart part;

	private final String shellTitle;

	public CrucibleReviewReplyDialog(Shell parentShell, String shellTitle, Review review, CrucibleFile file,
			Comment replyToComment, LineRange lineRange) {
		super(parentShell);
		this.shellTitle = shellTitle;
		this.review = review;
		this.replyToComment = replyToComment;
	}

	@Override
	protected Control createContents(Composite parent) {
		//CHECKSTYLE:MAGIC:OFF
		getShell().setText(shellTitle);
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
