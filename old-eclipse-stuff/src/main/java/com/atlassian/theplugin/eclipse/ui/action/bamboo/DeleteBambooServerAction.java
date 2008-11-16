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

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.CompositeOperation;
import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.DeleteBambooServerOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.RefreshBambooServersOperation;
import com.atlassian.theplugin.eclipse.core.operation.bamboo.SaveBambooServersOperation;

/**
 * Discard location action
 * 
 * @author Alexander Gurov
 */
public class DeleteBambooServerAction extends AbstractBambooServerAction {

	public DeleteBambooServerAction() {
		super();
	}

	public void runImpl(IAction action) {
		IBambooServer[] servers = this.getSelectedBambooServers();
		doDiscard(servers, new CompositeOperation(""));
		/*
		 * List selection = Arrays.asList(locations); List operateLocations =
		 * new ArrayList(); operateLocations.addAll(Arrays.asList(locations));
		 * ArrayList connectedProjects = new ArrayList(); HashSet
		 * connectedLocations = new HashSet(); IProject []projects =
		 * ResourcesPlugin.getWorkspace().getRoot().getProjects(); for (int i =
		 * 0; i < projects.length; i++) { RepositoryProvider tmp =
		 * RepositoryProvider.getProvider(projects[i]); if (tmp != null &&
		 * Activator.PLUGIN_ID.equals(tmp.getID())) { SVNTeamProvider provider =
		 * (SVNTeamProvider)tmp; if
		 * (selection.contains(provider.getRepositoryLocation())) {
		 * connectedProjects.add(projects[i]);
		 * connectedLocations.add(provider.getRepositoryLocation());
		 * operateLocations.remove(provider.getRepositoryLocation()); } } }
		 * 
		 * if (operateLocations.size() > 0) { locations = (IBambooServer
		 * [])operateLocations.toArray(new
		 * IBambooServer[operateLocations.size()]); DiscardConfirmationDialog
		 * dialog = new DiscardConfirmationDialog(this.getShell(),
		 * locations.length == 1, DiscardConfirmationDialog.MSG_LOCATION); if
		 * (dialog.open() == 0) { this.doDiscard(locations, null); } } if
		 * (connectedProjects.size() > 0) { ArrayList locationsList = new
		 * ArrayList(); for (Iterator iter = connectedLocations.iterator();
		 * iter.hasNext();) { IBambooServer location =
		 * (IBambooServer)iter.next(); locationsList.add(location.getLabel()); }
		 * IProject []tmp = (IProject [])connectedProjects.toArray(new
		 * IProject[connectedProjects.size()]); DiscardLocationFailurePanel
		 * panel = new DiscardLocationFailurePanel((String
		 * [])locationsList.toArray(new String[locationsList.size()]), tmp); int
		 * retVal = new DefaultDialog(this.getShell(), panel).open(); if (retVal
		 * == 0 || retVal == 1) { DisconnectOperation disconnectOp = new
		 * DisconnectOperation(tmp, false); CompositeOperation op = new
		 * CompositeOperation(disconnectOp.getId()); op.add(new
		 * NotifyProjectStatesChangedOperation(tmp,
		 * ProjectStatesChangedEvent.ST_PRE_DISCONNECTED));
		 * op.add(disconnectOp);
		 * 
		 * if (retVal == 0) { op.add(new RefreshResourcesOperation(tmp,
		 * IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_ALL)); }
		 * else { op.add(new NotifyProjectStatesChangedOperation(tmp,
		 * ProjectStatesChangedEvent.ST_PRE_DELETED)); op.add(new
		 * AbstractWorkingCopyOperation("Operation.DeleteProjects", tmp) {
		 * protected void runImpl(IProgressMonitor monitor) throws Exception {
		 * IProject []projects = (IProject [])this.operableData(); for (int i =
		 * 0; i < projects.length && !monitor.isCanceled(); i++) { final
		 * IProject current = projects[i]; this.protectStep(new
		 * IUnprotectedOperation() { public void run(IProgressMonitor monitor)
		 * throws Exception { current.delete(true, monitor); } }, monitor,
		 * projects.length); } } }); } this.doDiscard(locations, op); } }
		 */
	}

	protected void doDiscard(IBambooServer[] servers,
			IActionOperation disconnectOp) {
		DeleteBambooServerOperation mainOp = new DeleteBambooServerOperation(
				servers);

		CompositeOperation op = new CompositeOperation(mainOp.getId());

		if (disconnectOp != null) {
			op.add(disconnectOp);
			op.add(mainOp, new IActionOperation[] { disconnectOp });
		} else {
			op.add(mainOp);
		}
		op.add(new SaveBambooServersOperation());
		op.add(new RefreshBambooServersOperation(false));

		this.runNow(op, false);
	}

	public boolean isEnabled() {
		return this.getSelectedBambooServers().length > 0;
	}

}
