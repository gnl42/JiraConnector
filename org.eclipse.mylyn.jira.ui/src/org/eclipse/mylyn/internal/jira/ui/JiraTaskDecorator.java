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
import org.eclipse.mylar.tasks.ui.AbstractTaskDecorator;

/**
 * @author Mik Kersten
 */
public class JiraTaskDecorator extends AbstractTaskDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof JiraQueryHit) {
			JiraQueryHit hit = (JiraQueryHit) element;
			if (hit.getCorrespondingTask() != null) {
				decorate(hit.getCorrespondingTask(), decoration);
			}
		}
		if (element instanceof JiraTask) {
			JiraTask task = (JiraTask) element;
			if (JiraTask.Kind.BUG.toString().equals(task.getTaskKind())) {
				decoration.addOverlay(JiraImages.OVERLAY_BUG, getQuadrantForKind());
			} else if (JiraTask.Kind.FEATURE.toString().equals(task.getTaskKind())) {
				decoration.addOverlay(JiraImages.OVERLAY_FEATURE, getQuadrantForKind());
			} else if (JiraTask.Kind.IMPROVEMENT.toString().equals(task.getTaskKind())) {
				decoration.addOverlay(JiraImages.OVERLAY_IMPROVEMENT, getQuadrantForKind());
			} else if (JiraTask.Kind.TASK.toString().equals(task.getTaskKind())) {
				decoration.addOverlay(JiraImages.OVERLAY_TASK, getQuadrantForKind());
			}
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
