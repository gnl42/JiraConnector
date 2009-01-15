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
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
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
public class GeneralCommentPart extends CommentPart {

	private final GeneralComment generalComment;

	public GeneralCommentPart(GeneralComment comment, CrucibleReviewEditorPage editor) {
		super(comment, editor);
		this.generalComment = comment;
	}

	@Override
	protected String getSectionHeaderText() {
		return generalComment.getAuthor().getDisplayName()
				+ " "
				+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
						generalComment.getCreateDate());
	}

	@Override
	protected Composite createSectionContents(Section section, FormToolkit toolkit) {
		Composite composite = super.createSectionContents(section, toolkit);

		if (generalComment.getReplies().size() > 0) {
			List<GeneralComment> generalComments = new ArrayList<GeneralComment>(generalComment.getReplies());
			Collections.sort(generalComments, new Comparator<GeneralComment>() {

				public int compare(GeneralComment o1, GeneralComment o2) {
					if (o1 != null && o2 != null) {
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					return 0;
				}

			});

			for (GeneralComment comment : generalComments) {
				GeneralCommentPart generalCommentsComposite = new GeneralCommentPart(comment, crucibleEditor);
				Control commentControl = generalCommentsComposite.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(commentControl);
			}

		}
		return composite;
	}

	@Override
	protected String getAnnotationText() {
		// TODO make the text be based on the numbers properly (e.g. s's)
		String text = "";
		if (generalComment.isDefectRaised() || generalComment.isDefectApproved()) {

			text = "DEFECT ";
		}
		return text + "[" + generalComment.getReplies().size() + " replies]";
	}

	@Override
	protected ImageDescriptor getAnnotationImage() {
		if (generalComment.isDefectRaised() || generalComment.isDefectApproved()) {

			// TODO get an image for a bug
			return null;
		}
		return null;
	}

}
