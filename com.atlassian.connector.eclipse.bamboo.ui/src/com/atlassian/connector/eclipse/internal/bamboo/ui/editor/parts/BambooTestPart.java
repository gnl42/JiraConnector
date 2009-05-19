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
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiUtil;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowTestResultsAction;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part displaying the Test summary
 * 
 * @author Thomas Ehrnhoefer
 * @param <E>
 */
public class BambooTestPart extends AbstractBambooEditorFormPart {
	private ShowTestResultsAction showTestResultsAction;

	private Link link;

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

		createActions();

		createLink(mainComposite, toolkit, "Retrieving build logs from server...", null, null, null);

		createShowInJunitLink();

		toolkit.paintBordersFor(mainComposite);

		setSection(toolkit, section);

		return control;
	}

	private void createShowInJunitLink() {
		if (bambooBuild.getTestsFailed() + bambooBuild.getTestsPassed() > 0) {
			Link link = createLink(mainComposite, toolkit, null, "Show Test Results", null, new Listener() {
				public void handleEvent(Event event) {
					showTestResultsAction.run();
				}
			});
			if (!showTestResultsAction.isEnabled()) {
				link.setEnabled(false);
			}
		}
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		createActions();
		toolBarManager.add(showTestResultsAction);
	}

	private void createActions() {
		if (showTestResultsAction == null) {
			showTestResultsAction = new ShowTestResultsAction();
			showTestResultsAction.selectionChanged(new StructuredSelection(bambooBuild));
		}
	}

	@Override
	public void buildInfoRetrievalDone() {
		reinitMainComposite();

		Composite labelComposite = toolkit.createComposite(mainComposite, SWT.NONE);
		int failed = bambooBuild.getTestsFailed();
		int passed = bambooBuild.getTestsPassed();
		GridLayout layout = new GridLayout();
		if (failed > 0) {
			layout.numColumns = 4;
		} else {
			layout.numColumns = 2;
		}
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.makeColumnsEqualWidth = false;
		labelComposite.setLayout(layout);
		createReadOnlyText(toolkit, labelComposite, String.valueOf(failed + passed), "Tests in total:", false);
		if (failed > 0) {
			createReadOnlyText(toolkit, labelComposite, String.valueOf(failed), "   Failed:", false);
		}
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.TOP).applyTo(labelComposite);

		String failedTests = BambooUiUtil.getFailedTestsDescription(buildDetails);
		if (failedTests != null) {
			if (failedTests.length() > 0) {

				Composite labelComp = toolkit.createComposite(mainComposite);
				labelComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
				GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(labelComp);
				createLabelControl(toolkit, labelComp, CommonImages.getImage(BambooImages.FAILED_TESTS));
				createLabelControl(toolkit, labelComp, "Failed Tests:");
				createReadOnlyText(toolkit, mainComposite, JFaceResources.getDefaultFont(), failedTests,
						FULL_WIDTH / 2, 5);
			}
		} else {
			link = createLink(mainComposite, toolkit, "Retrieving tests from server failed. Click to", "try again",
					".", new Listener() {
						public void handleEvent(Event event) {
							link.removeListener(SWT.Selection, this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		createShowInJunitLink();
		getBuildEditor().reflow();

	}
}
