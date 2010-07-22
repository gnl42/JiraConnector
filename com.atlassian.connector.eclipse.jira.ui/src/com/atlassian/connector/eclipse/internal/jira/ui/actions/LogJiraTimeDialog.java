/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.actions;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraTimeFormat;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.JiraEditorUtil;
import com.atlassian.connector.eclipse.internal.jira.ui.editor.Messages;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class LogJiraTimeDialog extends MessageDialog {

	public static final int SKIP_LOGGING = 1;

	private final static String[] buttons = new String[] { Messages.LogJiraTimeDialog_Stop_And_Log,
			Messages.LogJiraTimeDialog_Stop_Only, IDialogConstants.CANCEL_LABEL };

	private JiraWorkLog workLog;

	private long workDoneAmountInSeconds = 0;

	private final TaskRepository repository;

	private Text descriptionText;

	private DateTime dateWidget;

	private DateTime timeWidget;

	protected AdjustEstimateMethod adjustEstimate;

	private Text setRemainigEstimateText;

	protected long remainingEstimateInSeconds;

	private Text timeSpentText;

	private final TaskData taskData;

	private Text reduceRemainigEstimateText;

	public LogJiraTimeDialog(Shell parentShell, TaskData taskData, ITask iTask, long seconds) {
		super(parentShell, Messages.WorkLogPart_Log_Work_Done + " " + iTask.getTaskKey(), null, null, SWT.NONE, //$NON-NLS-1$
				buttons, 0);
		this.taskData = taskData;
		this.workDoneAmountInSeconds = seconds;
		this.repository = TasksUi.getRepositoryManager().getRepository(iTask.getConnectorKind(),
				iTask.getRepositoryUrl());

		this.adjustEstimate = JiraUiUtil.getAdjustEstimateOption(repository);
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		super.createMessageArea(composite);

		// add log work controls
		createTimeComponents(composite);

		// add disable time tracking link
		createDisableTimeTrackingLink(composite);

		return composite;
	}

	private void createTimeComponents(Composite composite) {

		final Composite c1 = new Composite(composite, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		gl.verticalSpacing = 10;
		gl.marginWidth = 0;
		c1.setLayout(gl);

		// time spent
		final Label time = new Label(c1, SWT.NONE);
		time.setText(Messages.WorkLogPart_Time_Spent);

		timeSpentText = new Text(c1, SWT.BORDER);
		String wdhmTime = JiraUtil.getTimeFormat(repository).format(new Long(workDoneAmountInSeconds));
		timeSpentText.setText(wdhmTime);
		GridDataFactory.fillDefaults().applyTo(timeSpentText);

		new Label(c1, SWT.NONE).setText(""); //$NON-NLS-1$

		timeSpentText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateSettings();
			}
		});

		timeSpentText.setToolTipText(getTimeSpentTooltipText(wdhmTime));

		// start date
		final Label startDate = new Label(c1, SWT.NONE);
		startDate.setText(Messages.WorkLogPart_Start_Date);

		final Composite dateTimeComposite = new Composite(c1, SWT.NONE);
		GridLayout dateTimeLayout = new GridLayout(2, false);
		dateTimeLayout.horizontalSpacing = 0;
//		dateTimeLayout.verticalSpacing = 0;
		dateTimeLayout.marginWidth = 0;
		dateTimeLayout.marginHeight = 0;
		dateTimeComposite.setLayout(dateTimeLayout);

		GregorianCalendar now = new GregorianCalendar();

		dateWidget = new DateTime(dateTimeComposite, SWT.DATE);
		dateWidget.setYear(now.get(Calendar.YEAR));
		dateWidget.setMonth(now.get(Calendar.MONTH));
		dateWidget.setDay(now.get(Calendar.DAY_OF_MONTH));
