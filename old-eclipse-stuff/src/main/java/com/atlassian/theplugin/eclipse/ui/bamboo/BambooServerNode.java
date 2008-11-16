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

package com.atlassian.theplugin.eclipse.ui.bamboo;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.IDataTreeNode;
import com.atlassian.theplugin.eclipse.view.bamboo.IParentTreeNode;

/**
 * Repository location node representation 
 * 
 * @author Alexander Gurov
 */
public class BambooServerNode extends BambooFictiveNode implements IBambooTreeNode, IParentTreeNode, IDataTreeNode {
	protected IBambooServer server;
	protected Object []children;
	protected BambooTreeViewer bambooServerTree;
	//protected RepositoryFolder locationRoot;
	
	public BambooServerNode(IBambooServer server) {
		this.server = server;
		this.refresh();
	}

    public void setViewer(BambooTreeViewer bambooServerTree) {
    	this.bambooServerTree = bambooServerTree;
    }
    
    protected RefreshOperation getRefreshOperation(BambooTreeViewer viewer) {
		return new RefreshOperation(viewer);
	}
    
	public void refresh() {
		this.children = null;
	}
	
	public IBambooServer getBambooServer() {
		return this.server;
	}
	
	public Object getData() {
		return this.server;
	}
	
	public String getLabel(Object o) {
		return this.server.getLabel();
	}

	public boolean hasChildren() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public Object []getChildren(Object o) {
		if (this.children == null) {
			ArrayList list = new ArrayList(/*Arrays.asList(this.locationRoot.getChildren(o))*/);
			list.add(new BambooPlans(this));
			//list.add(new RepositoryRevisions(this.server));
			/*if (list.get(0) instanceof RepositoryPending) {
				return list.toArray(); 
			}*/
			this.children = list.toArray();
	    }
	    return this.children;
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO));
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof BambooServerNode) {
			return ((BambooServerNode)obj).server.equals(this.server);
		}
		return super.equals(obj);
	}

	protected class RefreshOperation extends AbstractNonLockingOperation {
		protected BambooTreeViewer viewer;
		
		public RefreshOperation(BambooTreeViewer viewer) {
			super("Operation.RefreshView");
			this.viewer = viewer;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			// TODO rework this using cancellation manager in order to make it thread-safe...
			if (this.viewer != null && !this.viewer.getControl().isDisposed()) {
				this.viewer.refresh(BambooServerNode.this, null, true);
			}
		}
	}
}
