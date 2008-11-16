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

package com.atlassian.theplugin.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;

/**
 * Referesh in progress node implementation
 * 
 * @author Alexander Gurov
 */
public class RefreshPending implements IWorkbenchAdapter, IWorkbenchAdapter2, IAdaptable {
	public static final String PENDING = "RefreshPending.Label";
	
	protected IWorkbenchAdapter2 parent;
	
	public RefreshPending(IWorkbenchAdapter2 parent) {
		this.parent = parent;
	}
	
    public RGB getForeground(Object element) {
    	return this.parent.getForeground(element);
    }
    
	public boolean hasChildren() {
		return false;
	}
	
	public Object[] getChildren(Object o) {
		return null;
	}

	public String getLabel(Object o) {
		return Activator.getDefault().getResource(RefreshPending.PENDING);
	}
	
	public ImageDescriptor getImageDescriptor(Object object) {
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_REFRESH_PENDING));
	}

	public Object getParent(Object o) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class) || adapter.equals(IWorkbenchAdapter2.class)) {
			return this;
		}
		return null;
	}
	
    public RGB getBackground(Object element) {
    	// do not change default background color
    	return null;
    }
    
    public FontData getFont(Object element) {
    	// do not change default font
    	return null;
    }

}