//		GridDataFactory.fillDefaults().applyTo(dateWidget);

		timeWidget = new DateTime(dateTimeComposite, SWT.TIME);
		timeWidget.setHours(now.get(Calendar.HOUR_OF_DAY));
		timeWidget.setMinutes(now.get(Calendar.MINUTE));
		timeWidget.setSeconds(now.get(Calendar.SECOND));
		GridDataFactory.fillDefaults().indent(4, 0).applyTo(timeWidget);

		new Label(c1, SWT.NONE).setText(""); //$NON-NLS-1$

		// adjust estimate
		final Label adjust = new Label(c1, SWT.NONE);
		adjust.setText(Messages.WorkLogPart_Adjust_Estimate);

		final Composite adjustComposite = new Composite(c1, SWT.NONE);
		adjustComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 5).spacing(0, 5).create());
		GridDataFactory.fillDefaults().span(2, 1).applyTo(adjustComposite);

		final Button autoAdjustButton = new Button(adjustComposite, SWT.RADIO);
		autoAdjustButton.setText(Messages.WorkLogPart_Auto_Adjust);
		autoAdjustButton.setToolTipText(Messages.WorkLogPart_Auto_Adjust_Explanation_Tooltip);
		autoAdjustButton.setSelection(adjustEstimate == AdjustEstimateMethod.AUTO);
		autoAdjustButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				adjustEstimate = JiraWorkLog.AdjustEstimateMethod.AUTO;
				validateSettings();
				JiraUiUtil.updateAdjustEstimateOption(adjustEstimate, repository);
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).applyTo(autoAdjustButton);

		final Button leaveAdjustButton = new Button(adjustComposite, SWT.RADIO);
		leaveAdjustButton.setText(Messages.WorkLogPart_Leave_Existing_Estimate);
		leaveAdjustButton.setSelection(adjustEstimate == AdjustEstimateMethod.LEAVE);
		leaveAdjustButton.setToolTipText(Messages.WorkLogPart_Leave_Existing_Explanation_Tooltip);
		leaveAdjustButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				adjustEstimate = JiraWorkLog.AdjustEstimateMethod.LEAVE;
				validateSettings();
				JiraUiUtil.updateAdjustEstimateOption(adjustEstimate, repository);
			}

		});
		GridDataFactory.fillDefaults().span(2, 1).applyTo(leaveAdjustButton);

		final Button setRemainingTimeButton = new Button(adjustComposite, SWT.RADIO);
		setRemainingTimeButton.setText(Messages.LogJiraTimeDialog_Set_estimated_time_remaining);
		setRemainingTimeButton.setSelection(adjustEstimate == AdjustEstimateMethod.SET);
		setRemainingTimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				adjustEstimate = JiraWorkLog.AdjustEstimateMethod.SET;
				setRemainigEstimateText.setEnabled(setRemainingTimeButton.getSelection());
				validateSettings();
				JiraUiUtil.updateAdjustEstimateOption(adjustEstimate, repository);
			}
		});

		setRemainigEstimateText = new Text(adjustComposite, SWT.BORDER);
		setRemainigEstimateText.setEnabled(setRemainingTimeButton.getSelection());
		setRemainigEstimateText.setToolTipText(getTimeSpentTooltipText());
		setRemainigEstimateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateSettings();
			}
		});
		GridDataFactory.fillDefaults().indent(10, SWT.DEFAULT).applyTo(setRemainigEstimateText);

		final Button reduceRemainingTimeButton = new Button(adjustComposite, SWT.RADIO);
		reduceRemainingTimeButton.setText(Messages.LogJiraTimeDialog_Reduce_estimated_time_by);
		reduceRemainingTimeButton.setSelection(adjustEstimate == AdjustEstimateMethod.REDUCE);
		reduceRemainingTimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				adjustEstimate = JiraWorkLog.AdjustEstimateMethod.REDUCE;
