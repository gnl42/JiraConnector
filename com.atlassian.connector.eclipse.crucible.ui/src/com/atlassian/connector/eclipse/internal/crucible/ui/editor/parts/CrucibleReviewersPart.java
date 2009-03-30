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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.ui.forms.SizeCachingComposite;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.util.Iterator;
import java.util.Set;

/**
 * Form part that displays the details of a reviewer
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewersPart {

	private final Set<Reviewer> reviewers;

	private Menu menu;

	public CrucibleReviewersPart(Set<Reviewer> reviewers) {
		super();
		this.reviewers = reviewers;
	}

	public Composite createControl(FormToolkit toolkit, Composite parent) {
		return createControl(toolkit, parent, "Reviewers:   ");
	}

	public Composite createControl(FormToolkit toolkit, Composite parent, String labelText) {
		//CHECKSTYLE:MAGIC:OFF

		Composite reviewersComposite = createComposite(toolkit, parent);

		// use indent to make up for forms border gap
		GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(reviewersComposite);
		RowLayout layout = new RowLayout();
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginLeft = 0;
		layout.marginWidth = 0;
		layout.spacing = 0;
		reviewersComposite.setLayout(layout);

		createLabelControl(toolkit, reviewersComposite, labelText);

		if (reviewers.isEmpty()) {
			// avoid blank gap on Linux
			createLabelControl(toolkit, reviewersComposite, " ");
		} else {
			Iterator<Reviewer> iterator = reviewers.iterator();
			while (iterator.hasNext()) {
				Reviewer reviewer = iterator.next();
				Text text = createReadOnlyText(toolkit, reviewersComposite, reviewer.getDisplayName(), null, false);
				text.setBackground(parent.getBackground());

				if (reviewer.isCompleted()) {
					Label imageLabel = createLabelControl(toolkit, reviewersComposite, "");
					imageLabel.setImage(CrucibleImages.getImage(CrucibleImages.REVIEWER_COMPLETE));
				}

				if (iterator.hasNext()) {
					createReadOnlyText(toolkit, reviewersComposite, ", ", null, false);
				}
			}
		}
		return reviewersComposite;
		//CHECKSTYLE:MAGIC:ON
	}

	private Composite createComposite(FormToolkit toolkit, Composite parent) {
		if (toolkit != null) {
			Composite composite = new SizeCachingComposite(parent, SWT.NONE);
			if (this.menu != null) {
				EditorUtil.setMenu(composite, null);
			}
			toolkit.adapt(composite);
			return composite;
		} else {
			return new SizeCachingComposite(parent, SWT.NONE);
		}
	}

	private Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline) {

		if (labelString != null) {
			createLabelControl(toolkit, composite, labelString);
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP;
		}
		Text text = new Text(composite, style);
		text.setFont(EditorUtil.TEXT_FONT);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);

		if (toolkit != null) {
			toolkit.adapt(text, true, true);
		}

		return text;
	}

	private Label createLabelControl(FormToolkit toolkit, Composite composite, String labelString) {

		Label labelControl = null;
		boolean isD = composite.isDisposed();
		if (toolkit != null) {
			labelControl = toolkit.createLabel(composite, labelString);
			labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		} else {
			labelControl = new Label(composite, SWT.NONE);
			labelControl.setText(labelString);
		}

		return labelControl;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}
}
