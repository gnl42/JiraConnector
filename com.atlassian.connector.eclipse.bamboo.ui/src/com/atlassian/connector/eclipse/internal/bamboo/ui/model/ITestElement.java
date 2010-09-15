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
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

/**
 * Common protocol for test elements. This set consists of {@link ITestCaseElement} , {@link ITestSuiteElement} and
 * {@link ITestRunSession}
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * 
 * @since 3.3
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITestElement {

	/**
	 * Result states of a test.
	 */
	public static final class Result {
		/** state that describes that the test result is undefined */
		public static final Result UNDEFINED = new Result("Undefined"); //$NON-NLS-1$

		/** state that describes that the test result is 'OK' */
		public static final Result OK = new Result("OK"); //$NON-NLS-1$

		/** state that describes that the test result is 'Error' */
		public static final Result ERROR = new Result("Error"); //$NON-NLS-1$

		/** state that describes that the test result is 'Failure' */
		public static final Result FAILURE = new Result("Failure"); //$NON-NLS-1$

		/** state that describes that the test result is 'Ignored' */
		public static final Result IGNORED = new Result("Ignored"); //$NON-NLS-1$

		private final String fName;

		private Result(String name) {
			fName = name;
		}

		public String toString() {
			return fName;
		}
	}

	/**
	 * A failure trace of a test.
	 * 
	 * This class is not intended to be instantiated or extended by clients.
	 */
	public static final class FailureTrace {
		private final String fActual;

		private final String fExpected;

		private final String fTrace;

		public FailureTrace(String trace, String expected, String actual) {
			fActual = actual;
			fExpected = expected;
			fTrace = trace;
		}

		/**
		 * Returns the failure stack trace.
		 * 
		 * @return the failure stack trace
		 */
		public String getTrace() {
			return fTrace;
		}

		/**
		 * Returns the expected result or <code>null</code> if the trace is not a comparison failure.
		 * 
		 * @return the expected result or <code>null</code> if the trace is not a comparison failure.
		 */
		public String getExpected() {
			return fExpected;
		}

		/**
		 * Returns the actual result or <code>null</code> if the trace is not a comparison failure.
		 * 
		 * @return the actual result or <code>null</code> if the trace is not a comparison failure.
		 */
		public String getActual() {
			return fActual;
		}
	}

	/**
	 * Returns the result of the test element.
	 * <dl>
	 * <li>{@link ITestElement.Result#UNDEFINED}: the result is not yet evaluated</li>
	 * <li>{@link ITestElement.Result#OK}: the test has succeeded</li>
	 * <li>{@link ITestElement.Result#ERROR}: the test has returned an error</li>
	 * <li>{@link ITestElement.Result#FAILURE}: the test has returned an failure</li>
	 * <li>{@link ITestElement.Result#IGNORED}: the test has been ignored (skipped)</li>
	 * </dl>
	 * 
	 * @param includeChildren
	 *            if <code>true</code>, the returned result is the combined result of the test and its children (if it
	 *            has any). If <code>false</code>, only the test's result is returned.
	 * 
	 * @return returns one of {@link ITestElement.Result#UNDEFINED}, {@link ITestElement.Result#OK},
	 *         {@link ITestElement.Result#ERROR}, {@link ITestElement.Result#FAILURE} or
	 *         {@link ITestElement.Result#IGNORED}. Clients should also prepare for other, new values.
	 */
	public Result getTestResult(boolean includeChildren);

	/**
	 * Returns the failure trace of this test element or <code>null</code> if the test has not resulted in an error or
	 * failure.
	 * 
	 * @return the failure trace of this test or <code>null</code>.
	 */
	public FailureTrace getFailureTrace();

	/**
	 * Returns the estimated total time elapsed in seconds while executing this test element. The total time for a test
	 * suite includes the time used for all tests in that suite. The total time for a test session includes the time
	 * used for all tests in that session.
	 * <p>
	 * <strong>NOTE</strong>: The elapsed time is only valid for {@link ITestElement.ProgressState#COMPLETED} test
	 * elements.
	 * </p>
	 * 
	 * @return total execution time for the test element in seconds, or {@link Double#NaN}</code> if the state of the
	 *         element is not {@link ITestElement.ProgressState#COMPLETED}
	 * 
	 * @since 3.4
	 */
	public double getElapsedTimeInSeconds();

}