//				reduceEstimate = true;
				reduceRemainigEstimateText.setEnabled(reduceRemainingTimeButton.getSelection());
				validateSettings();
				JiraUiUtil.updateAdjustEstimateOption(adjustEstimate, repository);
			}
		});

		reduceRemainigEstimateText = new Text(adjustComposite, SWT.BORDER);
		reduceRemainigEstimateText.setEnabled(reduceRemainingTimeButton.getSelection());
		reduceRemainigEstimateText.setToolTipText(getTimeSpentTooltipText());
		reduceRemainigEstimateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateSettings();
			}
		});
		GridDataFactory.fillDefaults().indent(10, SWT.DEFAULT).applyTo(reduceRemainigEstimateText);

		// work description
		final Label description = new Label(c1, SWT.NONE);
		description.setText(Messages.WorkLogPart_Work_Description);

		final Composite descComposite = new Composite(c1, SWT.NONE);
		descComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).create());
		GridData descGD = new GridData();
		descGD.widthHint = 350;
		descGD.horizontalSpan = 2;
		descComposite.setLayoutData(descGD);

		descriptionText = new Text(descComposite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).hint(150, 100).applyTo(descriptionText);
	}

	protected void validateSettings() {
		boolean ok = true;
		boolean controlIsOk = true;

		JiraTimeFormat formatter = JiraUtil.getTimeFormat(repository);

		timeSpentText.setToolTipText(getTimeSpentTooltipText());
		try {
			workDoneAmountInSeconds = formatter.parse(timeSpentText.getText());
			if (workDoneAmountInSeconds <= 0) {
				controlIsOk = false;
			}
		} catch (ParseException ex) {
			controlIsOk = false;
		}
		if (controlIsOk) {
			JiraEditorUtil.showTimeSpentDecorator(timeSpentText, repository, false);
		} else {
			JiraEditorUtil.showTimeSpentDecorator(timeSpentText, repository, true);
			ok = false;
		}

		JiraEditorUtil.showTimeSpentDecorator(reduceRemainigEstimateText, repository, false);
		JiraEditorUtil.showTimeSpentDecorator(setRemainigEstimateText, repository, false);

		if (adjustEstimate == AdjustEstimateMethod.SET) {
			try {
				remainingEstimateInSeconds = formatter.parse(setRemainigEstimateText.getText());
			} catch (ParseException ex) {
				controlIsOk = false;
			}

			if (!controlIsOk || remainingEstimateInSeconds < 0) {
				JiraEditorUtil.showTimeSpentDecorator(setRemainigEstimateText, repository, true);
				ok = false;
			}

		} else if (adjustEstimate == AdjustEstimateMethod.REDUCE) {
			TaskAttribute estimate = taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_ESTIMATE);
			if (estimate == null) {
				estimate = taskData.getRoot().getAttribute(IJiraConstants.ATTRIBUTE_INITIAL_ESTIMATE);
			}
			try {
				remainingEstimateInSeconds = (estimate == null ? 0 : Long.parseLong(estimate.getValue()))
						- formatter.parse(reduceRemainigEstimateText.getText());
			} catch (ParseException ex) {
				controlIsOk = false;
			}

			if (!controlIsOk || remainingEstimateInSeconds < 0) {
				JiraEditorUtil.showTimeSpentDecorator(reduceRemainigEstimateText, repository, true);
				ok = false;
			}
		}

		getButton(0).setEnabled(ok);
	}

	private GregorianCalendar collectDate() {
		GregorianCalendar cal = new GregorianCalendar();

		cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay(), timeWidget.getHours(),
				timeWidget.getMinutes(), timeWidget.getSeconds());

		return cal;
	}

	private void collectWorkLog() {

		GregorianCalendar cal = collectDate();

		JiraWorkLog tempworkLog = new JiraWorkLog();
		tempworkLog.setAuthor(repository.getUserName());
		tempworkLog.setComment(descriptionText.getText());
		tempworkLog.setStartDate(cal.getTime());
		tempworkLog.setTimeSpent(workDoneAmountInSeconds);
		tempworkLog.setAdjustEstimate(adjustEstimate);
		tempworkLog.setNewRemainingEstimate(remainingEstimateInSeconds);
		workLog = tempworkLog;
	}

	private void createDisableTimeTrackingLink(Composite composite) {

		final Link link = new Link(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(link);
		link.setText("<a>" + Messages.WorkLogTime_Disable_Time_Tracking + "</a> " + Messages.LogJiraTimeDialog_not_show_dialog); //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(Window.CANCEL);
				close();

				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

				final PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(shell,
						TasksUiPreferencePage.ID, null, null);
				if (prefDialog != null) {
					prefDialog.open();
				} else {
					String message = Messages.LogJiraTimeDialog_Unable_to_find_Time_Tracking_preference;
					MessageDialog.openError(shell, JiraUiPlugin.PRODUCT_NAME, message);
					StatusHandler.log(new Status(IStatus.ERROR, JiraUiPlugin.ID_PLUGIN, message));
				}
			}
		});
	}

	public JiraWorkLog getWorkLog() {
		return workLog;
	}

	private String getTimeSpentTooltipText() {
		final String timeSpendTooltip = NLS.bind(Messages.WorkLogPart_Time_Spent_Explanation_Tooltip,
				JiraUtil.getWorkDaysPerWeek(repository), JiraUtil.getWorkHoursPerDay(repository));

		return timeSpendTooltip;
	}

	private String getTimeSpentTooltipText(String wdhmTime) {

		StringBuilder tooltip = new StringBuilder(getTimeSpentTooltipText());

		if (wdhmTime != null && wdhmTime.length() > 0) {
			tooltip.append("\n\n"); //$NON-NLS-1$
			tooltip.append(NLS.bind(Messages.WorkLogPart_Auto_Filled, wdhmTime));
		}

		return tooltip.toString();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		// disable button for time less than 60seconds 
		if (workDoneAmountInSeconds >= 60) {
			getButton(0).setEnabled(true);
		} else {
			getButton(0).setEnabled(false);
		}

		validateSettings();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			collectWorkLog();
		}
		super.buttonPressed(buttonId);
	}
}
