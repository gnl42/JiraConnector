/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
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
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

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

	public VersionedCommentPart(VersionedComment comment, CrucibleReviewEditorPage editor) {
		super(comment, editor);
		this.versionedComment = comment;
	}

	@Override
	protected String getSectionHeaderText() {
		String headerText = versionedComment.getAuthor().getDisplayName() + " ";
		headerText += DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
				versionedComment.getCreateDate());
		if (versionedComment.getToEndLine() != 0) {
			headerText += "\t[Lines: " + versionedComment.getToStartLine() + " - " + versionedComment.getToEndLine()
					+ "]";
		} else if (versionedComment.getToStartLine() != 0) {
			headerText += "\t[Line: " + versionedComment.getToStartLine() + "]";
		} else {
			headerText += "\t[General File]";
		}
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
				VersionedCommentPart generalCommentsComposite = new VersionedCommentPart(comment, crucibleEditor);
				Control commentControl = generalCommentsComposite.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
			}

		}
		return composite;
	}

}
