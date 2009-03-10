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
import org.eclipse.ui.forms.widgets.FormToolkit;

import java.text.DateFormat;

/**
 * Summary row of a bamboo build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooSummaryPart extends AbstractBambooEditorFormPart {

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(8, false);
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		createReadOnlyText(toolkit, composite, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
				.format(bambooBuild.getCompletionDate()), "     Completed:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getDurationDescription(), "     Build took:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getReason(), "     Build Reason:", false);
		createReadOnlyText(toolkit, composite, bambooBuild.getProjectName(), "     Project:", false);

		toolkit.paintBordersFor(composite);

		control = composite;

		return control;
	}
}
