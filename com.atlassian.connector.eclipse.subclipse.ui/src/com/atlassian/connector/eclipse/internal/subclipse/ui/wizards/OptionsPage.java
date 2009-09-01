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
/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.subclipse.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.tigris.subversion.subclipse.ui.IHelpContextIds;
import org.tigris.subversion.subclipse.ui.Policy;

import java.util.HashSet;
import java.util.Set;

public class OptionsPage extends WizardPage {

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	protected OptionsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * The possible root of the patch
	 */
	public final static int ROOT_WORKSPACE = 1;

	public final static int ROOT_PROJECT = 2;

	public final static int ROOT_SELECTION = 3;

	private Button workspaceRelativeOption; //multi-patch format

	private Button projectRelativeOption; //full project path

	private Button selectionRelativeOption; //use path of whatever is selected

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		setControl(composite);

		// set F1 help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.PATCH_OPTIONS_PAGE);

		//Unified Format Options
		Group unifiedGroup = new Group(composite, SWT.None);
		layout = new GridLayout();
		unifiedGroup.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		unifiedGroup.setLayoutData(data);
		unifiedGroup.setText(Policy.bind("OptionsPage.patchRoot")); //$NON-NLS-1$

		workspaceRelativeOption = new Button(unifiedGroup, SWT.RADIO);
		workspaceRelativeOption.setText(Policy.bind("OptionsPage.workspace")); //$NON-NLS-1$
		workspaceRelativeOption.setSelection(true);

		projectRelativeOption = new Button(unifiedGroup, SWT.RADIO);
		projectRelativeOption.setText(Policy.bind("OptionsPage.project")); //$NON-NLS-1$

		selectionRelativeOption = new Button(unifiedGroup, SWT.RADIO);
		selectionRelativeOption.setText(Policy.bind("OptionsPage.selection")); //$NON-NLS-1$

		Dialog.applyDialogFont(parent);

		//check to see if this is a multi select patch, if so disable 
		IResource[] tempResources = ((CreatePreCommitReviewWizard) this.getWizard()).getResources();

		Set projects = new HashSet();
		for (IResource tempResource : tempResources) {
			projects.add(tempResource.getProject());
		}

		if (projects.size() > 1) {
			projectRelativeOption.setEnabled(false);
			selectionRelativeOption.setEnabled(false);
		}

		workspaceRelativeOption.setSelection(true);
	}

	public boolean isProjectRelative() {
		return projectRelativeOption.getSelection();
	}

	public boolean isMultiPatch() {
		return workspaceRelativeOption.getSelection();
	}

}