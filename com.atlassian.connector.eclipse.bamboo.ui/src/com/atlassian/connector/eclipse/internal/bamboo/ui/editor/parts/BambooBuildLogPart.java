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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowBuildLogAction;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * Part displaying the build log
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooBuildLogPart extends AbstractBambooEditorFormPart {

	private static final String LOG_STR_ERROR = "error";

	private ShowBuildLogAction showBuildLogAction;

	String buildLogSummary = "";

	int logLines = 0;

	int errorLines = 0;

	private Hyperlink link;

	public BambooBuildLogPart() {
		super("");
	}

	public BambooBuildLogPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		super.toolkit = toolkit;
		createSectionAndComposite(parent, toolkit, 2, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED);

		updateBuildLogSummary();

		createLinks(mainComposite, toolkit, "Retrieving build logs from server...", "", "", null);

		toolkit.paintBordersFor(mainComposite);

		section.setClient(mainComposite);
		setSection(toolkit, section);

		return control;
	}

	private void updateBuildLogSummary() {
		errorLines = 0;
		String[] buildLogLines = buildLog == null ? new String[0] : buildLog.split("[\r\n]");
		StringBuilder b = new StringBuilder();
		for (String buildLogLine : buildLogLines) {
			if (buildLogLine.startsWith(LOG_STR_ERROR)) {
				String[] lineElements = buildLogLine.split("\t");
				if (b.length() > 0) {
					b.append("\n");
				}
				//remove first 3 tokens (type, date, time)
				for (int i = 2; i < lineElements.length; i++) {
					b.append(lineElements[i]);
				}
				errorLines++;
			}
		}
		buildLogSummary = b.toString();
		logLines = buildLogLines.length;

	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		showBuildLogAction = new ShowBuildLogAction(bambooBuild);
		showBuildLogAction.setText("Show Build Log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(true);
		toolBarManager.add(showBuildLogAction);
	}

	@Override
	public void buildInfoRetrievalDone(boolean success) {
		reinitMainComposite();

		if (success) {
			updateBuildLogSummary();
			link = createLinks(mainComposite, toolkit, "The build generated " + String.valueOf(logLines) + " lines ("
					+ String.valueOf(errorLines) + " error lines). See the", "full build log", "for details.",
					new HyperlinkAdapter() {
						@Override
						public void linkActivated(HyperlinkEvent e) {
							link.removeHyperlinkListener(this);
							new ShowBuildLogAction(bambooBuild).run();
						}
					});

			if (buildLogSummary.length() > 0) {
				createReadOnlyText(toolkit, mainComposite, buildLogSummary, 500, 10);
			}
		} else {
			link = createLinks(mainComposite, toolkit, "Retrieving build logs from server failed. Click to",
					"try again", ".", new HyperlinkAdapter() {
						@Override
						public void linkActivated(HyperlinkEvent e) {
							link.removeHyperlinkListener(this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		getBuildEditor().reflow();
	}
}