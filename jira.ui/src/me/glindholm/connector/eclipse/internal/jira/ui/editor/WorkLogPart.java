/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.preferences.TasksUiPreferencePage;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import me.glindholm.connector.eclipse.internal.jira.core.WorkLogConverter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog.AdjustEstimateMethod;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraTimeFormat;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraConnectorUi;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiUtil;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class WorkLogPart extends AbstractTaskEditorPart {

    private static final String ID_POPUP_MENU = "org.eclipse.mylyn.jira.ui.editor.menu.worklog"; //$NON-NLS-1$

    private final String[] columns = { Messages.WorkLogPart_Creator, Messages.WorkLogPart_Date, Messages.WorkLogPart_Worked, Messages.WorkLogPart_Description };

    private final int[] columnWidths = { 200, 300, 100, 300 };

    private List<TaskAttribute> logEntries;

    private boolean hasIncoming;

    private MenuManager menuManager;

    private Composite composite;

    private long newWorkDoneAmount = 0;

    private Calendar newWorkDoneDate = new GregorianCalendar();

    private String newWorkDoneDescription = ""; //$NON-NLS-1$

    private AdjustEstimateMethod newWorkDoneAdjustEstimate = AdjustEstimateMethod.LEAVE;

    private DateTime dateWidget;

    private DateTime timeWidget;

    private Text timeSpentText;

    private boolean includeWorklog;

    private JiraWorkLog newWorkLog;

    private Section newWorkLogSection;

    private Button createNewWorkLog;

    public WorkLogPart() {
        setPartName(Messages.WorkLogPart_Work_Log);
    }

    private void createTable(final FormToolkit toolkit, final Composite composite) {
        final Table table = toolkit.createTable(composite, SWT.MULTI | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).hint(500, SWT.DEFAULT).applyTo(table);

        for (int i = 0; i < columns.length; i++) {
            final TableColumn column = new TableColumn(table, SWT.LEFT, i);
            column.setText(columns[i]);
            column.setWidth(columnWidths[i]);
        }
        table.getColumn(2).setAlignment(SWT.RIGHT);

        final TableViewer attachmentsViewer = new TableViewer(table);
        attachmentsViewer.setUseHashlookup(true);
        attachmentsViewer.setColumnProperties(columns);
        ColumnViewerToolTipSupport.enableFor(attachmentsViewer, ToolTip.NO_RECREATE);

        attachmentsViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object e1, final Object e2) {
                final JiraWorkLog item1 = (JiraWorkLog) e1;
                final JiraWorkLog item2 = (JiraWorkLog) e2;
                final Instant created1 = item1.getCreated();
                final Instant created2 = item2.getCreated();
                if ((created1 != null) && (created2 != null)) {
                    return created1.compareTo(created2);
                }
                if ((created1 == null) && (created2 != null)) {
                    return -1;
                }
                if ((created1 != null) && (created2 == null)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        final List<JiraWorkLog> workLogList = new ArrayList<>(logEntries.size());
        for (final TaskAttribute attribute : logEntries) {
            final JiraWorkLog log = new WorkLogConverter().createFrom(attribute);
            workLogList.add(log);
        }
        attachmentsViewer.setContentProvider(new ArrayContentProvider());
        attachmentsViewer.setLabelProvider(new WorkLogTableLabelProvider(getJiraTimeFormat()));
        attachmentsViewer
        .addOpenListener(event -> TasksUiUtil.openUrl(JiraConnectorUi.getTaskWorkLogUrl(getModel().getTaskRepository(), getModel().getTask())));
        attachmentsViewer.addSelectionChangedListener(getTaskEditorPage());
        attachmentsViewer.setInput(workLogList);

        menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(manager -> {
            // TODO provide popup menu
        });
        getTaskEditorPage().getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, attachmentsViewer, false);
        final Menu menu = menuManager.createContextMenu(table);
        table.setMenu(menu);
    }

    @Override
    public void createControl(final Composite parent, final FormToolkit toolkit) {
        initialize();

        final Section section = createSection(parent, toolkit, hasIncoming);
        section.setText(getPartName() + " (" + logEntries.size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$

        if (hasIncoming) {
            expandSection(toolkit, section);
        } else {
            section.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(final ExpansionEvent event) {
                    if (composite == null) {
                        expandSection(toolkit, section);
                        getTaskEditorPage().reflow();
                    }
                }
            });
        }
        logWorkToolbarAction = new LogWorkDoneAction(toolkit, section);
        setSection(toolkit, section);
    }

    private void expandSection(final FormToolkit toolkit, final Section section) {
        composite = toolkit.createComposite(section);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        getTaskEditorPage().registerDefaultDropListener(section);

        if (logEntries.size() > 0) {
            createTable(toolkit, composite);
        } else {
            final Label label = toolkit.createLabel(composite, Messages.WorkLogPart_No_work_logged);
            getTaskEditorPage().registerDefaultDropListener(label);
        }
        newWorkLogSection = createNewWorkLogSection(toolkit, composite);
        newWorkLogSection.addExpansionListener(new ExpansionAdapter() {
            private boolean firstTime = true;

            @Override
            public void expansionStateChanged(final ExpansionEvent e) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    setTimeSpendDecorator();
                }
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(newWorkLogSection);
        section.setClient(composite);

        toolkit.paintBordersFor(composite);
    }

    @Override
    public void dispose() {
        if (menuManager != null) {
            menuManager.dispose();
        }
        super.dispose();
    }

    private void initialize() {
        logEntries = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(), WorkLogConverter.TYPE_WORKLOG);
        for (final TaskAttribute attachmentAttribute : logEntries) {
            if (getModel().hasIncomingChanges(attachmentAttribute)) {
                hasIncoming = true;
                break;
            }
        }

        final TaskAttribute newWorkLogAttribute = getTaskData().getRoot().getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
        JiraWorkLog log = null;
        if (newWorkLogAttribute != null) {
            log = new WorkLogConverter().createFrom(newWorkLogAttribute);
        }

        if ((newWorkLogAttribute != null) && (log != null)) {
            newWorkDoneAmount = log.getTimeSpent();
            newWorkDoneDate = new GregorianCalendar();
            if (log.getStartDate() != null) {
                newWorkDoneDate.setTime(Date.from(log.getStartDate()));
            }
            newWorkDoneDescription = log.getComment();
            newWorkDoneAdjustEstimate = log.getAdjustEstimate();
            final TaskAttribute newWorkLogSubmitAttribute = newWorkLogAttribute.getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG);
            if ((newWorkLogSubmitAttribute != null) && newWorkLogSubmitAttribute.getValue().equals(String.valueOf(true))) {
                includeWorklog = true;
            }
        }
    }

    private JiraTimeFormat getJiraTimeFormat() {
        return JiraUtil.getTimeFormat(getTaskEditorPage().getTaskRepository());
    }

    private Section createNewWorkLogSection(final FormToolkit toolkit, final Composite parent) {
        newWorkLogSection = toolkit.createSection(parent,
                ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT | ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
        newWorkLogSection.setText(""); //$NON-NLS-1$

        createNewWorkLog = toolkit.createButton(newWorkLogSection, Messages.WorkLogPart_Log_Work_Done, SWT.CHECK);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.LEFT, SWT.CENTER).applyTo(createNewWorkLog);
        createNewWorkLog.setSelection(includeWorklog);
        newWorkLogSection.setExpanded(includeWorklog);

        createNewWorkLog.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.getSource().equals(createNewWorkLog)) {
                    newWorkLogSection.setExpanded(createNewWorkLog.getSelection());
                    showLogWorkComponents();
                }
            }

        });
        newWorkLogSection.setTextClient(createNewWorkLog);

        final Composite newWorkLogComposite = toolkit.createComposite(newWorkLogSection, SWT.NONE);
        newWorkLogSection.setClient(newWorkLogComposite);
        newWorkLogComposite.setLayout(GridLayoutFactory.swtDefaults().spacing(10, 5).numColumns(3).create());

        if (!MonitorUiPlugin.getDefault().getPreferenceStore().getBoolean(MonitorUiPlugin.ACTIVITY_TRACKING_ENABLED)) {
            final Link timeTrackingDisabled = new Link(newWorkLogComposite, SWT.NONE);
            timeTrackingDisabled.setText(Messages.WorkLogPart_Enable_Automatic_Tracking);
            GridDataFactory.fillDefaults().span(3, 1).applyTo(timeTrackingDisabled);
            timeTrackingDisabled.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    PreferencesUtil
                    .createPreferenceDialogOn(WorkbenchUtil.getShell(), TasksUiPreferencePage.ID, null, null)
                    .open();
                }
            });
            toolkit.adapt(timeTrackingDisabled, true, true);
        }

        final String timeSpendTooltip = getTimeSpentTooltipText();

        toolkit.createLabel(newWorkLogComposite, Messages.WorkLogPart_Time_Spent);
        timeSpentText = toolkit.createText(newWorkLogComposite, getJiraTimeFormat().format(Long.valueOf(newWorkDoneAmount)));
        timeSpentText.addModifyListener(e -> {
            setTimeSpendDecorator();
            timeSpentText.setToolTipText(timeSpendTooltip);
            addWorkLogToModel();
        });

        timeSpentText.setToolTipText(timeSpendTooltip);
        GridDataFactory.fillDefaults().span(2, 1).hint(135, SWT.DEFAULT).align(SWT.BEGINNING, SWT.FILL).applyTo(timeSpentText);

        toolkit.createLabel(newWorkLogComposite, Messages.WorkLogPart_Start_Date);
        dateWidget = new DateTime(newWorkLogComposite, SWT.DATE);
        dateWidget.setYear(newWorkDoneDate.get(Calendar.YEAR));
        dateWidget.setMonth(newWorkDoneDate.get(Calendar.MONTH));
        dateWidget.setDay(newWorkDoneDate.get(Calendar.DAY_OF_MONTH));
        dateWidget.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        toolkit.adapt(dateWidget, true, true);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(dateWidget);
        dateWidget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                collectDate();
                addWorkLogToModel();
            }
        });

        timeWidget = new DateTime(newWorkLogComposite, SWT.TIME);
        timeWidget.setHours(newWorkDoneDate.get(Calendar.HOUR_OF_DAY));
        timeWidget.setMinutes(newWorkDoneDate.get(Calendar.MINUTE));
        timeWidget.setSeconds(newWorkDoneDate.get(Calendar.SECOND));
        timeWidget.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
        toolkit.adapt(timeWidget, true, true);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL).applyTo(timeWidget);
        timeWidget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                collectDate();
                addWorkLogToModel();
            }
        });

        toolkit.createLabel(newWorkLogComposite, Messages.WorkLogPart_Adjust_Estimate);
        final Composite adjustComposite = toolkit.createComposite(newWorkLogComposite);
        adjustComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
        GridDataFactory.fillDefaults().span(2, 1).applyTo(adjustComposite);

        final Button autoAdjustButton = toolkit.createButton(adjustComposite, Messages.WorkLogPart_Auto_Adjust, SWT.RADIO);
        autoAdjustButton.setSelection(newWorkDoneAdjustEstimate == AdjustEstimateMethod.AUTO);
        autoAdjustButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                newWorkDoneAdjustEstimate = AdjustEstimateMethod.AUTO;
                addWorkLogToModel();
            }
        });
        autoAdjustButton.setToolTipText(Messages.WorkLogPart_Auto_Adjust_Explanation_Tooltip);

        final Button leaveAdjustButton = toolkit.createButton(adjustComposite, Messages.WorkLogPart_Leave_Existing_Estimate, SWT.RADIO);
        leaveAdjustButton.setSelection(newWorkDoneAdjustEstimate == AdjustEstimateMethod.LEAVE);
        leaveAdjustButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                newWorkDoneAdjustEstimate = AdjustEstimateMethod.LEAVE;
                addWorkLogToModel();
            }
        });
        leaveAdjustButton.setToolTipText(Messages.WorkLogPart_Leave_Existing_Explanation_Tooltip);

        toolkit.createLabel(newWorkLogComposite, Messages.WorkLogPart_Work_Description);
        final Text descriptionText = toolkit.createText(newWorkLogComposite, newWorkDoneDescription, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
        descriptionText.addModifyListener(e -> {
            newWorkDoneDescription = descriptionText.getText();
            addWorkLogToModel();
        });
        GridDataFactory.fillDefaults().grab(true, true).hint(150, 100).span(2, 1).applyTo(descriptionText);

        toolkit.paintBordersFor(newWorkLogComposite);
        return newWorkLogSection;
    }

    private void showLogWorkComponents() {
        addWorkLogToModel();
        includeWorklog = createNewWorkLog.getSelection();
        setSubmitWorklog();

        if (createNewWorkLog.getSelection() && MonitorUiPlugin.getDefault().isActivityTrackingEnabled()) {
            final long time = JiraUiUtil.getLoggedActivityTime(getTaskEditorPage().getTask());

            final String wdhmTime = JiraUtil.getTimeFormat(getTaskEditorPage().getTaskRepository())
                    .format(Long.valueOf(time));
            if ((wdhmTime != null) && (wdhmTime.length() > 0)) {
                timeSpentText.setText(wdhmTime);

                final StringBuilder tooltip = new StringBuilder(getTimeSpentTooltipText());

                tooltip.append("\n\n"); //$NON-NLS-1$
                tooltip.append(NLS.bind(Messages.WorkLogPart_Auto_Filled, wdhmTime));

                timeSpentText.setToolTipText(tooltip.toString());
            }
        }
    }

    private String getTimeSpentTooltipText() {
        return NLS.bind(Messages.WorkLogPart_Time_Spent_Explanation_Tooltip,
                JiraUtil.getWorkDaysPerWeek(getTaskEditorPage().getTaskRepository()), JiraUtil.getWorkHoursPerDay(getTaskEditorPage().getTaskRepository()));
    }

    protected void collectDate() {
        if (newWorkDoneDate == null) {
            newWorkDoneDate = new GregorianCalendar();
        }
        newWorkDoneDate.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay(), timeWidget.getHours(), timeWidget.getMinutes(),
                timeWidget.getSeconds());
    }

    /**
     * Updates the worklog and returns true if it has changed
     */
    private boolean updateNewWorkLog() {
        final JiraWorkLog tempworkLog = new JiraWorkLog();
        tempworkLog.setAuthor(null); // getTaskEditorPage().getTaskRepository().getUserName());
        tempworkLog.setComment(newWorkDoneDescription);
        tempworkLog.setStartDate(newWorkDoneDate.getTime().toInstant());
        tempworkLog.setTimeSpent(newWorkDoneAmount);
        tempworkLog.setAdjustEstimate(newWorkDoneAdjustEstimate);
        if (tempworkLog.equals(newWorkLog)) {
            return false;
        }
        newWorkLog = tempworkLog;
        return true;
    }

    private void addWorkLogToModel() {
        if (updateNewWorkLog()) {
            TaskAttribute attribute = getTaskData().getRoot().getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
            if (attribute == null) {
                attribute = getTaskData().getRoot().createAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
            }
            new WorkLogConverter().applyTo(newWorkLog, attribute);
            getModel().attributeChanged(attribute);
        }
    }

    private void setSubmitWorklog() {
        final TaskAttribute attribute = getTaskData().getRoot().getAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
        if (attribute != null) {
            attribute.createAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG).setValue(String.valueOf(includeWorklog));
            getModel().attributeChanged(attribute);
        }
    }

    private void setTimeSpendDecorator() {
        try {
            newWorkDoneAmount = getJiraTimeFormat().parse(timeSpentText.getText());
        } catch (final ParseException e) {
            // ignore
        }
        JiraEditorUtil.setTimeSpentDecorator(timeSpentText, false, getTaskEditorPage().getTaskRepository(), false);
    }

    class LogWorkDoneAction extends Action {
        private final Section section;

        private final FormToolkit toolkit;

        public LogWorkDoneAction(final FormToolkit toolkit, final Section section) {
            this.toolkit = toolkit;
            this.section = section;
            setImageDescriptor(CommonImages.CALENDAR);
            setToolTipText(Messages.WorkLogPart_Log_Work_Done);
        }

        @Override
        public void run() {
            if (composite == null) {
                expandSection(toolkit, section);
                getTaskEditorPage().reflow();
            }
            if (newWorkLogSection.isDisposed() || section.isDisposed()) {
                return;
            }
            section.setExpanded(true);
            if (newWorkLogSection != null) {
                newWorkLogSection.setExpanded(true);
                createNewWorkLog.setSelection(true);
                showLogWorkComponents();
            }
        }
    }

    private LogWorkDoneAction logWorkToolbarAction;

    @Override
    protected void fillToolBar(final ToolBarManager toolBarManager) {
        if (logWorkToolbarAction != null) {
            toolBarManager.add(logWorkToolbarAction);
        }
    }

}
