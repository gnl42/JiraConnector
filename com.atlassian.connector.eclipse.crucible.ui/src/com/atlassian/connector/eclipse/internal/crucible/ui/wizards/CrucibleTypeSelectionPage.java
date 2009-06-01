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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.CrucibleReviewWizard.Type;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Page for selecting which kind of review to create
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleTypeSelectionPage extends WizardPage {

	private Button patchReview;

	private Button changesetReview;

	public CrucibleTypeSelectionPage() {
		super("crucibleSelection"); //$NON-NLS-1$
		setTitle("Select type of review to create");
		setDescription("Select which kind of review you want to create.");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).create());

		new Label(composite, SWT.NONE).setText("Select how you want to add files to the review:");

		Composite buttonComp = new Composite(composite, SWT.NULL);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().margins(10, 5).create());

		changesetReview = new Button(buttonComp, SWT.CHECK);
		changesetReview.setText("From a Changeset");
		patchReview = new Button(buttonComp, SWT.CHECK);
		patchReview.setText("From a Patch");

		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonComp);
		setControl(composite);
	}

	public Type getType() {
		if (patchReview.getSelection()) {
			if (changesetReview.getSelection()) {
				return Type.ALL;
			}
			return Type.ADD_PATCH;
		}
		if (changesetReview.getSelection()) {
			return Type.ADD_CHANGESET;
		}
		return Type.EMPTY;
	}
}
