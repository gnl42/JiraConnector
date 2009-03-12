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

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowBuildLogAction;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Part displaying the build log
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooBuildLogPart extends AbstractBambooEditorFormPart {

	private ShowBuildLogAction showBuildLogAction;

	public BambooBuildLogPart() {
		super("");
	}

	public BambooBuildLogPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		Section section = createSection(parent, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Text text = createReadOnlyText(toolkit, composite, getBuildLogSummary(), null, true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		toolkit.paintBordersFor(composite);

		section.setClient(composite);
		setSection(toolkit, section);

		return control;
	}

	private String getBuildLogSummary() {
		String[] buildLogLines = buildLog.split("[\r\n]");
		StringBuilder b = new StringBuilder();
		for (int i = buildLogLines.length - 10; i < buildLogLines.length; i++) {
			b.append(buildLogLines[i]);
			if (i + 1 < buildLogLines.length) {
				b.append(System.getProperty("line.separator"));
			}
		}
		return b.toString();
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		showBuildLogAction = new ShowBuildLogAction(bambooBuild);
		showBuildLogAction.setText("Show Build Log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(true);
		toolBarManager.add(showBuildLogAction);
	}
}