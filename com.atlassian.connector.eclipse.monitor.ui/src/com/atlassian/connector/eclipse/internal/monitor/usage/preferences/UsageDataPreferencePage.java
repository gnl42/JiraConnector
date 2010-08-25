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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.atlassian.connector.eclipse.commons.core.CoreConstants;
import com.atlassian.connector.eclipse.internal.monitor.core.MonitorPreferenceConstants;
import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorUiPlugin;
import com.atlassian.connector.eclipse.internal.monitor.usage.UsageMonitorImages;
import com.atlassian.connector.eclipse.internal.monitor.usage.wizards.UsageSubmissionWizard;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;
import com.atlassian.connector.eclipse.ui.preferences.EclipsePreferencesAdapter;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class UsageDataPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button enableMonitoring;

	private Text logFileText;

	public UsageDataPreferencePage() {
		super();
		setPreferenceStore(MonitorUiPlugin.getPrefs());
		setDescription(Messages.UsageDataPreferencePage_description);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		createMonitoringAndFeedbackSection(container);
		createCollectorsSection(container);
		updateEnablement();
		return container;
	}

	@Override
	protected void contributeButtons(Composite parent) {
		super.contributeButtons(parent);

		if (parent.getLayout() instanceof GridLayout) {
			// fix number of columns for buttonBar
			GridLayout gl = (GridLayout) parent.getLayout();
			gl.numColumns += 1;
		}

		Button uploadNow = new Button(parent, SWT.PUSH);
		uploadNow.setText("Upload Usage Data Submission");
		uploadNow.setToolTipText("Show Usage Data Submission wizard");
		uploadNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog wizard = new WizardDialog(getShell(), new UsageSubmissionWizard());
				wizard.open();
			}
		});
	}

	private void createCollectorsSection(Composite parent) {
		final Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.UsageDataPreferencePage_collectors);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label info = new Label(group, SWT.NULL);
		info.setText(Messages.UsageDataPreferencePage_sent_to_following_recipients);

		Composite uc = new Composite(group, SWT.NONE);
		uc.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(uc);

		new Label(uc, SWT.NONE).setImage(UsageMonitorImages.getImage(UsageMonitorImages.LOGO));

		Link details = new Link(uc, SWT.NONE);
		details.setText(String.format("<A href=\"%s\">%s</A>", MonitorCorePlugin.HELP_URL, CoreConstants.PRODUCT_NAME));
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}
		});
	}

	public void init(IWorkbench workbench) {
		// Nothing to init
	}

	private void updateEnablement() {
		if (!enableMonitoring.getSelection()) {
			logFileText.setEnabled(false);
		} else {
			logFileText.setEnabled(true);
		}
	}

	private void createMonitoringAndFeedbackSection(Composite parent) {
		final Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.UsageDataPreferencePage_monitoring_and_submission);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enableMonitoring = new Button(group, SWT.CHECK);
		enableMonitoring.setText(Messages.UsageDataPreferencePage_enable_logging_to);
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableMonitoring.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		String logFilePath = MonitorCorePlugin.getDefault().getMonitorLogFile().getPath();
		logFilePath = logFilePath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		logFileText = new Text(group, SWT.BORDER);
		logFileText.setText(logFilePath);
		logFileText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(logFileText);

		createFeedbackSection(group);
	}

	private void createFeedbackSection(Composite group) {
		Label events = new Label(group, SWT.NULL);
		events.setText(Messages.UsageDataPreferencePage_events_since_upload);
		Label logged = new Label(group, SWT.NULL);
		logged.setText("" + getPreferenceStore().getInt(MonitorPreferenceConstants.PREF_NUM_USER_EVENTS)); //$NON-NLS-1$
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		logFileText.setText(MonitorCorePlugin.getDefault().getMonitorLogFile().getPath());
	}

	@Override
	public boolean performOk() {
		boolean wasEnabled = getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED);
		if (enableMonitoring.getSelection()) {
			if (!wasEnabled) {
				MonitorCorePlugin.getDefault().monitoringEnabled();
			}
			MonitorCorePlugin.getDefault().startMonitoring();
		} else {
			if (wasEnabled) {
				MonitorCorePlugin.getDefault().monitoringDisabled();
			}
			MonitorCorePlugin.getDefault().stopMonitoring();
		}

		getPreferenceStore().setValue(MonitorPreferenceConstants.PREF_MONITORING_ENABLED,
				enableMonitoring.getSelection());

		return true;
	}

	@Override
	public boolean performCancel() {
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return new EclipsePreferencesAdapter(new InstanceScope(), MonitorCorePlugin.ID_PLUGIN);
	}
}
