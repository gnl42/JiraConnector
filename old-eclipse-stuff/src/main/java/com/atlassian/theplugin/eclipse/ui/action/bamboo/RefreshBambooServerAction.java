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

package com.atlassian.theplugin.eclipse.ui.action.bamboo;

import org.eclipse.jface.action.IAction;

import com.atlassian.theplugin.eclipse.core.operation.bamboo.RefreshBambooServersOperation;

/**
 * Refresh repository location in the repository tree action
 * 
 * @author Alexander Gurov
 */
public class RefreshBambooServerAction extends AbstractBambooServerAction {

	public RefreshBambooServerAction() {
		super();
	}
	
	public void runImpl(IAction action) {
		this.runBusy(this.getSelection().isEmpty() 
				? new RefreshBambooServersOperation(true) : new RefreshBambooServersOperation(this.getSelectedBambooTreeNodes(), true));
	}

	public boolean isEnabled() {
		return true;
	}

}
