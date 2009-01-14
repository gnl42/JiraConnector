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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The form part that displays the details of the review
 * 
 * @author Shawn Minto
 */
public class CrucibleDetailsPart extends AbstractCrucibleEditorFormPart {

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

//		toolkit.createLabel(composite, "Name");
//
//		Text nameText = toolkit.createText(composite, crucibleReview.getName(), SWT.READ_ONLY);
//		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(nameText);
//
//		toolkit.createLabel(composite, "Author");
//
//		Text authorText = toolkit.createText(composite, crucibleReview.getAuthor().getDisplayName(), SWT.READ_ONLY);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(authorText);
//
//		toolkit.createLabel(composite, "Moderator");
//
//		Text moderatorText = toolkit.createText(composite, crucibleReview.getModerator().getDisplayName(),
//				SWT.READ_ONLY);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(moderatorText);
//
//		toolkit.createLabel(composite, "Reviewers");
//
//		String reviewerString = "";
//		try {
//			for (Reviewer reviewer : crucibleReview.getReviewers()) {
//				reviewerString += reviewer.getDisplayName() + ",";
//			}
//		} catch (ValueNotYetInitialized e) {
//			// TODO do something different here?
//			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
//		}
//		if (reviewerString.length() > 0) {
//			reviewerString = reviewerString.substring(0, reviewerString.length() - 1);
//		}
//
//		Text reviewersText = toolkit.createText(composite, reviewerString, SWT.READ_ONLY);
//		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(reviewersText);
//
//		toolkit.createLabel(composite, "Description");
//
//		Text descriptionText = toolkit.createText(composite, crucibleReview.getDescription(), SWT.READ_ONLY | SWT.MULTI
//				| SWT.WRAP);
//		GridDataFactory.fillDefaults().grab(true, true).hint(400, 100).span(4, 1).applyTo(descriptionText);
		//CHECKSTYLE:MAGIC:ON

		return composite;
	}

}
