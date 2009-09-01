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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A wizard for creating a patch file by running the SVN diff command.
 */
public class CreatePreCommitReviewWizard extends Wizard {

	class PreCommitOptionsPage extends OptionsPage {

		public PreCommitOptionsPage(String pageName, String title, ImageDescriptor titleImage) {
			super(pageName, title, titleImage);
		}

	}

	private PatchFileSelectionPage mainPage;

	private final IStructuredSelection selection;

	private final IResource[] unaddedResources;

	private final HashMap statusMap;

	private IResource[] selectedResources;

	private OptionsPage optionsPage;

	// end of PatchFileCreationOptionsPage

	public CreatePreCommitReviewWizard(IStructuredSelection selection, IResource[] unaddedResources, HashMap statusMap) {
		super();
		this.selection = selection;
		this.unaddedResources = unaddedResources;
		this.statusMap = statusMap;
		setWindowTitle("Create pre-commit review");
		initializeDefaultPageImageDescriptor();
	}

	@Override
	public void addPages() {
		String pageTitle = "Create pre-commit review"; //$NON-NLS-1$
		String pageDescription = Policy.bind("GenerateSVNDiff.pageDescription"); //$NON-NLS-1$
		mainPage = new PatchFileSelectionPage(pageTitle, pageTitle, SVNUIPlugin.getPlugin().getImageDescriptor(
				ISVNUIConstants.IMG_WIZBAN_DIFF), selection, statusMap);
		mainPage.setDescription(pageDescription);

		pageTitle = Policy.bind("GenerateSVNDiff.AdvancedOptions"); //$NON-NLS-1$
		pageDescription = Policy.bind("GenerateSVNDiff.ConfigureOptions");
		optionsPage = new PreCommitOptionsPage(pageTitle, pageTitle, SVNUIPlugin.getPlugin().getImageDescriptor(
				ISVNUIConstants.IMG_WIZBAN_DIFF));
		optionsPage.setDescription(pageDescription);
		addPage(mainPage);
		addPage(optionsPage);

	}

	/**
	 * Initializes this creation wizard using the passed workbench and object selection.
	 * 
	 * @param workbench
	 *            the current workbench
	 * @param selection
	 *            the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * Declares the wizard banner iamge descriptor
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath;
		iconPath = "icons/full/";
		try {
			URL installURL = SVNUIPlugin.getPlugin().getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, iconPath + "wizards/newconnect_wiz.gif"); //$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		} catch (MalformedURLException e) {
			// Should not happen.  Ignore.
		}
	}

	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	/**
	 * Completes processing of the wizard. If this method returns <code>
	 * true</code>, the wizard will close; otherwise, it will
	 * stay active.
	 */
	@Override
	public boolean performFinish() {
		boolean eclipseFormat = optionsPage.isMultiPatch();
		boolean projectRelative = optionsPage.isProjectRelative();

		try {
			GenerateDiffFileOperation generateDiffFileOperation = new GenerateDiffFileOperation(getResources(),
					getUnaddedResources(), null, true, false, eclipseFormat, projectRelative, getShell());
			generateDiffFileOperation.setSelectedResources(selectedResources);
			getContainer().run(true, true, generateDiffFileOperation);
			return true;
		} catch (InterruptedException e1) {
			return true;
		} catch (InvocationTargetException e2) {
			SVNUIPlugin.openError(getShell(), Policy.bind("GenerateSVNDiff.error"), null, e2);
			return false;
		}
	}

	protected IResource[] getResources() {
		return mainPage.getSelectedResources();
	}

	private IResource[] getUnaddedResources() {
		ArrayList unaddedResourceList = new ArrayList();
		for (IResource unaddedResource : unaddedResources) {
			unaddedResourceList.add(unaddedResource);
		}
		ArrayList selectedUnaddedResourceList = new ArrayList();
		IResource[] selectedResources = getResources();
		for (IResource selectedResource : selectedResources) {
			if (unaddedResourceList.contains(selectedResource)) {
				selectedUnaddedResourceList.add(selectedResource);
			} else {
				IResource unaddedParent = getUnaddedParent(selectedResource, unaddedResourceList);
				if (unaddedParent != null && !selectedUnaddedResourceList.contains(unaddedParent)) {
					selectedUnaddedResourceList.add(unaddedParent);
				}
			}
		}
		IResource[] unaddedResourceArray = new IResource[selectedUnaddedResourceList.size()];
		selectedUnaddedResourceList.toArray(unaddedResourceArray);
		return unaddedResourceArray;
	}

	private IResource getUnaddedParent(IResource resource, ArrayList unaddedResourceList) {
		IResource parent = resource;
		while (parent != null) {
			parent = parent.getParent();
			int index = unaddedResourceList.indexOf(parent);
			if (index != -1) {
				return (IResource) unaddedResourceList.get(index);
			}
		}
		return null;
	}

	public void setSelectedResources(IResource[] selectedResources) {
		this.selectedResources = selectedResources;
	}
}
