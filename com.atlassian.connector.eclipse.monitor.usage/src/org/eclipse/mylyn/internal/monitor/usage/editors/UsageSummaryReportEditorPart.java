/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage.editors;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventSummarySorter;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.internal.monitor.usage.wizards.UsageSubmissionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;

/**
 * @author Meghan Allen
 */
public class UsageSummaryReportEditorPart extends UsageEditorPart {

	public static final String ID = "com.atlassian.connector.eclipse.monitor.usage.summary.editor";

	private static final long MAX_FILE_LENGTH = 1024 * 1024;

	private static final String DATE_FORMAT_STRING = "MMMMM d, h:mm a";

	// private static final int MAX_NUM_LINES = 1000;

	private Table table;

	private TableViewer tableViewer;

	private final String[] columnNames = new String[] { "Kind", "ID", "Count" };

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		sForm.setText(new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date()));
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
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		createTable(container, toolkit);
		createTableViewer();
		toolkit.paintBordersFor(container);
	}

	@Override
	protected void createActionSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Actions");
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite topContainer = toolkit.createComposite(section);
		GridLayout topContainerLayout = new GridLayout();
		topContainerLayout.numColumns = 1;
		topContainer.setLayout(topContainerLayout);
		section.setClient(topContainer);

		Composite buttonContainer = toolkit.createComposite(topContainer);
		GridLayout buttonContainerLayout = new GridLayout();
		buttonContainerLayout.numColumns = 3;
		buttonContainer.setLayout(buttonContainerLayout);

		Button submitData = toolkit.createButton(buttonContainer, "Submit", SWT.PUSH | SWT.CENTER);
		submitData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				submitData();
			}
		});

		Button viewFile = toolkit.createButton(buttonContainer, "View File", SWT.PUSH | SWT.CENTER);
		viewFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewFile();
			}
		});

		Composite labelContainer = toolkit.createComposite(topContainer);
		GridLayout labelContainerLayout = new GridLayout();
		labelContainerLayout.numColumns = 1;
		labelContainer.setLayout(labelContainerLayout);
	}

	/**
	 * Only opens in workbench if file is small enough not to blow it up.
	 */
	private void viewFile() {
		File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();

		if (monitorFile.length() <= MAX_FILE_LENGTH) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(monitorFile.getAbsolutePath()));
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditorOnFileStore(page, fileStore);
				} catch (PartInitException e) {
				}
			}
		} else {
			boolean failed = false;
			failed = !Program.launch(monitorFile.getAbsolutePath());
			if (failed) {
				Program p = Program.findProgram(".txt");
				if (p != null) {
					p.execute(monitorFile.getAbsolutePath());
				}
			}
		}
	}

	private void submitData() {
		UsageSubmissionWizard submissionWizard = new UsageSubmissionWizard();
		submissionWizard.init(PlatformUI.getWorkbench(), null);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				submissionWizard);
		dialog.create();
		dialog.open();
	}

	private void createTable(Composite parent, FormToolkit toolkit) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = toolkit.createTable(parent, style);
		TableLayout tlayout = new TableLayout();
		table.setLayout(tlayout);
		GridData wd = new GridData(GridData.FILL_HORIZONTAL);
		wd.heightHint = 300;
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

	}

	private void createTableViewer() {
		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames);

		tableViewer.setContentProvider(new UsageCountContentProvider(editorInput.getReportGenerator()));
		tableViewer.setLabelProvider(new UsageCountLabelProvider());
		tableViewer.setInput(editorInput);
	}

}
