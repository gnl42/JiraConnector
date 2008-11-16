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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ui.actions.TeamAction;

import com.atlassian.theplugin.eclipse.ui.wizard.bamboo.NewBambooServerWizard;

/**
 * New repository location action implementation
 * 
 * @author Alexander Gurov
 */
@SuppressWarnings("restriction")
public class NewBambooServerAction extends TeamAction {

	public NewBambooServerAction() {
		super();
	}
	
	public void run(IAction action) {
		NewBambooServerWizard wizard = new NewBambooServerWizard();
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		dialog.open();
	}

	public boolean isEnabled() {
		return true;
	}

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// compatibility with 3.3
	}

}
