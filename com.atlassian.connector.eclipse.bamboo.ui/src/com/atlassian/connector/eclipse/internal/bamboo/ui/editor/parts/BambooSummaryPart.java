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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Summary row of a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooSummaryPart extends AbstractBambooEditorFormPart {

	private Text text;

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		super.toolkit = toolkit;
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(6, false);
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(composite);

		text = createReadOnlyText(toolkit, composite, bambooBuild.getBuild().getProjectName(), "Project:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getBuild().getPlanName(), "Plan:", false);
		createReadOnlyText(toolkit, composite, String.valueOf(bambooBuild.getBuild().getNumber()), "Build:", false);

		toolkit.paintBordersFor(composite);

		control = composite;

		return control;
	}

	@Override
	public void setFocus() {
		text.forceFocus();
	}
}
