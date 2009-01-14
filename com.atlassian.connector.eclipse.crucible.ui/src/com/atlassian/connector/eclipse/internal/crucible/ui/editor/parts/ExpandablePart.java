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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A UI part that is expandable like a tree
 * 
 * @author Shawn Minto
 */
public abstract class ExpandablePart {

	private Section commentSection;

	protected final CrucibleReviewEditorPage crucibleEditor;

	public ExpandablePart(CrucibleReviewEditorPage editor) {
		this.crucibleEditor = editor;
	}

	public Control createControl(Composite parent, final FormToolkit toolkit) {

		String headerText = getSectionHeaderText();

		int style = ExpandableComposite.TWISTIE;
		commentSection = toolkit.createSection(parent, style);
		commentSection.setText(headerText);
		commentSection.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentSection);

		if (commentSection.isExpanded() || crucibleEditor == null) {
			Composite composite = createSectionContents(commentSection, toolkit);
			commentSection.setClient(composite);
		} else {
			commentSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (commentSection.getClient() == null) {
						try {
							if (crucibleEditor != null) {
								crucibleEditor.setReflow(false);
							}
							Composite composite = createSectionContents(commentSection, toolkit);
							commentSection.setClient(composite);
						} finally {
							if (crucibleEditor != null) {
								crucibleEditor.setReflow(true);
							}
						}
						if (crucibleEditor != null) {
							crucibleEditor.reflow();
						}
					}
				}
			});
		}
		return commentSection;
	}

	protected abstract String getSectionHeaderText();

	protected abstract Composite createSectionContents(Section section, FormToolkit toolkit);

}
