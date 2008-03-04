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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.ui.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.internal.jira.ui.JiraUtils;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.mylyn.web.core.AbstractWebLocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Wizard page used to specify a JIRA repository address, username, and password.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String TITLE = "JIRA Repository Settings";

	private static final String DESCRIPTION = "Example: http://developer.atlassian.com/jira";

	private Button compressionButton;

	private boolean characterEncodingValidated;

	private Button autoRefreshConfigurationButton;

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

		if (repository != null) {
			this.characterEncodingValidated = JiraUtils.getCharacterEncodingValidated(repository);
		}

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

		Label label = new Label(parent, SWT.NONE);
		label.setText("Automatically refresh attributes:");
		autoRefreshConfigurationButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		autoRefreshConfigurationButton.setToolTipText("If checked Mylyn will periodically update the the repository attributes. Note: This can cause a significant load on the repository if it has many projects.");
		if (repository != null) {
			autoRefreshConfigurationButton.setSelection(JiraUtils.getAutoRefreshConfiguration(repository));
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
		JiraUtils.setAutoRefreshConfiguration(repository, autoRefreshConfigurationButton.getSelection());
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
				Set<String> urls = new LinkedHashSet<String>();
				urls.add(url);
				urls.add(serverInfo.getBaseUrl());
				if (serverInfo.getWebBaseUrl() != null) {
					urls.add(serverInfo.getWebBaseUrl());
				}

				UrlSelectionDialog dialog = new UrlSelectionDialog(getShell(), urls.toArray(new String[0]));
				dialog.setSelectedUrl(serverInfo.getBaseUrl());
				int result = dialog.open();
				if (result == Window.OK) {
					setUrl(dialog.getSelectedUrl());
				}
			}

			if (serverInfo.getCharacterEncoding() != null) {
				setEncoding(serverInfo.getCharacterEncoding());
			} else {
				setEncoding(TaskRepository.DEFAULT_CHARACTER_ENCODING);

				jiraValidator.setStatus(new Status(
						IStatus.WARNING,
						JiraUiPlugin.PLUGIN_ID,
						IStatus.OK,
						"Authentication credentials are valid. Note: The character encoding could not be determined, verify 'Additional Settings'.",
						null));
			}

			if (serverInfo.isInsecureRedirect()) {
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.PLUGIN_ID, IStatus.OK,
						"Authentication credentials are valid. Note: The server redirected to an insecure location.",
						null));
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

			AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
			try {
				this.serverInfo = JiraClientFactory.getDefault().validateConnection(location, monitor);
			} catch (JiraAuthenticationException e) {
				throw new CoreException(RepositoryStatus.createStatus(repository.getUrl(), IStatus.ERROR,
						JiraUiPlugin.PLUGIN_ID, INVALID_LOGIN));
			} catch (Exception e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			}
		}

	}

	private static class UrlSelectionDialog extends Dialog {

		private final String[] locations;

		private String selectedUrl;

		protected UrlSelectionDialog(Shell parentShell, String[] locations) {
			super(parentShell);

			if (locations == null || locations.length < 2) {
				throw new IllegalArgumentException();
			}

			this.locations = locations;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			getShell().setText("Select repository location");

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			applyDialogFont(composite);

			Label label = new Label(composite, SWT.NONE);
			label.setText("The repository location reported by the server does not match the provided location.");

			final List<Button> buttons = new ArrayList<Button>(locations.length);

			if (getSelectedUrl() == null) {
				setSelectedUrl(locations[0]);
			}

			for (int i = 1; i < locations.length; i++) {
				Button button = new Button(composite, SWT.RADIO);
				button.setText("Use server location: " + locations[i]);
				button.setData(locations[i]);
				button.setSelection(getSelectedUrl().equals(locations[i]));
				buttons.add(button);
			}

			Button keepLocationButton = new Button(composite, SWT.RADIO);
			keepLocationButton.setText("Keep current location: " + locations[0]);
			keepLocationButton.setData(locations[0]);
			keepLocationButton.setSelection(getSelectedUrl().equals(locations[0]));
			buttons.add(keepLocationButton);

			SelectionListener listener = new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					Object source = e.getSource();
					if (source instanceof Button && ((Button) source).getSelection()) {
						setSelectedUrl((String) ((Button) source).getData());
					}
				}

			};

			for (Button button : buttons) {
				button.addSelectionListener(listener);
			}

			return composite;
		}

		public void setSelectedUrl(String selectedUrl) {
			this.selectedUrl = selectedUrl;
		}

		public String getSelectedUrl() {
			return selectedUrl;
		}

	}

}
