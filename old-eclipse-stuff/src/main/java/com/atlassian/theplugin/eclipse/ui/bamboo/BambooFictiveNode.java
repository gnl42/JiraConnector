package com.atlassian.theplugin.eclipse.ui.bamboo;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public abstract class BambooFictiveNode implements IWorkbenchAdapter, IWorkbenchAdapter2, IAdaptable {

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
    
    public RGB getForeground(Object element) {
    	return null;
    }
    
    public FontData getFont(Object element) {
    	// do not change default font
    	return null;
    }

}
