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

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiUtil;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowBuildLogAction;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part displaying the build log
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooBuildLogPart extends AbstractBambooEditorFormPart {

	private ShowBuildLogAction showBuildLogAction;

	String buildLogSummary = "";

	int logLines = 0;

	int errorLines = 0;

	private Link link;

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

		createLink(mainComposite, toolkit, "Retrieving build logs from server...", null, null, null);

		toolkit.paintBordersFor(mainComposite);

		setSection(toolkit, section);

		return control;
	}

	private void updateBuildLogSummary() {
		errorLines = 0;
		String[] buildLogLines = buildLog == null ? new String[0] : buildLog.split("[\r\n]");
		StringBuilder b = new StringBuilder();
		for (String buildLogLine : buildLogLines) {
			if (buildLogLine.startsWith(BambooUiUtil.LOG_STR_ERROR)) {
				String[] lineElements = buildLogLine.split("\t");
				if (errorLines > 0) {
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
		showBuildLogAction = new ShowBuildLogAction();
		showBuildLogAction.selectionChanged(new StructuredSelection(bambooBuild));
		toolBarManager.add(showBuildLogAction);
	}

	@Override
	public void buildInfoRetrievalDone() {
		reinitMainComposite();

		if (buildLog != null) {
			updateBuildLogSummary();
			link = createLink(mainComposite, toolkit, "The build generated " + String.valueOf(logLines) + " lines ("
					+ String.valueOf(errorLines) + " error lines). See the", "full build log", "for details.",
					new Listener() {
						public void handleEvent(Event event) {
							BaseSelectionListenerAction showBuildAction = new ShowBuildLogAction();
							showBuildAction.selectionChanged(new StructuredSelection(bambooBuild));
							showBuildAction.run();
						}
					});
			link.setEnabled(showBuildLogAction.isEnabled());

			if (buildLogSummary.length() > 0) {
				createReadOnlyText(toolkit, mainComposite, JFaceResources.getTextFont(), buildLogSummary, FULL_WIDTH,
						10);
			}
		} else {
			link = createLink(mainComposite, toolkit, "Retrieving build logs from server failed. Click to",
					"try again", ".", new Listener() {
						public void handleEvent(Event event) {
							link.removeListener(SWT.Selection, this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		getBuildEditor().reflow();
	}
}