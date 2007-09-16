/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFacade;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.jira.ui.JiraUtils;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page used to specify a JIRA repository address, username, and password.
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

	private boolean characterEncodingValidated;

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

		this.characterEncodingValidated = JiraUtils.getCharacterEncodingValidated(repository);
		
		serverUrlCombo.addSelectionListener(new SelectionAdapter() {
			@Override
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
			compressionButton.setSelection(JiraUtils.getCompression(repository));
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
		JiraUtils.setCompression(repository, compressionButton.getSelection());
		if (characterEncodingValidated) {
			JiraUtils.setCharacterEncodingValidated(repository, true);
		}
	}

	@Override
	protected void applyValidatorResult(Validator validator) {
		JiraValidator jiraValidator = (JiraValidator) validator;
		ServerInfo serverInfo = jiraValidator.getServerInfo();
		if (serverInfo != null) {
			String url = jiraValidator.getRepositoryUrl();
			if (serverInfo.getBaseUrl() != null && !url.equals(serverInfo.getBaseUrl())) {
				// TODO prompt user
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
						"Authentication credentials are valid. Note: The server reported a different location.", null));
				setUrl(serverInfo.getBaseUrl());
			}
			
			if (serverInfo.getCharacterEncoding() != null) {
				setEncoding(serverInfo.getCharacterEncoding());
			} else {
				setEncoding(TaskRepository.DEFAULT_CHARACTER_ENCODING);
				
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
						"Authentication credentials are valid. Note: The character encoding could not be determined, verify 'Additional Settings'.", null));
			}
			
			if (serverInfo.isInsecureRedirect()) {
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
						"Authentication credentials are valid. Note: The server redirected to an insecure location.", null));				
			}
			
			characterEncodingValidated = true;			
		}
		
		super.applyValidatorResult(validator);

	}

	private class JiraValidator extends Validator {

		final TaskRepository repository;
		
		private ServerInfo serverInfo;

		public JiraValidator(TaskRepository repository) {
			this.repository = repository;
		}

		public ServerInfo getServerInfo() {
			return serverInfo;
		}
		
		public String getRepositoryUrl() {
			return repository.getUrl();
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
				this.serverInfo = JiraClientFacade.getDefault().validateConnection(repository.getUrl(),
						repository.getUserName(), repository.getPassword(), repository.getProxy(),
						repository.getHttpUser(), repository.getHttpPassword());
			} catch (JiraAuthenticationException e) {
				throw new CoreException(RepositoryStatus.createStatus(repository.getUrl(), IStatus.ERROR,
						JiraUiPlugin.PLUGIN_ID, INVALID_LOGIN));
			} catch (Exception e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			}
		}

	}

}
