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

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.RepositorySelectionWizard;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SelectCrucibleVersionOrNewerRepositoryPage;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.SnippetReviewWizard;
import com.atlassian.connector.eclipse.ui.actions.AbstractResourceAction;
import com.atlassian.connector.eclipse.ui.commons.ResourceEditorBean;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * @author Jacek Jaroczynski
 * @author Pawel Niewiadomski
 */
public class CreateSnippetReviewFromResourcesAction extends AbstractResourceAction {

	public CreateSnippetReviewFromResourcesAction() {
		super("Create Snippet Review Action");
	}

	@Override
	protected void processResources(List<ResourceEditorBean> selection, Shell shell) {
		SelectCrucibleVersionOrNewerRepositoryPage selectRepositoryPage = new SelectCrucibleVersionOrNewerRepositoryPage(
				new CrucibleVersionInfo("2.3", null)) {
			@Override
			protected IWizard createWizard(TaskRepository taskRepository) {
				return new SnippetReviewWizard(taskRepository, getSelectionData().get(0));
			}
		};

		WizardDialog wd = new WizardDialog(WorkbenchUtil.getShell(),
				new RepositorySelectionWizard(selectRepositoryPage));
		wd.setBlockOnOpen(true);
		wd.open();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && getSelectionData().size() == 1
				&& getSelectionData().get(0).getResource().getType() == IResource.FILE;
	}

}
