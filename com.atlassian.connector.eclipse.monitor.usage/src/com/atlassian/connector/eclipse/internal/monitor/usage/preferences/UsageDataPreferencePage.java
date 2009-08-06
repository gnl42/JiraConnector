/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Ken Sueda - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassian.connector.eclipse.internal.monitor.usage.InteractionEventObfuscator;
import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorPreferenceConstants;
import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;
import com.atlassian.connector.eclipse.internal.monitor.usage.UsageCollector;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class UsageDataPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = Messages.UsageDataPreferencePage_description;

	private static final long DAYS_IN_MS = 1000 * 60 * 60 * 24;

	private Button enableMonitoring;

	private Button enableObfuscation;

	private Button enableSubmission;

	private Text logFileText;

	private Text submissionTime;

	public UsageDataPreferencePage() {
		super();
		setPreferenceStore(UiUsageMonitorPlugin.getPrefs());
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		createLogFileSection(container);
		createUsageSection(container);
		createCollectorsSection(container);
		updateEnablement();
		return container;
	}

	private void createCollectorsSection(Composite parent) {
		final Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Usage Data Collectors");
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label info = new Label(group, SWT.NULL);
		info.setText("Usage Data collected will be sent to following recipients:");

		Link details;
		for (UsageCollector collector : UiUsageMonitorPlugin.getDefault().getStudyParameters().getUsageCollectors()) {
			final String detailsUrl = collector.getDetailsUrl();

			details = new Link(group, SWT.NULL);
			details.setText(String.format("<A>%s</A>", (String) Platform.getBundle(collector.getBundle())
					.getHeaders()
					.get("Bundle-Name")));
			details.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					WorkbenchUtil.openUrl(detailsUrl);
				}
			});
		}
	}

	public void init(IWorkbench workbench) {
		// Nothing to init
	}

	private void updateEnablement() {
		if (!enableMonitoring.getSelection()) {
			logFileText.setEnabled(false);
			enableSubmission.setEnabled(false);
			submissionTime.setEnabled(false);
		} else {
			logFileText.setEnabled(true);
			enableSubmission.setEnabled(true);
			if (!enableSubmission.getSelection()) {
				submissionTime.setEnabled(false);
			} else {
				submissionTime.setEnabled(true);
			}
		}

	}

	private void createLogFileSection(Composite parent) {
		final Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.UsageDataPreferencePage_4);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableMonitoring = new Button(group, SWT.CHECK);
		enableMonitoring.setText(Messages.UsageDataPreferencePage_5);
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableMonitoring.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});

		String logFilePath = UiUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath();
		logFilePath = logFilePath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		logFileText = new Text(group, SWT.BORDER);
		logFileText.setText(logFilePath);
		logFileText.setEditable(false);
		logFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableObfuscation = new Button(group, SWT.CHECK);
		enableObfuscation.setText(Messages.UsageDataPreferencePage_8);
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
		Label obfuscationLablel = new Label(group, SWT.NULL);
		obfuscationLablel.setText(InteractionEventObfuscator.ENCRYPTION_ALGORITHM + Messages.UsageDataPreferencePage_9);
	}

	private void createUsageSection(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.UsageDataPreferencePage_10);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label events = new Label(group, SWT.NULL);
		events.setText(Messages.UsageDataPreferencePage_12);
		Label logged = new Label(group, SWT.NULL);
		logged.setText("" + getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS)); //$NON-NLS-1$

		Composite enableSubmissionComposite = new Composite(group, SWT.NULL);
		GridLayout submissionGridLayout = new GridLayout(4, false);
		submissionGridLayout.marginWidth = 0;
		submissionGridLayout.marginHeight = 0;
		enableSubmissionComposite.setLayout(submissionGridLayout);
		enableSubmission = new Button(enableSubmissionComposite, SWT.CHECK);

		enableSubmission.setText(Messages.UsageDataPreferencePage_14);
		enableSubmission.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION));
		enableSubmission.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		submissionTime = new Text(enableSubmissionComposite, SWT.BORDER | SWT.RIGHT);
		GridData gridData = new GridData();
		gridData.widthHint = 30;
		submissionTime.setLayoutData(gridData);
		long submissionFreq = UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;
		if (UiUsageMonitorPlugin.getPrefs().contains(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY)) {
			submissionFreq = UiUsageMonitorPlugin.getPrefs().getLong(
					MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY);
		}
		long submissionFreqInDays = submissionFreq / DAYS_IN_MS;
		submissionTime.setText("" + submissionFreqInDays); //$NON-NLS-1$
		submissionTime.setTextLimit(2);
		submissionTime.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
		Label label2 = new Label(enableSubmissionComposite, SWT.NONE);
		label2.setText(Messages.UsageDataPreferencePage_16);
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		logFileText.setText(UiUsageMonitorPlugin.getDefault().getMonitorLogFile().getPath());
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE,
				enableObfuscation.getSelection());
		if (enableMonitoring.getSelection()) {
			UiUsageMonitorPlugin.getDefault().startMonitoring();
		} else {
			UiUsageMonitorPlugin.getDefault().stopMonitoring();
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLE_SUBMISSION,
				enableSubmission.getSelection());

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED,
				enableMonitoring.getSelection());

		long transmitFrequency = UiUsageMonitorPlugin.DEFAULT_DELAY_BETWEEN_TRANSMITS;

		String submissionFrequency = submissionTime.getText();

		try {
			transmitFrequency = Integer.parseInt(submissionFrequency);
			transmitFrequency *= DAYS_IN_MS;
		} catch (NumberFormatException nfe) {
			// do nothing, transmitFrequency will have the default value
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_SUBMIT_FREQUENCY, transmitFrequency);
		return true;
	}

	@Override
	public boolean performCancel() {
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableObfuscation.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_OBFUSCATE));
		return true;
	}
}
