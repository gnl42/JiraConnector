/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.mylar.internal.jira.JiraFilterHit;
import org.eclipse.mylar.internal.jira.JiraTask;

/**
 * @author Mik Kersten
 */
public class JiraTaskDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof JiraFilterHit) {
			JiraFilterHit hit = (JiraFilterHit)element;
			if (hit.getCorrespondingTask() != null) {
				if (JiraTask.Kind.BUG.toString().equals(hit.getOrCreateCorrespondingTask().getKind())) {
//					decoration.addOverlay(JiraImages.OVERLAY_BUG, IDecoration.BOTTOM_RIGHT);
					System.err.println(">>> decorated: " + hit.getDescription());
				}
			}
			decoration.addPrefix("xxxx: ");
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// ignore
	}

	public void dispose() {
		// ignore
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// ignore
	}

}
