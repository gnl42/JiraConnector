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
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
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
		createSectionAndComposite(parent, toolkit, 1, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);

		Display display = parent.getDisplay();
		Color foreground;
		Color contentForeground;
		Color contentBackground;
		Color titleBackground;
		String summary = "Build " + bambooBuild.getPlanKey() + "-" + String.valueOf(bambooBuild.getNumber());

		switch (bambooBuild.getStatus()) {
		case FAILURE:
			foreground = display.getSystemColor(SWT.COLOR_WHITE);
			contentForeground = display.getSystemColor(SWT.COLOR_BLACK);
			contentBackground = new Color(display, FAILED_BACKGROUND_CONTENT);
			titleBackground = new Color(display, FAILED_BACKGROUND_TITLE);
			summary += " failed.";
			break;
		case SUCCESS:
			contentForeground = display.getSystemColor(SWT.COLOR_BLACK);
			foreground = display.getSystemColor(SWT.COLOR_WHITE);
			contentBackground = new Color(display, SUCCESS_BACKGROUND_CONTENT);
			titleBackground = new Color(display, SUCCESS_BACKGROUND_TITLE);
			summary += " was successful.";
			break;
		default:
			foreground = contentForeground = toolkit.getColors().getForeground();
			contentBackground = titleBackground = toolkit.getColors().getBackground();
			summary += " is in an unknown state.";
		}

		Composite titleComp = toolkit.createComposite(mainComposite);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginTop = 3;
		layout.marginWidth = 2;
		titleComp.setLayout(layout);
		titleComp.setBackground(titleBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(titleComp);

		Label label = createLabelControl(toolkit, titleComp, summary);
		label.setAlignment(SWT.CENTER);
		label.setForeground(foreground);
		label.setBackground(titleBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		Composite contentComp = toolkit.createComposite(titleComp);
		layout = new GridLayout();
		contentComp.setLayout(layout);
		contentComp.setBackground(contentBackground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(contentComp);

		label = createLabelControl(toolkit, contentComp, "Build completed on " + bambooBuild.getCompletionDate());
		label.setBackground(contentBackground);
		label.setForeground(contentForeground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		label = createLabelControl(toolkit, contentComp, "Build took " + bambooBuild.getDurationDescription());
		label.setBackground(contentBackground);
		label.setForeground(contentForeground);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		Link link = new Link(contentComp, SWT.NONE);
		link.setText("Build reason: " + bambooBuild.getReason());
		link.setBackground(contentBackground);
		link.setForeground(contentForeground);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TasksUiUtil.openUrl(e.text);
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(link);

		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(titleComp);

		toolkit.paintBordersFor(mainComposite);

		setSection(toolkit, section);

		return control;
	}
}