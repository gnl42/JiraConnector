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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.ExpandablePart;
import com.atlassian.connector.eclipse.ui.editor.AbstractFormPagePart;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Collection;

/**
 * A form part that needs to be aware of the review that it is displaying
 * 
 * @author Shawn Minto
 */
public abstract class AbstractCrucibleEditorFormPart extends AbstractFormPagePart {

	private Section expandableSection;

	public abstract void initialize(CrucibleReviewEditorPage editor, Review review);

	public abstract CrucibleReviewEditorPage getReviewEditor();

	public abstract Collection<? extends ExpandablePart> getExpandableParts();

	protected void collapseAll() {
		if (getExpandableParts() == null) {
			return;
		}
		try {
			getReviewEditor().setReflow(false);

			for (ExpandablePart part : getExpandableParts()) {
				part.setExpanded(false);
			}

			if (expandableSection != null) {
				EditorUtil.toggleExpandableComposite(false, expandableSection);
			}
		} finally {
			getReviewEditor().setReflow(true);
		}
		getReviewEditor().reflow();
	}

	protected void expandAll() {
		if (getExpandableParts() == null) {
			return;
		}
		try {
			getReviewEditor().setReflow(false);

			if (expandableSection != null && !expandableSection.isExpanded()) {
				EditorUtil.toggleExpandableComposite(true, expandableSection);
			}

			for (ExpandablePart part : getExpandableParts()) {
				part.setExpanded(true);

			}

		} finally {
			getReviewEditor().setReflow(true);
		}
		getReviewEditor().reflow();
	}

	protected void fillToolBar(ToolBarManager toolBarManager) {
		Action collapseAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				collapseAll();
			}
		};
		collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL_SMALL);
		collapseAllAction.setToolTipText("Collapse All");
		toolBarManager.add(collapseAllAction);

		Action expandAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				expandAll();
			}
		};
		expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL_SMALL);
		expandAllAction.setToolTipText("Expand All");
		toolBarManager.add(expandAllAction);
	}

	protected void setSection(FormToolkit toolkit, Section section) {
		this.expandableSection = section;
		if (section.getTextClient() == null) {
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
			fillToolBar(toolBarManager);

			// TODO toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

			if (toolBarManager.getSize() > 0) {
				Composite toolbarComposite = toolkit.createComposite(section);
				toolbarComposite.setBackground(null);
				RowLayout rowLayout = new RowLayout();
				rowLayout.marginTop = 0;
				rowLayout.marginBottom = 0;
				toolbarComposite.setLayout(rowLayout);

				toolBarManager.createControl(toolbarComposite);
				section.setTextClient(toolbarComposite);
			}
		}
	}

}
