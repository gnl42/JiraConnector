/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.wizard.bamboo;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.ui.wizard.AbstractSVNWizard;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Repository location registration wizard
 * 
 * @author Alexander Gurov
 */
public class NewBambooServerWizard extends AbstractSVNWizard implements
		INewWizard {
	protected AddBambooServerPage serverPage;
	protected IBambooServer editable;
	protected boolean performAction;
	protected IBambooServer backup;

	public NewBambooServerWizard() {
		this(null, true);
	}

	public NewBambooServerWizard(IBambooServer editable, boolean performAction) {
		super();
		this.performAction = performAction;
		this.editable = editable;
		if (this.editable != null) {
			this.setWindowTitle(Activator.getDefault().getResource(
					"NewBambooServerWizard.Title.Edit"));
			this.backup = BambooConfigurationStorage.instance()
					.newBambooServer();
			BambooConfigurationStorage.instance().copyBambooServer(this.backup,
					this.editable);
		} else {
			this.setWindowTitle(Activator.getDefault().getResource(
					"NewBambooServerWizard.Title.New"));
		}
	}

	public void addPages() {
		this.addPage(this.serverPage = new AddBambooServerPage(this.editable));
	}

	public IActionOperation getOperationToPerform() {
		return this.serverPage.getOperationToPeform();
	}

	public boolean performCancel() {
		if (this.editable != null) {
			BambooConfigurationStorage.instance().copyBambooServer(
					this.editable, this.backup);
		}
		return super.performCancel();
	}

	public boolean performFinish() {
		if (this.serverPage.performFinish()) {
			if (this.performAction) {
				IActionOperation op = this.serverPage.getOperationToPeform();
				if (op != null) {
					UIMonitorUtil.doTaskBusyDefault(op);
				}
			}

			return true;
		}

		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

}
