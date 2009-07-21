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

package org.eclipse.mylyn.internal.monitor.usage.editors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

/**
 * @author Mik Kersten
 */
public class UsageEditorPart extends EditorPart {

	protected UsageStatsEditorInput editorInput;

	protected FormToolkit toolkit;

	protected ScrolledForm sForm;

	protected Composite editorComposite;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		editorInput = (UsageStatsEditorInput) input;
		setPartName(editorInput.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		sForm = toolkit.createScrolledForm(parent);
		sForm.getBody().setLayout(new GridLayout());// TableWrapLayout());
		editorComposite = sForm.getBody();
		sForm.setText("Usage Summary");
		toolkit.decorateFormHeading(sForm.getForm());
		createSummaryStatsSection(editorComposite, toolkit);
		addSections(editorComposite, toolkit);
		createActionSection(editorComposite, toolkit);
	}

	protected void addSections(Composite composite, FormToolkit toolkit) {
		// none
	}

	@Override
	public void setFocus() {
	}

	protected void createActionSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Actions");
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		Button exportHtml = toolkit.createButton(container, "Export as HTML", SWT.PUSH | SWT.CENTER);
		exportHtml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToHtml();
			}
		});

		Button export = toolkit.createButton(container, "Export as CSV Files", SWT.PUSH | SWT.CENTER);
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToCSV();
			}
		});
	}

	protected void createSummaryStatsSection(Composite parent, FormToolkit toolkit) {
		for (IUsageCollector collector : editorInput.getReportGenerator().getLastParsedSummary().getCollectors()) {
			List<String> summary = collector.getPlainTextReport();
			if (!summary.isEmpty()) {
				Section summarySection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR
						| ExpandableComposite.TWISTIE);
				summarySection.setText(collector.getReportTitle());
				summarySection.setLayout(new GridLayout());
				summarySection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				Composite summaryContainer = toolkit.createComposite(summarySection);
				summaryContainer.setLayout(new GridLayout());

				GridData data = new GridData(GridData.FILL_BOTH);
				data.horizontalSpan = 1;
				data.grabExcessHorizontalSpace = true;
				summaryContainer.setLayoutData(data);

				// GridDataFactory.fillDefaults().grab(true,
				// false).applyTo(summaryContainer);
				summarySection.setClient(summaryContainer);
				/*
				 * 
				 * GridLayout attributesLayout = new GridLayout();
				 * attributesLayout.numColumns = 4;
				 * attributesLayout.horizontalSpacing = 5;
				 * attributesLayout.verticalSpacing = 4;
				 * attributesComposite.setLayout(attributesLayout); GridData
				 * attributesData = new GridData(GridData.FILL_BOTH);
				 * attributesData.horizontalSpan = 1;
				 * attributesData.grabExcessVerticalSpace = false;
				 * attributesComposite.setLayoutData(attributesData);
				 * attributesSection.setClient(attributesComposite);
				 */

				StyledText t = new StyledText(summaryContainer, SWT.NONE);
				t.setEditable(false);
				for (String description : summary) {
					t.append(description + System.getProperty("line.separator"));

				}
				/*
				 * Browser browser = new Browser(summaryContainer, SWT.NONE);
				 * GridData browserLayout = new GridData(GridData.FILL_BOTH);
				 * browserLayout.heightHint = summary.size() * 25; //
				 * browserLayout.widthHint = 500;
				 * browser.setLayoutData(browserLayout); String htmlText = "<html><head><LINK
				 * REL=STYLESHEET HREF=\"http://eclipse.org/default_style.css\"
				 * TYPE=\"text/css\"></head><body topmargin=0 leftmargin=0
				 * rightmargin=0><font size=-1>\n"; for (String description :
				 * summary) { htmlText += description;  } htmlText += "</font></body></html>";
				 * browser.setText(htmlText);
				 */
				// if (description.equals(ReportGenerator.SUMMARY_SEPARATOR)) {
				// toolkit.createLabel(summaryContainer,
				// "---------------------------------");
				// toolkit.createLabel(summaryContainer,
				// "---------------------------------");
				// } else {
				// Label label = toolkit.createLabel(summaryContainer,
				// description);
				// if (!description.startsWith("<h"));
				// label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
				// label.setLayoutData(new
				// TableWrapData(TableWrapData.FILL_GRAB));
				// }
				// }
			}
		}
	}

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

	protected void exportToHtml() {
		File outputFile;
		try {
			FileDialog dialog = new FileDialog(getSite().getWorkbenchWindow().getShell());
			dialog.setText("Specify a file name");
			dialog.setFilterExtensions(new String[] { "*.html", "*.*" });

			String filename = dialog.open();
			if (!filename.endsWith(".html")) {
				filename += ".html";
			}
			outputFile = new File(filename);
			// outputStream = new FileOutputStream(outputFile, true);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write("<html><head>");
			for (IUsageCollector collector : editorInput.getReportGenerator().getCollectors()) {
				writer.write("<h3>" + collector.getReportTitle() + "</h3>");
				for (String reportLine : collector.getReport()) {
					writer.write(reportLine);
				}
				writer.write("<br><hr>");
			}
			writer.write("</body></html>");
			writer.close();
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Cound not write to file", e));
		}
	}

}
