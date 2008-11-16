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

package com.atlassian.theplugin.eclipse.ui.action.bamboo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.CompositeOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.RefreshBambooServersOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.SaveBambooServersOperation;
import com.atlassian.theplugin.eclipse.ui.wizard.bamboo.NewBambooServerWizard;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Edit repository location properties action implementation
 * 
 * @author Alexander Gurov
 */
public class EditBambooServerPropertiesAction extends AbstractBambooServerAction {

	public EditBambooServerPropertiesAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		final IBambooServer []servers = this.getSelectedBambooServers();
		
		final IBambooServer backup = BambooConfigurationStorage.instance().newBambooServer();
		BambooConfigurationStorage.instance().copyBambooServer(backup, servers[0]);
		
		NewBambooServerWizard wizard = new NewBambooServerWizard(servers[0], false);
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		if (dialog.open() == 0) {
			/*if (!locations[0].getUrl().startsWith(oldRootUrl)) {
				FindRelatedProjectsOperation scannerOp = new FindRelatedProjectsOperation(locations[0]);
				final RelocateWorkingCopyOperation mainOp = new RelocateWorkingCopyOperation(scannerOp, locations[0]);
				CompositeOperation op = new CompositeOperation(mainOp.getId());
				op.add(scannerOp);
				op.add(mainOp);
				op.add(new AbstractNonLockingOperation("Operation.CheckRelocationState") {
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						if (mainOp.getExecutionState() != IActionOperation.OK) {
							SVNRemoteStorage.instance().copyRepositoryLocation(locations[0], backup);
						}
					}
				});
				op.add(wizard.getOperationToPerform());
				op.add(new RefreshResourcesOperation(mainOp));
				
				this.runNow(op, false); 
			}
			else {
				CompositeOperation op = (CompositeOperation)wizard.getOperationToPerform();
				FindRelatedProjectsOperation findOp = new FindRelatedProjectsOperation(locations[0]);
				op.add(findOp);
				op.add(new RefreshResourcesOperation(findOp, IResource.DEPTH_ZERO, RefreshResourcesOperation.REFRESH_CACHE));
				
				this.runNow(op, false);
			}*/
			
			CompositeOperation op = new CompositeOperation("Operation.EditBambooServer");	
			op.add(new SaveBambooServersOperation());
			op.add(new RefreshBambooServersOperation(false));
			
			this.runNow(op, false);
		}
	}

	public boolean isEnabled() {
		return this.getSelectedBambooServers().length == 1;
	}

}
