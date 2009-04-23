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
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Page for selecting which kind of review to create
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleTypeSelectionPage extends WizardPage {

	private final TaskRepository taskRepository;

	private Type selectedType;

	public CrucibleTypeSelectionPage(TaskRepository repository) {
		super("crucibleSelection"); //$NON-NLS-1$
		setTitle("Select type of review to create");
		setDescription("Please select which kind of review you want to create.");
		this.taskRepository = repository;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		new Label(composite, SWT.NONE).setText("Do you want to create a");

		Composite buttonComp = new Composite(composite, SWT.NULL);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().margins(10, 5).create());

		Button emptyReview = new Button(buttonComp, SWT.RADIO);
		emptyReview.setText("Empty Review without files,");
		emptyReview.setSelection(true);
		emptyReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedType = Type.EMPTY;
			}
		});
		Button changesetReview = new Button(buttonComp, SWT.RADIO);
		changesetReview.setText("Review from changesets,");
		changesetReview.setSelection(false);
		changesetReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedType = Type.ADD_CHANGESET;
			}
		});
		Button patchReview = new Button(buttonComp, SWT.RADIO);
		patchReview.setText("Review from a patch, or");
		patchReview.setSelection(false);
		patchReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedType = Type.ADD_PATCH;
			}
		});
		Button allReview = new Button(buttonComp, SWT.RADIO);
		allReview.setText("Review with all options.");
		allReview.setSelection(false);
		allReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedType = Type.ALL;
			}
		});
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonComp);
		setControl(composite);
	}

	public Type getType() {
		return selectedType;
	}
}
