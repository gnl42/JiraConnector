/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.model.ServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraConfiguration;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import com.atlassian.connector.eclipse.internal.jira.ui.MigrateToSecureStorageJob;

/**
 * Wizard page used to specify a JIRA repository address, username, and password.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private Button compressionButton;

	private boolean characterEncodingValidated;

	private Button autoRefreshConfigurationButton;

	private Spinner workDaysPerWeekSpinner;

	private Spinner workHoursPerDaySpinner;

	private Spinner maxSearchResultsSpinner;

	private Button linkedTasksAsSubtasksButton;

	private FormToolkit toolkit;

	private JiraConfiguration configuration;

	private Text dateTimePatternText;

	private Text datePatternText;

	private Combo localeCombo;

	private Locale[] locales;

	private Button limitSearchResultsButton;

	private Button followRedirectsButton;

	public JiraRepositorySettingsPage(TaskRepository taskRepository) {
		super(Messages.JiraRepositorySettingsPage_JIRA_Repository_Settings,
				Messages.JiraRepositorySettingsPage_Validate_server_settings, taskRepository);
		setNeedsProxy(true);
		setNeedsHttpAuth(true);
	}

	@Override
	protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		getContainer().updateButtons();
	}

	/** Create a button to validate the specified repository settings */
	@Override
	protected void createAdditionalControls(Composite parent) {
		if (repository != null) {
			configuration = JiraUtil.getConfiguration(repository);
		} else {
			configuration = new JiraConfiguration();
		}

		toolkit = new FormToolkit(parent.getDisplay());

		addRepositoryTemplatesToServerUrlCombo();

		if (repository != null) {
			this.characterEncodingValidated = JiraUtil.getCharacterEncodingValidated(repository);
		}

		Label compressionLabel = new Label(parent, SWT.NONE);
		compressionLabel.setText(Messages.JiraRepositorySettingsPage_Compression);
		compressionButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		compressionButton.setText(Messages.JiraRepositorySettingsPage_Enabled);
		if (repository != null) {
			compressionButton.setSelection(JiraUtil.getCompression(repository));
		}

		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Refresh_configuration);
		autoRefreshConfigurationButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		autoRefreshConfigurationButton.setText(Messages.JiraRepositorySettingsPage_Automatically);
		autoRefreshConfigurationButton.setToolTipText(Messages.JiraRepositorySettingsPage_If_checked_the_repository_configuration_will_be_periodically_updated);
		if (repository != null) {
			autoRefreshConfigurationButton.setSelection(JiraUtil.getAutoRefreshConfiguration(repository));
		}

		label = new Label(parent, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Subtasks);
		linkedTasksAsSubtasksButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		linkedTasksAsSubtasksButton.setText(Messages.JiraRepositorySettingsPage_Show_linked_tasks);
		linkedTasksAsSubtasksButton.setToolTipText(Messages.JiraRepositorySettingsPage_If_checked_linked_tasks_show_as_subtasks_in_the_task_list);
		if (repository != null) {
			linkedTasksAsSubtasksButton.setSelection(JiraUtil.getLinkedTasksAsSubtasks(repository));
		}

		Label followRedirectsLabel = new Label(parent, SWT.NONE);
		followRedirectsLabel.setText(Messages.JiraRepositorySettingsPage_Follow_redirects);
		followRedirectsButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		followRedirectsButton.setText(Messages.JiraRepositorySettingsPage_Enabled);
		if (configuration != null) {
			followRedirectsButton.setSelection(configuration.getFollowRedirects());
		}

		label = new Label(parent, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Time_tracking);

		Composite timeTrackingComposite = new Composite(parent, SWT.NONE);
		timeTrackingComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		workDaysPerWeekSpinner = new Spinner(timeTrackingComposite, SWT.BORDER | SWT.RIGHT);
		workDaysPerWeekSpinner.setValues(JiraConfiguration.DEFAULT_WORK_DAYS_PER_WEEK, 1, 7, 0, 1, 1);
		if (repository != null) {
			workDaysPerWeekSpinner.setSelection(JiraUtil.getWorkDaysPerWeek(repository));
		}
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(workDaysPerWeekSpinner);
		label = new Label(timeTrackingComposite, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_working_days_per_week);

		workHoursPerDaySpinner = new Spinner(timeTrackingComposite, SWT.BORDER);
		workHoursPerDaySpinner.setValues(JiraConfiguration.DEFAULT_WORK_HOURS_PER_DAY, 1, 24, 0, 1, 1);
		if (repository != null) {
			workHoursPerDaySpinner.setSelection(JiraUtil.getWorkHoursPerDay(repository));
		}
		label = new Label(timeTrackingComposite, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_working_hours_per_day);

		label = new Label(parent, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Search_results);

		Composite maxSearchResultsComposite = new Composite(parent, SWT.NONE);
		maxSearchResultsComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		limitSearchResultsButton = new Button(maxSearchResultsComposite, SWT.CHECK | SWT.LEFT);
		limitSearchResultsButton.setText(Messages.JiraRepositorySettingsPage_Limit);

		maxSearchResultsSpinner = new Spinner(maxSearchResultsComposite, SWT.BORDER);
		maxSearchResultsSpinner.setValues(JiraUtil.DEFAULT_MAX_SEARCH_RESULTS, 1, 99999, 0, 1, 1000);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(maxSearchResultsSpinner);
		if (repository != null) {
			int maxSearchResults = JiraUtil.getMaxSearchResults(repository);
			if (maxSearchResults != -1) {
				maxSearchResultsSpinner.setSelection(maxSearchResults);
				limitSearchResultsButton.setSelection(true);
			} else {
				maxSearchResultsSpinner.setSelection(JiraUtil.DEFAULT_MAX_SEARCH_RESULTS);
				limitSearchResultsButton.setSelection(false);
				maxSearchResultsSpinner.setEnabled(false);
			}
		} else {
			maxSearchResultsSpinner.setSelection(JiraUtil.DEFAULT_MAX_SEARCH_RESULTS);
			limitSearchResultsButton.setSelection(true);
		}
		limitSearchResultsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				maxSearchResultsSpinner.setEnabled(limitSearchResultsButton.getSelection());
			}
		});

		createAdvancedComposite(parent);
	}

	private void createAdvancedComposite(final Composite parent) {
		ExpandableComposite expandableComposite = toolkit.createExpandableComposite(parent,
				ExpandableComposite.TITLE_BAR | ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = -5;
		expandableComposite.setLayoutData(gd);
		expandableComposite.setFont(parent.getFont());
		expandableComposite.setBackground(parent.getBackground());
		expandableComposite.setText(Messages.JiraRepositorySettingsPage_Advanced_Configuration);
		expandableComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getControl().getShell().pack();
			}
		});
		toolkit.paintBordersFor(expandableComposite);

		Composite composite = toolkit.createComposite(expandableComposite, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		expandableComposite.setClient(composite);

//		compressionButton = new Button(composite, SWT.CHECK | SWT.LEFT);
//		compressionButton.setText("Customize");
//		if (repository != null) {
//			compressionButton.setSelection(JiraUtil.getCompression(repository));
//		}
//
//		new Label(composite, SWT.NONE);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Date_Picker_Format);

		datePatternText = new Text(composite, SWT.NONE);
		datePatternText.setText(configuration.getDatePattern());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(datePatternText);
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Date_Time_Picker_Format);

		dateTimePatternText = new Text(composite, SWT.NONE);
		dateTimePatternText.setText(configuration.getDateTimePattern());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(dateTimePatternText);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(datePatternText);
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.JiraRepositorySettingsPage_Locale);

		localeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		locales = Locale.getAvailableLocales();
		Arrays.sort(locales, new Comparator<Locale>() {
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		for (Locale locale : locales) {
			localeCombo.add(locale.getDisplayName());
		}
		localeCombo.setText(configuration.getLocale().getDisplayName());

		Hyperlink hyperlink = toolkit.createHyperlink(composite, Messages.JiraRepositorySettingsPage_Reset_to_defaults,
				SWT.NONE);
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				datePatternText.setText(JiraConfiguration.DEFAULT_DATE_PATTERN);
				dateTimePatternText.setText(JiraConfiguration.DEFAULT_DATE_TIME_PATTERN);
				localeCombo.setText(JiraConfiguration.DEFAULT_LOCALE.getDisplayName());
			}
		});

		toolkit.paintBordersFor(composite);
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

	@SuppressWarnings("restriction")
	@Override
	public void applyTo(TaskRepository repository) {
		MigrateToSecureStorageJob.migrateToSecureStorage(repository);

		super.applyTo(repository);
		configuration.setDatePattern(datePatternText.getText());
		configuration.setDateTimePattern(dateTimePatternText.getText());
		if (localeCombo.getSelectionIndex() != -1) {
			configuration.setLocale(locales[localeCombo.getSelectionIndex()]);
		}
		configuration.setFollowRedirects(followRedirectsButton.getSelection());
		JiraUtil.setConfiguration(repository, configuration);
		JiraUtil.setCompression(repository, compressionButton.getSelection());
		JiraUtil.setAutoRefreshConfiguration(repository, autoRefreshConfigurationButton.getSelection());
		JiraUtil.setLinkedTasksAsSubtasks(repository, linkedTasksAsSubtasksButton.getSelection());
		JiraUtil.setWorkDaysPerWeek(repository, workDaysPerWeekSpinner.getSelection());
		JiraUtil.setWorkHoursPerDay(repository, workHoursPerDaySpinner.getSelection());
		if (limitSearchResultsButton.getSelection()) {
			JiraUtil.setMaxSearchResults(repository, maxSearchResultsSpinner.getSelection());
		} else {
			JiraUtil.setMaxSearchResults(repository, -1);
		}
		if (characterEncodingValidated) {
			JiraUtil.setCharacterEncodingValidated(repository, true);
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

				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, IStatus.OK,
						Messages.JiraRepositorySettingsPage_Authentication_credentials_are_valid_character_encodeing,
						null));
			}

			if (serverInfo.isInsecureRedirect()) {
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, IStatus.OK,
						Messages.JiraRepositorySettingsPage_Authentication_credentials_are_valid_server_redirected,
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
			return repository.getRepositoryUrl();
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			try {
				new URL(repository.getRepositoryUrl());
			} catch (MalformedURLException ex) {
				throw new CoreException(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, IStatus.OK,
						INVALID_REPOSITORY_URL, null));
			}

			AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
			JiraConfiguration configuration = JiraUtil.getConfiguration(repository);
			try {
				this.serverInfo = JiraClientFactory.getDefault().validateConnection(location, configuration, monitor);
			} catch (JiraAuthenticationException e) {
				throw new CoreException(RepositoryStatus.createStatus(repository.getRepositoryUrl(), IStatus.ERROR,
						JiraUiPlugin.ID_PLUGIN, INVALID_LOGIN));
			} catch (Exception e) {
				throw new CoreException(JiraCorePlugin.toStatus(repository, e));
			}

			MultiStatus status = new MultiStatus(JiraUiPlugin.ID_PLUGIN, 0, NLS.bind("Validation results for {0}", //$NON-NLS-1$
					repository.getRepositoryLabel()), null);
			status.addAll(serverInfo.getStatistics().getStatus());
			status.add(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, NLS.bind(
					"Web base: {0}", serverInfo.getWebBaseUrl()))); //$NON-NLS-1$
			status.add(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, NLS.bind(
					"Character encoding: {0}", serverInfo.getCharacterEncoding()))); //$NON-NLS-1$
			status.add(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, NLS.bind("Version: {0}", serverInfo.toString()))); //$NON-NLS-1$
			StatusHandler.log(status);
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
			getShell().setText(Messages.JiraRepositorySettingsPage_Select_repository_location);

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
			label.setText(Messages.JiraRepositorySettingsPage_The_repository_location_reported_by_the_server_does_not_match_the_provided_location);

			final List<Button> buttons = new ArrayList<Button>(locations.length);

			if (getSelectedUrl() == null) {
				setSelectedUrl(locations[0]);
			}

			for (int i = 1; i < locations.length; i++) {
				Button button = new Button(composite, SWT.RADIO);
				button.setText(Messages.JiraRepositorySettingsPage_Use_server_location_ + locations[i]);
				button.setData(locations[i]);
				button.setSelection(getSelectedUrl().equals(locations[i]));
				buttons.add(button);
			}

			Button keepLocationButton = new Button(composite, SWT.RADIO);
			keepLocationButton.setText(Messages.JiraRepositorySettingsPage_Keep_current_location_ + locations[0]);
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

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public void dispose() {
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		super.dispose();
	}

}
