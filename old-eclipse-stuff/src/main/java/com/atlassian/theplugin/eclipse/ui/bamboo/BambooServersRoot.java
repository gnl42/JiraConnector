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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;
import com.atlassian.theplugin.eclipse.view.bamboo.IDataTreeNode;
import com.atlassian.theplugin.eclipse.view.bamboo.IParentTreeNode;

/**
 * All repositories node representation 
 * 
 * @author Alexander Gurov
 */
public class BambooServersRoot extends BambooFictiveNode implements ITreeContentProvider, IParentTreeNode, IDataTreeNode {
	protected BambooServerNode []children;
	protected boolean softRefresh;

	public Object getData() {
		return null;
	}
	
	public void refresh() {
		this.children = null;
	}
	
	public void softRefresh() {
		this.softRefresh = true;
	}
	
	public String getLabel(Object o) {
		return null;
	}

	public boolean hasChildren(Object inputElement) {
		return true;
	}
	
	public boolean hasChildren() {
		return true;
	}
	
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}
	
	public Object []getChildren(Object o) {
		if (this.children == null || this.softRefresh) {
			Map<IBambooServer, BambooServerNode> oldLocations = new HashMap<IBambooServer, BambooServerNode>();
			if (this.children != null) {
				for (int i = 0; i < this.children.length; i++) {
					oldLocations.put(this.children[i].getBambooServer(), this.children[i]);
				}
			}
			
			IBambooServer []servers = BambooConfigurationStorage.instance().getBambooServers();
			Arrays.sort(servers, new Comparator<IBambooServer>() {
				public int compare(IBambooServer first, IBambooServer second) {
					return first.getLabel().compareTo(second.getLabel());
				}
			});
			this.children = new BambooServerNode[servers.length];
			for (int i = 0; i < servers.length; i++) {
				this.children[i] = (BambooServerNode)oldLocations.get(servers[i]);
				if (this.children[i] == null) {
					this.children[i] = new BambooServerNode(servers[i]);
				}
			}
		}
		return this.children;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

}
