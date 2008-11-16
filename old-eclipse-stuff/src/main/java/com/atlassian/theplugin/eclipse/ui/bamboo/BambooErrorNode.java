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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Unaccessible node representation 
 * 
 * @author Alexander Gurov
 */
public class BambooErrorNode extends BambooFictiveNode {
	public static final String ERROR_MSG = "BambooErrorNode.Label";
	
	protected IStatus errorStatus;
	
	public BambooErrorNode(IStatus errorStatus) {
		this.errorStatus = errorStatus;
	}
	
	public IStatus getErrorStatus() {
		return this.errorStatus;
	}
	
	public boolean hasChildren() {
		return false;
	}
	
	public Object[] getChildren(Object o) {
		return null;
	}

	public String getLabel(Object o) {
		return Activator.getDefault().getResource(BambooErrorNode.ERROR_MSG);
	}

	public ImageDescriptor getImageDescriptor(Object o) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_ERROR_TSK);
	}

}
