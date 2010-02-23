/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.ui.viewers;

import com.atlassian.connector.eclipse.ui.commons.DecoratedResource;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Widget;

public class DecoratedResourceInfoProvider implements ICustomToolTipInfoProvider {

	private static DecoratedResourceInfoProvider instance;

	private DecoratedResourceInfoProvider() {
	}

	public static DecoratedResourceInfoProvider getInstance() {
		if (instance == null) {
			instance = new DecoratedResourceInfoProvider();
		}
		return instance;
	}

	public ICustomToolTipInfo getToolTipInfo(Object hoverObject) {
		if (hoverObject instanceof Widget) {
			Object data = ((Widget) hoverObject).getData();
			if (data != null) {
				if (data instanceof DecoratedResource) {
					return (DecoratedResource) data;
				} else if (data instanceof IResource) {
					return new DecoratedResource((IResource) data);
				} else if (data instanceof IAdaptable) {
					IResource resource = (IResource) ((IAdaptable) data).getAdapter(IResource.class);
					return new DecoratedResource(resource);
				}
			}
		}
		return null;
	}

}
