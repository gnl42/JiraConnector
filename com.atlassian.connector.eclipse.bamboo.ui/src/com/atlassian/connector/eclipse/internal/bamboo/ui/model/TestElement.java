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

package com.atlassian.connector.eclipse.internal.bamboo.ui.model;

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

import org.eclipse.core.runtime.Assert;

public abstract class TestElement implements ITestElement {
	public final static class Status {
		public static final Status ERROR = new Status("ERROR"); //$NON-NLS-1$

		public static final Status OK = new Status("OK"); //$NON-NLS-1$

		private final String fName;

		private Status(String name) {
			fName = name;
		}

		public String toString() {
			return fName;
		}

		/* error state predicates */

		public boolean isOK() {
			return this == OK;
		}

		public boolean isError() {
			return this == ERROR;
		}

		public static Status combineStatus(Status one, Status two) {
			return combineError(one, two);
		}

		private static Status combineError(Status one, Status two) {
			if (one.isError() || two.isError()) {
				return ERROR;
			} else {
				return OK;
			}
		}

		public Result convertToResult() {
			if (isError()) {
				return Result.ERROR;
			}
			return Result.OK;
		}

	}

	private final TestSuiteElement fParent;

	private String fTestName;

	private Status fStatus;

	private String fTrace;

	private String fExpected;

	private String fActual;

	/**
	 * Running time in seconds. Contents depend on the current {@link #getProgressState()}:
	 * <ul>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#NOT_STARTED}: {@link Double#NaN}</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#RUNNING}: negated start time</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#STOPPED}: elapsed time</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#COMPLETED}: elapsed time</li>
	 * </ul>
	 */
	/* default */double fTime = Double.NaN;

	/**
	 * @param parent
	 *            the parent, can be <code>null</code>
	 * @param id
	 *            the test id
	 * @param testName
	 *            the test name
	 */
	public TestElement(TestSuiteElement parent, String testName) {
		Assert.isNotNull(testName);
		fParent = parent;
		fTestName = testName;
		fStatus = Status.OK;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.ITestElement#getTestResult()
	 */
	public Result getTestResult(boolean includeChildren) {
		return getStatus().convertToResult();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.model.ITestElement#getFailureTrace()
	 */
	public FailureTrace getFailureTrace() {
		Result testResult = getTestResult(false);
		if (testResult == Result.ERROR || testResult == Result.FAILURE) {
			return new FailureTrace(fTrace, fExpected, fActual);
		}
		return null;
	}

	/**
	 * @return the parent suite, or <code>null</code> for the root
	 */
	public TestSuiteElement getParent() {
		return fParent;
	}

	public String getTestName() {
		return fTestName;
	}

	public void setName(String name) {
		fTestName = name;
	}

	public void setStatus(Status status) {
		fStatus = status;
		TestSuiteElement parent = getParent();
		if (parent != null) {
			parent.childChangedStatus(this, status);
		}
	}

	public void setStatus(Status status, String trace, String expected, String actual) {
		if (trace != null && fTrace != null) {
			//don't overwrite first trace if same test run logs multiple errors
			fTrace = fTrace + trace;
		} else {
			fTrace = trace;
			fExpected = expected;
			fActual = actual;
		}
		setStatus(status);
	}

	public Status getStatus() {
		return fStatus;
	}

	public String getTrace() {
		return fTrace;
	}

	public String getExpected() {
		return fExpected;
	}

	public String getActual() {
		return fActual;
	}

	public boolean isComparisonFailure() {
		return fExpected != null && fActual != null;
	}

	/**
	 * @return return the class name
	 * @see org.eclipse.jdt.internal.junit.runner.ITestIdentifier#getName()
	 * @see org.eclipse.jdt.internal.junit.runner.MessageIds#TEST_IDENTIFIER_MESSAGE_FORMAT
	 */
	public String getClassName() {
		return extractClassName(getTestName());
	}

	private static String extractClassName(String testNameString) {
		testNameString = extractRawClassName(testNameString);
		testNameString = testNameString.replace('$', '.'); // see bug 178503
		return testNameString;
	}

	public static String extractRawClassName(String testNameString) {
		int index = testNameString.indexOf('(');
		if (index < 0) {
			return testNameString;
		}
		testNameString = testNameString.substring(index + 1);
		testNameString = testNameString.substring(0, testNameString.indexOf(')'));
		return testNameString;
	}

	public TestRoot getRoot() {
		return getParent().getRoot();
	}

	public void setElapsedTimeInSeconds(double time) {
		fTime = time;
	}

	public double getElapsedTimeInSeconds() {
		if (Double.isNaN(fTime) || fTime < 0.0d) {
			return Double.NaN;
		}

		return fTime;
	}

}
