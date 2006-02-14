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
import org.eclipse.mylar.internal.jira.JiraFilter;
import org.eclipse.mylar.internal.jira.JiraFilterHit;
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.internal.tasklist.ITask;

/**
 * @author Mik Kersten
 */
public class JiraTaskDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof JiraFilter) {
			decoration.addOverlay(JiraImages.OVERLAY_JIRA, IDecoration.TOP_RIGHT);
		} else if (element instanceof JiraFilterHit) {
			JiraFilterHit hit = (JiraFilterHit)element;
			if (hit.getCorrespondingTask() != null) {
				ITask task = hit.getCorrespondingTask();
//				decoration.addOverlay(JiraImages.OVERLAY_JIRA, IDecoration.TOP_LEFT);
				  
				if (JiraTask.Kind.BUG.toString().equals(task.getKind())) {
					decoration.addOverlay(JiraImages.OVERLAY_BUG, IDecoration.BOTTOM_RIGHT);
				} else if (JiraTask.Kind.FEATURE.toString().equals(task.getKind())) {
					decoration.addOverlay(JiraImages.OVERLAY_FEATURE, IDecoration.BOTTOM_RIGHT);
				} else if (JiraTask.Kind.IMPROVEMENT.toString().equals(task.getKind())) {
					decoration.addOverlay(JiraImages.OVERLAY_IMPROVEMENT, IDecoration.BOTTOM_RIGHT);
				} else if (JiraTask.Kind.TASK.toString().equals(task.getKind())) {
					decoration.addOverlay(JiraImages.OVERLAY_TASK, IDecoration.BOTTOM_RIGHT);
				}
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
