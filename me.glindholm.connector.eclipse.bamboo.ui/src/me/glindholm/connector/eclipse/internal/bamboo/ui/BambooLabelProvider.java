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

package me.glindholm.connector.eclipse.internal.bamboo.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import me.glindholm.theplugin.commons.bamboo.BambooPlan;

public class BambooLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		// ignore
		return null;
	}

	public String getText(Object element) {
		if (element instanceof BambooPlan) {
			return ((BambooPlan) element).getName() + " (" + ((BambooPlan) element).getKey() + ")";
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// ignore

	}

	public void dispose() {
		// ignore

	}

	public boolean isLabelProperty(Object element, String property) {
		// ignore
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// ignore

	}

}
