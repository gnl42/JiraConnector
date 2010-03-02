/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Meghan Allen - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.atlassian.connector.eclipse.internal.monitor.usage.InteractionEventSummarySorter;
import com.atlassian.connector.eclipse.internal.monitor.usage.UiUsageMonitorPlugin;

/**
 * @author Mik Kersten
 */
public class UserStudyEditorPart extends UsageEditorPart {

	private Table table;

	private TableViewer tableViewer;

	private final String[] columnNames = new String[] { "Kind", "ID", "Num", "Last Delta", "Users" };

	public UserStudyEditorPart(String id, String title) {
		// super(id, title);
	}

	@Override
	protected void addSections(Composite composite, FormToolkit toolkit) {
		if (editorInput.getReportGenerator().getLastParsedSummary().getSingleSummaries().size() > 0) {
			createUsageSection(editorComposite, toolkit);
		}
	}

	private void createUsageSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Usage Details");
		section.setLayout(new TableWrapLayout());
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		createTable(container, toolkit);
		createTableViewer();
		toolkit.paintBordersFor(container);
	}

	private void createTable(Composite parent, FormToolkit toolkit) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = toolkit.createTable(parent, style);
		TableLayout tlayout = new TableLayout();
		table.setLayout(tlayout);
		TableWrapData wd = new TableWrapData(TableWrapData.FILL_GRAB);
		wd.heightHint = 300;
		wd.grabVertical = true;
		table.setLayoutData(wd);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText(columnNames[0]);
		column.setWidth(60);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new InteractionEventSummarySorter(InteractionEventSummarySorter.TYPE));

			}
		});

		column = new TableColumn(table, SWT.LEFT, 1);
		column.setText(columnNames[1]);
		column.setWidth(370);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new InteractionEventSummarySorter(InteractionEventSummarySorter.NAME));
			}
		});

		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText(columnNames[2]);
		column.setWidth(50);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new InteractionEventSummarySorter(InteractionEventSummarySorter.USAGE_COUNT));
			}
		});

		// column = new TableColumn(table, SWT.LEFT, 3);
		// column.setText(columnNames[3]);
		// column.setWidth(60);

		column = new TableColumn(table, SWT.LEFT, 3);
		column.setText(columnNames[3]);
		column.setWidth(60);

		column = new TableColumn(table, SWT.LEFT, 4);
		column.setText(columnNames[4]);
		column.setWidth(60);
	}

	private void createTableViewer() {
		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames);

		tableViewer.setContentProvider(new UsageCountContentProvider(editorInput.getReportGenerator()));
		tableViewer.setLabelProvider(new UsageCountLabelProvider());
		tableViewer.setInput(editorInput);
	}

	@Override
	protected void exportToCSV() {

		// Ask the user to pick a directory into which to place multiple CSV
		// files
		try {
			DirectoryDialog dialog = new DirectoryDialog(getSite().getWorkbenchWindow().getShell());
			dialog.setText("Specify a directory for the CSV files");
			String directoryName = dialog.open();

			File outputFile;
			FileOutputStream outputStream;

			String filename = directoryName + File.separator + "Usage.csv";
			outputFile = new File(filename);

			outputStream = new FileOutputStream(outputFile, false);

			// Delegate to all collectors
			for (IUsageCollector collector : editorInput.getReportGenerator().getCollectors()) {
				collector.exportAsCSVFile(directoryName);
			}

			int columnCount = table.getColumnCount();
			for (TableItem item : table.getItems()) {

				for (int i = 0; i < columnCount - 1; i++) {

					outputStream.write((item.getText(i) + ",").getBytes());
				}

				outputStream.write(item.getText(columnCount - 1).getBytes());
				outputStream.write(("\n").getBytes());

			}
			outputStream.flush();
			outputStream.close();
		} catch (SWTException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Unable to get directory name",
					e));
		} catch (FileNotFoundException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Could not resolve file", e));
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Could not write to file", e));
		}
	}

}
