/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.mylyn.internal.jira.core.WorkLogConverter;
import org.eclipse.mylyn.internal.jira.core.model.JiraWorkLog;
import org.eclipse.mylyn.internal.jira.ui.JiraConnectorUi;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Steffen Pingel
 */
public class WorkLogPart extends AbstractTaskEditorPart {

	private static final String ID_POPUP_MENU = "org.eclipse.mylyn.jira.ui.editor.menu.worklog";

	private final String[] columns = { "Creator", "Date", "Worked", "Description" };

	private final int[] columnWidths = { 130, 70, 100, 150 };

	private List<TaskAttribute> logEntries;

	private boolean hasIncoming;

	private MenuManager menuManager;

	private Composite composite;

	public WorkLogPart() {
		setPartName("Work Log");
	}

	private void createTable(FormToolkit toolkit, final Composite composite) {
		Table table = toolkit.createTable(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayout(new GridLayout());
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, false)
				.hint(500, SWT.DEFAULT)
				.applyTo(table);

		for (int i = 0; i < columns.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columns[i]);
			column.setWidth(columnWidths[i]);
		}
		table.getColumn(2).setAlignment(SWT.RIGHT);

		TableViewer attachmentsViewer = new TableViewer(table);
		attachmentsViewer.setUseHashlookup(true);
		attachmentsViewer.setColumnProperties(columns);
		ColumnViewerToolTipSupport.enableFor(attachmentsViewer, ToolTip.NO_RECREATE);

		attachmentsViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				JiraWorkLog item1 = (JiraWorkLog) e1;
				JiraWorkLog item2 = (JiraWorkLog) e2;
				Date created1 = item1.getCreated();
				Date created2 = item2.getCreated();
				if (created1 != null && created2 != null) {
					return created1.compareTo(created2);
				} else if (created1 == null && created2 != null) {
					return -1;
				} else if (created1 != null && created2 == null) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		List<JiraWorkLog> workLogList = new ArrayList<JiraWorkLog>(logEntries.size());
		for (TaskAttribute attribute : logEntries) {
			JiraWorkLog log = new WorkLogConverter().createFrom(attribute);
			workLogList.add(log);
		}
		attachmentsViewer.setContentProvider(new ArrayContentProvider());
		attachmentsViewer.setLabelProvider(new WorkLogTableLabelProvider());
		attachmentsViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				TasksUiUtil.openUrl(JiraConnectorUi.getTaskWorkLogUrl(getModel().getTaskRepository(),
						getModel().getTask()));
			}
		});
		attachmentsViewer.addSelectionChangedListener(getTaskEditorPage());
		attachmentsViewer.setInput(workLogList);

		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				// TODO provide popup menu
			}
		});
		getTaskEditorPage().getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, attachmentsViewer, false);
		Menu menu = menuManager.createContextMenu(table);
		table.setMenu(menu);
	}

	@Override
	public void createControl(Composite parent, final FormToolkit toolkit) {
		initialize();

		final Section section = createSection(parent, toolkit, hasIncoming);
		section.setText(getPartName() + " (" + logEntries.size() + ")");
		if (hasIncoming) {
			expandSection(toolkit, section);
		} else {
			section.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent event) {
					if (composite == null) {
						expandSection(toolkit, section);
						getTaskEditorPage().reflow();
					}
				}
			});
		}
		setSection(toolkit, section);
	}

	private void expandSection(FormToolkit toolkit, Section section) {
		composite = toolkit.createComposite(section);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		getTaskEditorPage().registerDefaultDropListener(section);

		if (logEntries.size() > 0) {
			createTable(toolkit, composite);
		} else {
			Label label = toolkit.createLabel(composite, "No work has yet been logged on this issue.");
			getTaskEditorPage().registerDefaultDropListener(label);
		}

		section.setClient(composite);
	}

	@Override
	public void dispose() {
		if (menuManager != null) {
			menuManager.dispose();
		}
		super.dispose();
	}

	private void initialize() {
		logEntries = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(),
				WorkLogConverter.TYPE_WORKLOG);
		for (TaskAttribute attachmentAttribute : logEntries) {
			if (getModel().hasIncomingChanges(attachmentAttribute)) {
				hasIncoming = true;
				break;
			}
		}
	}

}
