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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylar.internal.jira.ui.JiraServerFacade;
import org.eclipse.mylar.tasks.core.RepositoryTemplate;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylar.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page used to specify a Jira repository address, username, and
 * password.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Eugene Kuleshov
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String MESSAGE_FAILURE_CONNECT = "Could not connect to the Jira server or the login was not accepted";

	private static final String TITLE = "Jira Repository Settings";

	private static final String DESCRIPTION = "Example: http://developer.atlassian.com/jira";

	public JiraRepositorySettingsPage(AbstractRepositoryConnectorUi repositoryUi) {
		super(TITLE, DESCRIPTION, repositoryUi);
		setNeedsProxy(true);
		setNeedsHttpAuth(true);
	}

	/** Create a button to validate the specified repository settings */
	@Override
	protected void createAdditionalControls(Composite parent) {
		for (RepositoryTemplate template : connector.getTemplates()) {
			serverUrlCombo.add(template.label);
		}

		serverUrlCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String text = serverUrlCombo.getText();
				RepositoryTemplate template = connector.getTemplate(text);
				if (template != null) {
					repositoryLabelEditor.setStringValue(template.label);
					setUrl(nvl(template.repositoryUrl));
					getContainer().updateButtons();
				}
			}

			private String nvl(String s) {
				return s == null ? "" : s;
			}
		});
	}

	@Override
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

	@Override
	protected void validateSettings() {
		final String serverUrl = getServerUrl();
		final String userName = getUserName();
		final String password = getPassword();
		try {
			getWizard().getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						new URL(serverUrl);
					} catch (MalformedURLException ex) {
						throw new InvocationTargetException(new RuntimeException("Malformed server URL"));
					}
					monitor.beginTask("Validating repository settings", IProgressMonitor.UNKNOWN);
					try {
						String message = JiraServerFacade.getDefault().validateServerAndCredentials(serverUrl,
								userName, password);
						if (message != null) {
							throw new InvocationTargetException(new RuntimeException(message));
						}
					} finally {
						monitor.done();
					}
				}
			});
			setErrorMessage(null);
			setMessage("Valid Jira server found and your login was accepted", IMessageProvider.INFORMATION);
		} catch (InvocationTargetException e) {
			// MessageDialog.openError(null, JiraUiPlugin.TITLE_MESSAGE_DIALOG, e.getTargetException().getMessage());
			setErrorMessage(e.getTargetException().getMessage());
			setMessage(null);
		} catch (InterruptedException e) {
			// MessageDialog.openError(null, JiraUiPlugin.TITLE_MESSAGE_DIALOG, MESSAGE_FAILURE_CONNECT);
			setErrorMessage(MESSAGE_FAILURE_CONNECT);
			setMessage(null);
		}
	}
}
