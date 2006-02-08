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

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.mylar.internal.tasklist.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Mik Kersten
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String TITLE = "JIRA Repository Settings";

	private static final String DESCRIPTION = "Example: http://developer.atlassian.com/jira/secure/Dashboard.jspa";

	public JiraRepositorySettingsPage() {
		super(TITLE, DESCRIPTION);
	}

	protected void createAdditionalControls(Composite parent) {
		// ignore
	}

	@Override 
	public boolean isPageComplete() {
		return super.isPageComplete();
	}
}
