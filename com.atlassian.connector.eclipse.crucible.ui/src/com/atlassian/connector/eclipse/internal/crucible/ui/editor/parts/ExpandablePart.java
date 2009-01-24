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
import com.atlassian.connector.eclipse.ui.AtlassianImages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import java.util.List;

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

	public CrucibleReviewEditorPage getCrucibleEditor() {
		return crucibleEditor;
	}

	public Control createControl(Composite parent, final FormToolkit toolkit) {

		String headerText = getSectionHeaderText();

		int style = ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT | ExpandableComposite.TWISTIE;

		if (crucibleEditor == null) {
			style |= ExpandableComposite.EXPANDED;
		}

		commentSection = toolkit.createSection(parent, style);
		commentSection.setText(headerText);
		commentSection.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(commentSection);

		final Composite actionsComposite = createSectionAnnotationsAndToolbar(commentSection, toolkit);

		if (commentSection.isExpanded() || crucibleEditor == null) {
			fillToolBar(actionsComposite, toolkit, true);
			Composite composite = createSectionContents(commentSection, toolkit);
			commentSection.setClient(composite);

		} else {
			fillToolBar(actionsComposite, toolkit, false);
			commentSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {

					fillToolBar(actionsComposite, toolkit, e.getState());

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

	private void fillToolBar(Composite actionsComposite, FormToolkit toolkit, boolean isExpanded) {
		List<IAction> toolbarActions = getToolbarActions(isExpanded);

		for (Control control : actionsComposite.getChildren()) {
			if (control instanceof ImageHyperlink) {
				control.dispose();
			}
		}

		if (toolbarActions != null) {

			for (final IAction action : toolbarActions) {
				createActionHyperlink(actionsComposite, toolkit, action);
			}
		}
		actionsComposite.getParent().layout();
	}

	protected ImageHyperlink createActionHyperlink(Composite actionsComposite, FormToolkit toolkit, final IAction action) {
		ImageHyperlink link = toolkit.createImageHyperlink(actionsComposite, SWT.NONE);
		if (action.getImageDescriptor() != null) {
			link.setImage(AtlassianImages.getImage(action.getImageDescriptor()));
		} else {
			link.setText(action.getText());
		}
		link.setToolTipText(action.getToolTipText());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				action.run();
			}
		});
		return link;
	}

	/**
	 * @return A composite that image hyperlinks can be placed on
	 */
	protected Composite createSectionAnnotationsAndToolbar(Section section, FormToolkit toolkit) {

		Composite toolbarComposite = toolkit.createComposite(section);
		section.setTextClient(toolbarComposite);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		toolbarComposite.setLayout(rowLayout);

		Composite annotationsComposite = toolkit.createComposite(toolbarComposite);

		rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 0;

		annotationsComposite.setLayout(rowLayout);

		ImageDescriptor annotationImage = getAnnotationImage();
		if (annotationImage != null) {
			Label imageLabel = toolkit.createLabel(annotationsComposite, "");
			imageLabel.setImage(AtlassianImages.getImage(annotationImage));
		}

		String annotationsText = getAnnotationText();
		if (annotationsText == null) {
			annotationsText = "";
		}
		toolkit.createLabel(annotationsComposite, annotationsText);

		createCustomAnnotations(annotationsComposite, toolkit);

//		Composite actionsComposite = toolkit.createComposite(toolbarComposite);
//		actionsComposite.setBackground(null);
//		rowLayout = new RowLayout();
//		rowLayout.marginTop = 0;
//		rowLayout.marginBottom = 0;
//		actionsComposite.setLayout(rowLayout);

		return toolbarComposite;
	}

	protected void createCustomAnnotations(Composite toolbarComposite, FormToolkit toolkit) {
		// default do nothing
	}

	protected abstract List<IAction> getToolbarActions(boolean isExpanded);

	protected abstract String getAnnotationText();

	protected abstract ImageDescriptor getAnnotationImage();

	protected abstract String getSectionHeaderText();

	protected abstract Composite createSectionContents(Section section, FormToolkit toolkit);

}
