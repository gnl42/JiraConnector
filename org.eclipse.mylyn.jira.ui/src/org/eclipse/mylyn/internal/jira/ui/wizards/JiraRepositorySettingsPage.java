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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraTimeFormat;
import org.eclipse.mylyn.internal.jira.core.model.JiraConfiguration;
import org.eclipse.mylyn.internal.jira.core.model.ServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraAuthenticationException;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.jira.ui.JiraUiPlugin;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
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

	private Button useResolutionButton;

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

	public JiraRepositorySettingsPage(TaskRepository taskRepository) {
		super(TITLE, DESCRIPTION, taskRepository);
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
		compressionLabel.setText("Compression:");
		compressionButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		compressionButton.setText("Enabled");
		if (repository != null) {
			compressionButton.setSelection(JiraUtil.getCompression(repository));
		}

		Label label = new Label(parent, SWT.NONE);
		label.setText("Refresh configuration:");
		autoRefreshConfigurationButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		autoRefreshConfigurationButton.setText("Automatically");
		autoRefreshConfigurationButton.setToolTipText("If checked the repository configuration will be periodically updated. Note: This can cause a significant load on the repository if it has many projects.");
		if (repository != null) {
			autoRefreshConfigurationButton.setSelection(JiraUtil.getAutoRefreshConfiguration(repository));
		}

		label = new Label(parent, SWT.NONE);
		label.setText("Completed tasks:");
		useResolutionButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		useResolutionButton.setText("Based on resolution");
		useResolutionButton.setToolTipText("If checked an issue is considered completed if it has a resolution. Otherwise detection is based on the status of the issue.");
		if (repository != null) {
			useResolutionButton.setSelection(JiraUtil.getCompletedBasedOnResolution(repository));
		}

		label = new Label(parent, SWT.NONE);
		label.setText("Subtasks:");
		linkedTasksAsSubtasksButton = new Button(parent, SWT.CHECK | SWT.LEFT);
		linkedTasksAsSubtasksButton.setText("Show linked tasks");
		linkedTasksAsSubtasksButton.setToolTipText("If checked linked tasks show as subtasks in the task list.");
		if (repository != null) {
			linkedTasksAsSubtasksButton.setSelection(JiraUtil.getLinkedTasksAsSubtasks(repository));
		}

		label = new Label(parent, SWT.NONE);
		label.setText("Time tracking:");

		Composite timeTrackingComposite = new Composite(parent, SWT.NONE);
		timeTrackingComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		workDaysPerWeekSpinner = new Spinner(timeTrackingComposite, SWT.BORDER | SWT.RIGHT);
		workDaysPerWeekSpinner.setValues(JiraTimeFormat.DEFAULT_WORK_DAYS_PER_WEEK, 1, 7, 0, 1, 1);
		if (repository != null) {
			workDaysPerWeekSpinner.setSelection(JiraUtil.getWorkDaysPerWeek(repository));
		}
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(workDaysPerWeekSpinner);
		label = new Label(timeTrackingComposite, SWT.NONE);
		label.setText("working days per week");

		workHoursPerDaySpinner = new Spinner(timeTrackingComposite, SWT.BORDER);
		workHoursPerDaySpinner.setValues(JiraTimeFormat.DEFAULT_WORK_HOURS_PER_DAY, 1, 24, 0, 1, 1);
		if (repository != null) {
			workHoursPerDaySpinner.setSelection(JiraUtil.getWorkHoursPerDay(repository));
		}
		label = new Label(timeTrackingComposite, SWT.NONE);
		label.setText("working hours per day");

		label = new Label(parent, SWT.NONE);
		label.setText("Search results:");

		Composite maxSearchResultsComposite = new Composite(parent, SWT.NONE);
		maxSearchResultsComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		limitSearchResultsButton = new Button(maxSearchResultsComposite, SWT.CHECK | SWT.LEFT);
		limitSearchResultsButton.setText("Limit");

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
		expandableComposite.setText("Advanced &Configuration");
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
		label.setText("Date Picker Format:");

		datePatternText = new Text(composite, SWT.NONE);
		datePatternText.setText(configuration.getDatePattern());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(datePatternText);
		label = new Label(composite, SWT.NONE);
		label.setText("Date Time Picker Format:");

		dateTimePatternText = new Text(composite, SWT.NONE);
		dateTimePatternText.setText(configuration.getDateTimePattern());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(dateTimePatternText);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(datePatternText);
		label = new Label(composite, SWT.NONE);
		label.setText("Locale:");

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

		Hyperlink hyperlink = toolkit.createHyperlink(composite, "Reset to defaults", SWT.NONE);
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

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		configuration.setDatePattern(datePatternText.getText());
		configuration.setDateTimePattern(dateTimePatternText.getText());
		if (localeCombo.getSelectionIndex() != -1) {
			configuration.setLocale(locales[localeCombo.getSelectionIndex()]);
		}
		JiraUtil.setConfiguration(repository, configuration);
		JiraUtil.setCompression(repository, compressionButton.getSelection());
		JiraUtil.setAutoRefreshConfiguration(repository, autoRefreshConfigurationButton.getSelection());
		JiraUtil.setCompletedBasedOnResolution(repository, useResolutionButton.getSelection());
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

				jiraValidator.setStatus(new Status(
						IStatus.WARNING,
						JiraUiPlugin.ID_PLUGIN,
						IStatus.OK,
						"Authentication credentials are valid. Note: The character encoding could not be determined, verify 'Additional Settings'.",
						null));
			}

			if (serverInfo.isInsecureRedirect()) {
				jiraValidator.setStatus(new Status(IStatus.WARNING, JiraUiPlugin.ID_PLUGIN, IStatus.OK,
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
			try {
				this.serverInfo = JiraClientFactory.getDefault().validateConnection(location, monitor);
			} catch (JiraAuthenticationException e) {
				throw new CoreException(RepositoryStatus.createStatus(repository.getRepositoryUrl(), IStatus.ERROR,
						JiraUiPlugin.ID_PLUGIN, INVALID_LOGIN));
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
