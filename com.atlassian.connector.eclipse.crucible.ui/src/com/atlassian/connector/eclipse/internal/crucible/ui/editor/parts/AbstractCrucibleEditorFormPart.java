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

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.connector.eclipse.ui.editor.AbstractFormPagePart;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFormUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A form part that needs to be aware of the review that it is displaying
 * 
 * @author Shawn Minto
 */
public abstract class AbstractCrucibleEditorFormPart extends AbstractFormPagePart {

	/*
	 * changable attributes in a review
	 */
	public enum ReviewAttributeType {
		TITLE, OBJECTIVE, REVIEWERS, FILES;
	}

	/*
	 * attributes that got changed in the this part
	 */
	protected final Map<ReviewAttributeType, Object> changedAttributes;

	private Section expandableSection;

	public abstract void initialize(CrucibleReviewEditorPage editor, Review review, boolean isNewReview);

	public abstract CrucibleReviewEditorPage getReviewEditor();

	public abstract Collection<? extends ExpandablePart<?, ?>> getExpandableParts();

	public AbstractCrucibleEditorFormPart() {
		changedAttributes = new HashMap<ReviewAttributeType, Object>();
	}

	protected void collapseAll() {
		if (getExpandableParts() == null) {
			return;
		}
		try {
			getReviewEditor().setReflow(false);

			for (ExpandablePart<?, ?> part : getExpandableParts()) {
				part.setExpanded(false);
			}
		} finally {
			getReviewEditor().setReflow(true);
		}
		getReviewEditor().reflow(false);
	}

	protected void expandAll() {
		if (getExpandableParts() == null) {
			return;
		}
		try {
			getReviewEditor().setReflow(false);

			for (ExpandablePart<?, ?> part : getExpandableParts()) {
				part.setExpanded(true);
			}
			if (expandableSection != null && !expandableSection.isExpanded()) {
				CommonFormUtil.setExpanded(expandableSection, true);
			}
		} finally {
			getReviewEditor().setReflow(true);
		}
		getReviewEditor().reflow(false);
	}

	protected void fillToolBar(ToolBarManager toolBarManager) {
		Action collapseAllAction = new Action("") {
			@Override
			public void run() {
				collapseAll();
			}
		};
		collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL);
		collapseAllAction.setToolTipText("Collapse All");
		toolBarManager.add(collapseAllAction);

		Action expandAllAction = new Action("") {
			@Override
			public void run() {
				expandAll();
			}
		};
		expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL);
		expandAllAction.setToolTipText("Expand All");
		toolBarManager.add(expandAllAction);
	}

	private ToolBarManager toolBarManager;

	protected ToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	protected void setSection(FormToolkit toolkit, Section section) {
		this.expandableSection = section;
		if (section.getTextClient() == null) {
			toolBarManager = new ToolBarManager(SWT.FLAT);
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

	public abstract void updateControl(Review review, Composite parent, FormToolkit toolkit);

	/**
	 * Retrieves a copy of the changed attributes map
	 * 
	 * @return
	 */
	public Map<ReviewAttributeType, Object> getChangedAttributes() {
		return new HashMap<ReviewAttributeType, Object>(changedAttributes);
	}

	public boolean hasChangedAttributes() {
		return changedAttributes.size() > 0;
	}
}
