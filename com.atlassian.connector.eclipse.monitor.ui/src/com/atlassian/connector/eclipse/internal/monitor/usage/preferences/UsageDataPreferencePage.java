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

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;

import com.atlassian.connector.eclipse.commons.core.CoreConstants;
import com.atlassian.connector.eclipse.internal.monitor.usage.Messages;
import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorUiPlugin;
import com.atlassian.connector.eclipse.internal.monitor.usage.wizards.UsageSubmissionWizard;
import com.atlassian.connector.eclipse.monitor.core.MonitorCorePlugin;
import com.atlassian.connector.eclipse.monitor.core.MonitorPreferenceConstants;
import com.atlassian.connector.eclipse.ui.preferences.EclipsePreferencesAdapter;

/**
 * @author Mik Kersten
 * @author Ken Sueda
 */
public class UsageDataPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button enableMonitoring;

	private Text logFileText;

	private Button openFile;

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
		Link details = new Link(parent, SWT.NONE);
		details.setText(String.format("<A href=\"%s\">Check %s documentation for details.</A>",
				MonitorCorePlugin.HELP_URL, CoreConstants.PRODUCT_NAME));
		details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}
		});
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(null);
	}

	private void updateEnablement() {
		logFileText.setEnabled(enableMonitoring.getSelection());
		openFile.setEnabled(enableMonitoring.getSelection());
	}

	private void createMonitoringAndFeedbackSection(Composite parent) {
		enableMonitoring = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(enableMonitoring);
		enableMonitoring.setText(Messages.UsageDataPreferencePage_enable_monitoring);
		enableMonitoring.setSelection(getPreferenceStore().getBoolean(
				MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
		enableMonitoring.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		Composite c = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(c);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(c);

		final Label label = new Label(c, SWT.NONE);
		label.setText("Log file:");

		final File logFilePath = MonitorCorePlugin.getDefault().getMonitorLogFile();
		logFileText = new Text(c, SWT.BORDER);
		logFileText.setText(logFilePath.getPath());
		logFileText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(logFileText);

		openFile = new Button(c, SWT.PUSH);
		openFile.setText("Show file");
		openFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(logFilePath.getPath()));
				try {
					IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							fileStore);
					((PreferenceDialog) getContainer()).close();
				} catch (PartInitException e1) {
					StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN, "Unable to open editor", e1));
				}
			}
		});
		openFile.setEnabled(getPreferenceStore().getBoolean(MonitorPreferenceConstants.PREF_MONITORING_ENABLED));
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
