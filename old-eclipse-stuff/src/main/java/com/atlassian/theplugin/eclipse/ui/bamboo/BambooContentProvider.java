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

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.atlassian.theplugin.eclipse.view.bamboo.IParentTreeNode;

/**
 * Repository content provider 
 * 
 * @author Alexander Gurov
 */
public class BambooContentProvider extends WorkbenchContentProvider {
	protected BambooTreeViewer bambooTree;
	protected IBambooContentFilter filter;

	public BambooContentProvider(BambooTreeViewer bambooTree) {
		this.bambooTree = bambooTree;
	}
	
	public IBambooContentFilter getFilter() {
		return this.filter;
	}
	
	public void setFilter(IBambooContentFilter filter) {
		this.filter = filter;
	}

	public boolean hasChildren(Object element) {
		IWorkbenchAdapter adapter = this.getAdapter(element);
		if (adapter instanceof IParentTreeNode) {
			return ((IParentTreeNode)adapter).hasChildren();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Object []getChildren(Object parentElement) {
		IWorkbenchAdapter adapter = this.getAdapter(parentElement);
		if (adapter instanceof IParentTreeNode) {
			if (adapter instanceof IBambooTreeNode) {
				((IBambooTreeNode)adapter).setViewer(this.bambooTree);
			}
			ArrayList filtered = new ArrayList();
			Object []children = adapter.getChildren(parentElement);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (this.filter == null || this.filter.accept(children[i])) {
						if (children[i] instanceof IBambooTreeNode) {
							((IBambooTreeNode)children[i]).setViewer(this.bambooTree);
						}
						filtered.add(children[i]);
					}
				}
			}
			return filtered.toArray();
		}
		return new Object[0];
	}
	
}
