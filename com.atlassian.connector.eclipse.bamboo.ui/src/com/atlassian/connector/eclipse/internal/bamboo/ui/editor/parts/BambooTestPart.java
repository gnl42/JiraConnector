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
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowTestResultsAction;
import com.atlassian.theplugin.commons.bamboo.TestDetails;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import java.util.Iterator;

/**
 * Part displaying the Test summary
 * 
 * @author Thomas Ehrnhoefer
 * @param <E>
 */
public class BambooTestPart extends AbstractBambooEditorFormPart {
	private ShowTestResultsAction showTestResultsAction;

	private Hyperlink link;

	public BambooTestPart() {
		super("");
	}

	public BambooTestPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		this.toolkit = toolkit;
		createSectionAndComposite(parent, toolkit, 1, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED
				| ExpandableComposite.TWISTIE);

		createLinks(mainComposite, toolkit, "Retrieving build logs from server...", "", "", null);

		createShowInJunitLink();

		toolkit.paintBordersFor(mainComposite);

		section.setClient(mainComposite);
		setSection(toolkit, section);

		return control;
	}

	private void createShowInJunitLink() {
		Hyperlink link = toolkit.createImageHyperlink(mainComposite, SWT.NONE);
		link.setText("Open tests in JUnit view.");
		link.setEnabled(true);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				showTestResultsAction.run();
			}
		});
	}

	private String getFailedTests() {
		if (buildDetails == null) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		Iterator<TestDetails> it = buildDetails.getFailedTestDetails().iterator();
		while (it.hasNext()) {
			TestDetails details = it.next();
			String testClassName = details.getTestClassName();
			int index = testClassName.lastIndexOf('.');
			if (index == -1) {
				testClassName = "N/A";
			} else {
				testClassName = testClassName.substring(index + 1);
			}
			b.append(testClassName + "   :  " + formatTestMethodName(details.getTestMethodName()));
			if (it.hasNext()) {
				b.append(System.getProperty("line.separator"));
			}
		}
		return b.toString();
	}

	private String formatTestMethodName(String methodName) {
		int i = methodName.indexOf("test");
		if (i != -1) {
			methodName = methodName.substring(i + 4);
		}
		StringBuilder b = new StringBuilder();
		for (char c : methodName.toCharArray()) {
			if (Character.isUpperCase(c)) {
				b.append(" ");
			}
			b.append(c);
		}
		return b.toString();
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		createActions();
		toolBarManager.add(showTestResultsAction);
	}

	private void createActions() {
		if (showTestResultsAction == null) {
			showTestResultsAction = new ShowTestResultsAction(bambooBuild);
			showTestResultsAction.setText("Show Test Results");
			showTestResultsAction.setImageDescriptor(BambooImages.JUNIT);
			if (buildDetails != null
					&& (buildDetails.getFailedTestDetails().size() + buildDetails.getSuccessfulTestDetails().size()) > 0) {
				showTestResultsAction.setEnabled(true);
			} else {
				showTestResultsAction.setEnabled(false);
			}
		}
	}

	@Override
	public void buildInfoRetrievalDone(boolean success) {
		reinitMainComposite();

		if (success) {
			Composite labelComposite = toolkit.createComposite(mainComposite, SWT.NONE);
			labelComposite.setLayout(new GridLayout(6, false));
			int failed = bambooBuild.getTestsFailed();
			int passed = bambooBuild.getTestsPassed();
			createReadOnlyText(toolkit, labelComposite, String.valueOf(failed + passed), "Tests in total:", false);
			createReadOnlyText(toolkit, labelComposite, String.valueOf(failed), "        Failed:", false);
			createReadOnlyText(toolkit, labelComposite, String.valueOf(passed), "        Passed:", false);
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.CENTER, SWT.CENTER).applyTo(labelComposite);

			String failedTests = getFailedTests();
			if (failedTests.length() > 0) {
				Text text = createReadOnlyText(toolkit, mainComposite, failedTests, "Failed Tests:", true, true);
				text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
				GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, DEFAULT_HEIGHT).applyTo(text);
			}
			if (buildDetails != null
					&& (buildDetails.getFailedTestDetails().size() + buildDetails.getSuccessfulTestDetails().size()) > 0) {
				showTestResultsAction.setEnabled(true);
			} else {
				showTestResultsAction.setEnabled(false);
			}
		} else {
			link = createLinks(mainComposite, toolkit, "Retrieving tests from server failed. Click to", "try again",
					".", new HyperlinkAdapter() {
						@Override
						public void linkActivated(HyperlinkEvent e) {
							link.removeHyperlinkListener(this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		createShowInJunitLink();
		getBuildEditor().reflow();

	}
}
