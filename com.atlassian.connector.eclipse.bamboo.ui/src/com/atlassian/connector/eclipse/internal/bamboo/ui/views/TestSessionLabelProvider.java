/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *******************************************************************************/
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

package com.atlassian.connector.eclipse.internal.bamboo.ui.views;

import com.atlassian.connector.eclipse.internal.bamboo.ui.model.ITestCaseElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.ITestElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.ITestSuiteElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestCaseElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestSuiteElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestElement.Status;

import org.eclipse.jdt.internal.junit.Messages;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Image;

import java.text.NumberFormat;

public class TestSessionLabelProvider extends LabelProvider implements IStyledLabelProvider {

	private final TestResultsView fTestRunnerPart;

	private final int fLayoutMode;

	private final NumberFormat timeFormat;

	private boolean fShowTime;

	public TestSessionLabelProvider(TestResultsView testRunnerPart, int layoutMode) {
		fTestRunnerPart = testRunnerPart;
		fLayoutMode = layoutMode;
		fShowTime = true;

		timeFormat = NumberFormat.getNumberInstance();
		timeFormat.setGroupingUsed(true);
		timeFormat.setMinimumFractionDigits(3);
		timeFormat.setMaximumFractionDigits(3);
		timeFormat.setMinimumIntegerDigits(1);
	}

	public StyledString getStyledText(Object element) {
		String label = getSimpleLabel(element);
		if (label == null) {
			return new StyledString(element.toString());
		}
		StyledString text = new StyledString(label);

		ITestElement testElement = (ITestElement) element;
		if (fLayoutMode != TestResultsView.LAYOUT_HIERARCHICAL) {
			if (element instanceof ITestCaseElement) {
				String className = BasicElementLabels.getJavaElementName(((ITestCaseElement) element).getTestClassName());
				String decorated = Messages.format(JUnitMessages.TestSessionLabelProvider_testMethodName_className,
						new Object[] { label, className });
				text = StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.QUALIFIER_STYLER, text);
			}
		}
		return addElapsedTime(text, testElement.getElapsedTimeInSeconds());
	}

	private StyledString addElapsedTime(StyledString styledString, double time) {
		String string = styledString.getString();
		String decorated = addElapsedTime(string, time);
		return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.COUNTER_STYLER, styledString);
	}

	private String addElapsedTime(String string, double time) {
		if (!fShowTime || Double.isNaN(time)) {
			return string;
		}
		String formattedTime = timeFormat.format(time);
		return Messages.format(JUnitMessages.TestSessionLabelProvider_testName_elapsedTimeInSeconds, new String[] {
				string, formattedTime });
	}

	private String getSimpleLabel(Object element) {
		if (element instanceof ITestCaseElement) {
			return BasicElementLabels.getJavaElementName(((ITestCaseElement) element).getTestMethodName());
		} else if (element instanceof ITestSuiteElement) {
			return BasicElementLabels.getJavaElementName(((ITestSuiteElement) element).getSuiteTypeName());
		}
		return null;
	}

	public String getText(Object element) {
		String label = getSimpleLabel(element);
		if (label == null) {
			return element.toString();
		}
		ITestElement testElement = (ITestElement) element;
		if (fLayoutMode != TestResultsView.LAYOUT_HIERARCHICAL) {
			if (element instanceof ITestCaseElement) {
				String className = BasicElementLabels.getJavaElementName(((ITestCaseElement) element).getTestClassName());
				label = Messages.format(JUnitMessages.TestSessionLabelProvider_testMethodName_className, new Object[] {
						label, className });
			}
		}
		return addElapsedTime(label, testElement.getElapsedTimeInSeconds());
	}

	public Image getImage(Object element) {
		if (element instanceof TestCaseElement) {
			TestCaseElement testCaseElement = ((TestCaseElement) element);
			Status status = testCaseElement.getStatus();
			if (status.isError()) {
				return fTestRunnerPart.fTestErrorIcon;
			} else if (status.isOK()) {
				return fTestRunnerPart.fTestOkIcon;
			} else {
				throw new IllegalStateException(element.toString());
			}

		} else if (element instanceof TestSuiteElement) {
			Status status = ((TestSuiteElement) element).getStatus();
			if (status.isError()) {
				return fTestRunnerPart.fSuiteErrorIcon;
			} else if (status.isOK()) {
				return fTestRunnerPart.fSuiteOkIcon;
			} else {
				throw new IllegalStateException(element.toString());
			}

		} else {
			throw new IllegalArgumentException(String.valueOf(element));
		}
	}

	public void setShowTime(boolean showTime) {
		fShowTime = showTime;
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}

}
