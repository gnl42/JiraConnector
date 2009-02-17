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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Decorating the build viewer labels
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooViewLabelDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		// ignore for now
//		if (element instanceof BambooBuild) {
//			BambooBuild build = (BambooBuild) element;
//			if (build.getErrorMessage() != null) {
//				decoration.addOverlay(CommonImages.OVERLAY_SYNC_WARNING, IDecoration.BOTTOM_RIGHT);
//			}
//		}
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
