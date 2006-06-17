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
import org.eclipse.swt.widgets.Text;


/**
 * Wizard page for web-based new Jira task wizard
 * 
 * @author Eugene Kuleshov 
 */
public class NewJiraTaskPage extends WizardPage {
	
	public NewJiraTaskPage() {
		super("New Jira task");
	}

	public void createControl(Composite parent) {
		Text text = new Text(parent, SWT.WRAP);
		text.setEditable(false);
		text.setText("This will open a web browser that can be used to create new task.\n" +
				"Once done you can link newly created task back to the Task List.");
		setControl(text);
	}
	
}

