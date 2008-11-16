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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.ui.action.AbstractAction;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooServerNode;
import com.atlassian.theplugin.eclipse.ui.bamboo.IBambooTreeNode;

public abstract class AbstractBambooServerAction extends AbstractAction {
	private IStructuredSelection selection;
	
	public AbstractBambooServerAction() {
		super();
	}

	protected IStructuredSelection getSelection() {
		if (this.selection == null) {
			this.selection = StructuredSelection.EMPTY;
		}
		return this.selection;
	}
	
	protected void checkSelection(IStructuredSelection selectedElement) {
		this.selection = (IStructuredSelection) selectedElement;
	}
	
	protected IBambooServer []getSelectedBambooServers() {
		Object []locationWrappers = this.getSelectedResources(BambooServerNode.class);
		IBambooServer []locations = new IBambooServer[locationWrappers.length];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = ((BambooServerNode) locationWrappers[i]).getBambooServer();
		}
		return locations;
	}
	
	protected IBambooTreeNode []getSelectedBambooTreeNodes() {
		return (IBambooTreeNode []) this.getSelectedResources(IBambooTreeNode.class);
	}
}
