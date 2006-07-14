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

package org.eclipse.mylar.internal.jira.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.mylar.internal.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnector;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page used to specify a Jira repository address, username, and
 * password.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String MESSAGE_FAILURE_CONNECT = "Could not connect to the Jira server or the login was not accepted.";

	private static final String TITLE = "Jira Repository Settings";

	private static final String DESCRIPTION = "Example: http://developer.atlassian.com/jira";

	public JiraRepositorySettingsPage(AbstractRepositoryConnector connector) {
		super(TITLE, DESCRIPTION, connector);
	}

	/** Create a button to validate the specified repository settings */
	protected void createAdditionalControls(Composite parent) {
		// ignore
	}
	
	protected boolean isValidUrl(String name) {
		if (name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
			}
		}
		return false;
	}

	protected void validateSettings() {
//		if (JiraServerFacade.getDefault().validateServerAndCredentials(JiraRepositorySettingsPage.super.serverUrlEditor.getStringValue(),
//				getUserName(), getPassword())) {
//			MessageDialog.openInformation(null, MylarJiraPlugin.TITLE_MESSAGE_DIALOG,
//					"Valid Jira server found and your login was accepted.");
//		} else {
//			MessageDialog.openInformation(null, MylarJiraPlugin.TITLE_MESSAGE_DIALOG, MESSAGE_FAILURE_CONNECT);
//		}
		final String serverUrl = super.serverUrlEditor.getStringValue();
		final String userName = getUserName();
		final String password = getPassword();
		try {
			getWizard().getContainer().run(true, false, new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Validating repository settings", IProgressMonitor.UNKNOWN);
						String message = JiraServerFacade.getDefault().validateServerAndCredentials(serverUrl, userName, password);
						if (message != null) {
							throw new InvocationTargetException(new RuntimeException(message));
						}
					} finally {
						monitor.done();
					}
				}
			});
			MessageDialog.openInformation(null, MylarJiraPlugin.TITLE_MESSAGE_DIALOG,
				"Valid Jira server found and your login was accepted.");
		} catch (InvocationTargetException e) {
			MessageDialog.openError(null, MylarJiraPlugin.TITLE_MESSAGE_DIALOG, e.getTargetException().getMessage());
		} catch (InterruptedException e) {
			MessageDialog.openError(null, MylarJiraPlugin.TITLE_MESSAGE_DIALOG, MESSAGE_FAILURE_CONNECT);
		}
	}
}
