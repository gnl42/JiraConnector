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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
		//leave it empty for now until ACC-29 is done

//		section = createSection(parent, toolkit, ExpandableComposite.NO_TITLE | ExpandableComposite.EXPANDED
//				| ExpandableComposite.TWISTIE);

//		Composite composite = toolkit.createComposite(section, SWT.BORDER);
//		GridLayout layout = new GridLayout();
//		layout.numColumns = 8;
//		composite.setLayout(layout);
//
//		//TODO
//		String buildNr;
//		try {
//			buildNr = String.valueOf(bambooBuild.getNumber());
//		} catch (UnsupportedOperationException e) {
//			buildNr = "N/A";
//		}
//		int passedTests = bambooBuild.getTestsPassed();
//		int failedTests = bambooBuild.getTestsFailed();
//
//		StringBuilder builder = new StringBuilder();
//		builder.append("Build ");
//		builder.append(bambooBuild.getPlanKey());
//		builder.append("-");
//		builder.append(buildNr);
//
//		if (bambooBuild.getStatus() == BuildStatus.SUCCESS) {
//			builder.append(" succeeded");
//			if (passedTests <= 0) {
//				builder.append(" [testless build].");
//			} else {
//				builder.append(" with ");
//				builder.append(String.valueOf(passedTests));
//				builder.append(" passing tests.");
//			}
//		} else if (bambooBuild.getStatus() == BuildStatus.FAILURE) {
//			builder.append(" failed");
//			if (failedTests <= 0) {
//				builder.append(" [testless build].");
//			} else {
//				builder.append(" with ");
//				builder.append(String.valueOf(failedTests));
//				builder.append(" failing tests.");
//			}
//		} else {
//			builder.append(" disabled / Build data unavailable.");
//		}
//		createReadOnlyText(toolkit, composite, builder.toString(), null, false);
//
//		//TODO more Content here
//
//		toolkit.paintBordersFor(composite);
//
//		section.setClient(composite);
//		setSection(toolkit, section);
//
//		return control;

		return parent;
	}
}
