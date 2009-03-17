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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part displaying important Build details
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooDetailsPart extends AbstractBambooEditorFormPart {

	public BambooDetailsPart() {
		super("");
	}

	public BambooDetailsPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		super.toolkit = toolkit;
		createSectionAndComposite(parent, toolkit, 1, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR);

		Display display = parent.getDisplay();
		Color foreground;
		Color titleBackground;
		Color mainBackground;
		Color sepBackground;

		String summary = "Build " + bambooBuild.getPlanKey() + "-" + String.valueOf(bambooBuild.getNumber());

		switch (bambooBuild.getStatus()) {
		case FAILURE:
			foreground = display.getSystemColor(SWT.COLOR_WHITE);
			titleBackground = new Color(display, FAILED_BACKGROUND_TITLE);
			mainBackground = new Color(display, FAILED_BACKGROUND_MAIN);
			sepBackground = new Color(display, FAILED_BACKGROUND_SEPARATOR);
			summary += " failed.";
			break;
		case SUCCESS:
			foreground = display.getSystemColor(SWT.COLOR_WHITE);
			titleBackground = new Color(display, SUCCESS_BACKGROUND_TITLE);
			mainBackground = new Color(display, SUCCESS_BACKGROUND_MAIN);
			sepBackground = new Color(display, SUCCESS_BACKGROUND_SEPARATOR);
			summary += " was successful.";
			break;
		default:
			foreground = toolkit.getColors().getForeground();
			titleBackground = mainBackground = sepBackground = toolkit.getColors().getBackground();
			summary += " is in an unknown state.";
		}

		mainComposite.setBackground(mainBackground);

		Composite sepComp = toolkit.createComposite(mainComposite);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		sepComp.setLayout(layout);
		sepComp.setBackground(sepBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sepComp);

		Label label = createLabelControl(toolkit, sepComp, summary);
		label.setAlignment(SWT.CENTER);
		label.setForeground(foreground);
		label.setBackground(titleBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		label = createLabelControl(toolkit, mainComposite, "Build completed on " + bambooBuild.getCompletionDate());
		label.setForeground(foreground);
		label.setBackground(mainBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		label = createLabelControl(toolkit, mainComposite, "Build took " + bambooBuild.getDurationDescription());
		label.setForeground(foreground);
		label.setBackground(mainBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		label = createLabelControl(toolkit, mainComposite, "Build reason: " + bambooBuild.getReason());
		label.setForeground(foreground);
		label.setBackground(mainBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		toolkit.paintBordersFor(mainComposite);

		section.setClient(mainComposite);
		setSection(toolkit, section);

		return control;
	}
}
