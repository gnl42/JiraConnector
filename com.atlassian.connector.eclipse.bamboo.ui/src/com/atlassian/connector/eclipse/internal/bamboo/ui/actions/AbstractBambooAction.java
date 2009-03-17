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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public abstract class AbstractBambooAction extends BaseSelectionListenerAction {

	protected BambooBuild build;

	protected ISelectionProvider selectionProvider;

	public AbstractBambooAction(ISelectionProvider selectionProvider) {
		super(null);
		this.selectionProvider = selectionProvider;
	}

	public AbstractBambooAction(BambooBuild build) {
		super(null);
		this.build = build;
	}

	protected BambooBuild getBuild() {
		if (build != null) {
			return build;
		}
		if (selectionProvider != null) {
			ISelection s = selectionProvider.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					return (BambooBuild) selected;
				}
			}
		}
		return null;
	}
}