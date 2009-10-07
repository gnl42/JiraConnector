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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @author Wojciech Seliga
 */
public abstract class EclipseBambooBuildSelectionListenerAction extends BaseSelectionListenerAction {

	public EclipseBambooBuildSelectionListenerAction(String text) {
		super(text);
	}

	@Nullable
	protected EclipseBambooBuild getEclipseBambooBuild() {
		ISelection s = this.getStructuredSelection();
		if (s instanceof IStructuredSelection) {
			final Object selected = ((IStructuredSelection) s).getFirstElement();
			if (selected instanceof EclipseBambooBuild) {
				return (EclipseBambooBuild) selected;
			}
		}
		return null;

	}

	@Override
	public void run() {
		final EclipseBambooBuild eclipseBambooBuild = getEclipseBambooBuild();
		if (eclipseBambooBuild != null) {
			onRun(eclipseBambooBuild);

		}
	}

	protected boolean isValidBuild(IStructuredSelection selection) {
		if (selection.size() == 1) {
			BambooBuild build = ((EclipseBambooBuild) selection.getFirstElement()).getBuild();
			return build.isValid();
		}
		return false;
	}

	abstract void onRun(EclipseBambooBuild eclipseBambooBuild);

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() != 1) {
			return false;
		}
		final Object element = selection.getFirstElement();
		if (element instanceof EclipseBambooBuild) {
			EclipseBambooBuild eclipseBambooBuild = (EclipseBambooBuild) element;
			if (eclipseBambooBuild.getBuild().isValid()) {
				return onUpdateSelection(eclipseBambooBuild);
			}
		}
		return false;
	}

	abstract boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild);
}