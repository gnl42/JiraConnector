/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * Wizard page for web-based new Jira task wizard
 * 
 * @author Eugene Kuleshov 
 * @author Mik Kersten
 */
public class NewJiraTaskPage extends WizardPage {
	
	public NewJiraTaskPage() {
		super("New Jira task");
		setTitle("Create via Web Browser");
		setDescription("Once submitted synchronize queries or add the task to a category.\n"
				+ "Note: you may need to log in via the Web UI.");
	}

	public void createControl(Composite parent) {
		Label label = new Label(parent, SWT.NULL);
		setControl(label);
//		Text text = new Text(parent, SWT.WRAP);
//		text.setEditable(false);
//		text.setText("\nThis will open a web browser that can be used to create new task.\n" +
//				"Once submitted you can refresh a corresponding query or add the task to a category.");
//		setControl(text);
//		parent.setFocus();
	}
	
}

