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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * The form part that displays the general comments for the review
 * 
 * @author Shawn Minto
 */
public class CrucibleGeneralCommentsPart extends AbstractCrucibleEditorFormPart {

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;
		Section section = toolkit.createSection(parent, style);
		section.setText("General Comments");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);

		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		section.setClient(composite);
		//CHECKSTYLE:MAGIC:ON
		return section;
	}
}
