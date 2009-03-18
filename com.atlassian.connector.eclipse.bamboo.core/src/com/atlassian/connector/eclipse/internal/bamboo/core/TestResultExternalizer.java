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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.bamboo.TestResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds the junit model of the test results and writes it
 * 
 * @author Thomas Ehrnhoefer
 */
public class TestResultExternalizer {

	public static final String ATTRIBUTE_NAME = "name";

	public static final String ATTRIBUTE_CLASS_NAME = "classname";

	public static final String ATTRIBUTE_PROJECT = "project";

	public static final String ATTRIBUTE_TESTS = "tests";

	public static final String ATTRIBUTE_STARTED = "started";

	public static final String ATTRIBUTE_FAILURES = "failures";

	public static final String ATTRIBUTE_ERRORS = "errors";

	public static final String ATTRIBUTE_IGNORED = "ignored";

	public static final String ATTRIBUTE_TIME = "time";

	public static final String ELEMENT_TESTRUN = "testrun";

	public static final String ELEMENT_TESTSUITE = "testsuite";

	public static final String ELEMENT_TESTCASE = "testcase";

	public static final String ELEMENT_FAILURE = "failure";

	private final SaxTestResultsWriter writer = new SaxTestResultsWriter();

	public File writeApplicationsToXML(BambooBuild build, BuildDetails buildDetails, File file) throws CoreException {

		Map<String, TestSuite> testResults = prepareTestResults(build, buildDetails);
		int failed = buildDetails.getFailedTestDetails().size();
		int success = buildDetails.getSuccessfulTestDetails().size();

		if (testResults == null) {
			return null;
		}
		OutputStream stream = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			stream = new FileOutputStream(file);
			writer.setOutputStream(stream);
			writer.writeApplicationsToStream(testResults, build, failed, success);
			stream.flush();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, "Could not write: "
					+ file.getAbsolutePath(), e));
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					StatusHandler.log(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID,
							"Unable to terminate output stream to applications file.", e));
				}
			}
		}
		return file;
	}

	private Map<String, TestSuite> prepareTestResults(BambooBuild build, BuildDetails buildDetails) {
		Map<String, TestSuite> testSuites = new HashMap<String, TestSuite>();
		for (TestDetails test : buildDetails.getSuccessfulTestDetails()) {
			TestSuite suite = testSuites.get(test.getTestClassName());
			if (suite == null) {
				suite = new TestSuite(test.getTestClassName());
				testSuites.put(test.getTestClassName(), suite);
			}
			suite.addTest(test);
		}
		for (TestDetails test : buildDetails.getFailedTestDetails()) {
			TestSuite suite = testSuites.get(test.getTestClassName());
			if (suite == null) {
				suite = new TestSuite(test.getTestClassName());
				testSuites.put(test.getTestClassName(), suite);
			}
			suite.addTest(test);
		}
		return testSuites;
	}

	protected class TestSuite {
		private final String className;

		private double totalDuration;

		private int totalTests;

		private int totalFailed;

		private int totalSucceeded;

		private final Set<TestDetails> tests;

		public TestSuite(String className) {
			super();
			this.className = className;
			this.tests = new LinkedHashSet<TestDetails>();
		}

		public void addTest(TestDetails test) {
			tests.add(test);
			totalDuration += test.getTestDuration();
			totalTests++;
			if (test.getTestResult() == TestResult.TEST_SUCCEED) {
				totalSucceeded++;
			} else {
				totalFailed++;
			}
		}

		public double getTotalDuration() {
			return totalDuration;
		}

		public int getTotalTests() {
			return totalTests;
		}

		public int getFailedTests() {
			return totalFailed;
		}

		public int getSucceededTests() {
			return totalSucceeded;
		}

		public String getClassName() {
			return className;
		}

		public Set<TestDetails> getTests() {
			return tests;
		}
	}
}
