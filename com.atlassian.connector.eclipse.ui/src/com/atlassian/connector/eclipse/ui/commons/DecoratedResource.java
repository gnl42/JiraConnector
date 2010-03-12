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

package com.atlassian.connector.eclipse.ui.commons;

import com.atlassian.connector.eclipse.ui.viewers.ICustomToolTipInfo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class DecoratedResource implements ICustomToolTipInfo {

	private final String decorationText;

	private final boolean upToDate;

	private final IResource resource;

	private final String tooltipText;

	private final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

	public DecoratedResource(IResource resource, boolean upToDate, String decorationText, String tooltipText) {
		this.resource = resource;
		this.upToDate = upToDate;
		this.decorationText = decorationText;
		this.tooltipText = tooltipText;
	}

	public DecoratedResource(IResource parent) {
		// treat the resource as up-to-date (may need to be changed later)
		this(parent, true, "", "");
	}

	public IResource getResource() {
		return resource;
	}

	public String getDecorationText() {
		return decorationText;
	}

	public boolean isUpToDate() {
		return upToDate;
	}

	public String getTooltipText() {
		return tooltipText;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DecoratedResource other = (DecoratedResource) obj;
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		return true;
	}

	public Image getImage() {
		return workbenchLabelProvider.getImage(resource);
	}

	public void createToolTipArea(CustomToolTip tooltip, Composite composite) {
		tooltip.addIconAndLabel(composite, getImage(), getResource().getName(), true);

		String detailsText = getTooltipText();
		if (detailsText != null) {
			tooltip.addIconAndLabel(composite, null, detailsText);
		}
	}

	public boolean isContainer() {
		return resource instanceof IContainer;
	}

}