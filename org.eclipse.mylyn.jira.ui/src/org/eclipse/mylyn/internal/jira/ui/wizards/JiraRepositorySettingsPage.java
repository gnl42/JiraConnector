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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylar.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylar.internal.jira.ui.JiraRepositoryConnector;
import org.eclipse.mylar.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylar.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylar.tasks.core.RepositoryTemplate;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylar.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page used to specify a JIRA repository address, username, and
 * password.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String TITLE = "Jira Repository Settings";

	private static final String DESCRIPTION = "Example: http://developer.atlassian.com/jira";

	private Button compressionButton;

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

		Label compressionLabel = new Label(parent, SWT.NONE);
		compressionLabel.setText("Compression:");
		compressionButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		if (repository != null) {
			boolean shortLogin = Boolean.parseBoolean(repository.getProperty(JiraRepositoryConnector.COMPRESSION_KEY));
			compressionButton.setSelection(shortLogin);
		}

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
	protected Validator getValidator(TaskRepository repository) {
		return new JiraValidator(repository);
	}

	@Override
	public void updateProperties(TaskRepository repository) {
		repository.setProperty(JiraRepositoryConnector.COMPRESSION_KEY, String
				.valueOf(compressionButton.getSelection()));
	}

	private class JiraValidator extends Validator {

		final TaskRepository repository;

		public JiraValidator(TaskRepository repository) {
			this.repository = repository;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			try {
				new URL(repository.getUrl());
			} catch (MalformedURLException ex) {
				throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
						INVALID_REPOSITORY_URL, null));
			}

			try {
				JiraClientFacade.getDefault().validateServerAndCredentials(repository.getUrl(),
						repository.getUserName(), repository.getPassword(), repository.getProxy(),
						repository.getHttpUser(), repository.getHttpPassword());
			} catch (Exception e) {
				throw new CoreException(JiraCorePlugin.toStatus(e));
			}

			setStatus(new Status(IStatus.OK, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
					"Valid JIRA server found and your login was accepted", null));
		}

	}

}
