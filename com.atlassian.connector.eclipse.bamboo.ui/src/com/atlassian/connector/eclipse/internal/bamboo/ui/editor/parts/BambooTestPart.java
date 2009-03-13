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
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Iterator;

/**
 * Part displaying the Test summary
 * 
 * @author Thomas Ehrnhoefer
 * @param <E>
 */
public class BambooTestPart extends AbstractBambooEditorFormPart {
	private ShowTestResultsAction showTestResultsAction;

	public BambooTestPart() {
		super("");
	}

	public BambooTestPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		Section section = createSection(parent, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 7;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		int failed = bambooBuild.getTestsFailed();
		int passed = bambooBuild.getTestsPassed();

		Image stateImage;
		switch (bambooBuild.getStatus()) {
		case SUCCESS:
			stateImage = CommonImages.getImage(BambooImages.STATUS_PASSED);
			break;
		case FAILURE:
			stateImage = CommonImages.getImage(BambooImages.STATUS_FAILED);
			break;
		default:
			stateImage = CommonImages.getImage(BambooImages.STATUS_DISABLED);
		}
		Label label = createLabelControl(toolkit, composite, stateImage);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);
		createReadOnlyText(toolkit, composite, String.valueOf(failed + passed), "Tests in total:", false);
		createReadOnlyText(toolkit, composite, String.valueOf(failed), "     Test failed:", false);
		createReadOnlyText(toolkit, composite, String.valueOf(passed), "     Test passed:", false);

		Text text = createReadOnlyText(toolkit, composite, getFailedTests(), "Failed Tests", true);

		GridDataFactory.fillDefaults().grab(true, true).span(6, 1).applyTo(text);

		toolkit.paintBordersFor(composite);

		section.setClient(composite);
		setSection(toolkit, section);

		return control;
	}

	private String getFailedTests() {
		StringBuilder b = new StringBuilder();
		Iterator<TestDetails> it = buildDetails.getFailedTestDetails().iterator();
		while (it.hasNext()) {
			TestDetails details = it.next();
			b.append(details.getTestClassName() + "." + details.getTestMethodName());
			if (it.hasNext()) {
				b.append(System.getProperty("line.separator"));
			}
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
			showTestResultsAction.setEnabled(true);
		}
	}

}
