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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.util.ResourceSelectionTree;

import java.util.HashMap;

/**
 * Page to select a patch file. Overriding validatePage was necessary to allow entering a file name that already exists.
 */
class PatchFileSelectionPage extends WizardPage {
	private ResourceSelectionTree resourceSelectionTree;

	private final IResource[] resources;

	private final HashMap<?, ?> statusMap;

	public PatchFileSelectionPage(String pageName, String title, ImageDescriptor image, IStructuredSelection selection,
			HashMap<?, ?> statusMap) {
		super(pageName, title, image);
		this.statusMap = statusMap;
		Object[] selectedResources = selection.toArray();
		resources = new IResource[selectedResources.length];
		for (int i = 0; i < selectedResources.length; i++) {
			resources[i] = (IResource) selectedResources[i];
		}
		setPageComplete(false);
	}

	protected void validatePage() {
		setPageComplete(getSelectedResources().length > 0);
	}

	/**
	 * Allow the user to chose to save the patch to the workspace or outside of the workspace.
	 */
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());
		setControl(composite);
		initializeDialogUnits(composite);

//		LabelProvider labelProvider = new SVNLightweightDecorator();
		resourceSelectionTree = new ResourceSelectionTree(composite, SWT.NONE,
				Policy.bind("GenerateSVNDiff.Changes"), resources, statusMap, null, true, null, null); //$NON-NLS-1$
		((CheckboxTreeViewer) resourceSelectionTree.getTreeViewer()).setAllChecked(true);

		resourceSelectionTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validatePage();
			}
		});

		validatePage();
		updateEnablements();
	}

	public IResource[] getSelectedResources() {
		return resourceSelectionTree.getSelectedResources();
	}

	protected void updateEnablements() {
		// nothing to do for now
	}

}