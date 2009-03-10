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

import com.atlassian.connector.eclipse.internal.bamboo.core.TestResultExternalizer.TestSuite;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.bamboo.TestResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Thomas Ehrnhoefer
 */
public class SaxTestResultsWriter {
	private OutputStream outputStream;

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void writeApplicationsToStream(Map<String, TestSuite> testResults, BambooBuild build, int failed, int success)
			throws IOException {
		if (outputStream == null) {
			IOException ioe = new IOException("OutputStream not set");
			throw ioe;
		}

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new SAXSource(new TestResultsWriter(), new TestResultsInputSource(testResults, build,
					failed, success)), new StreamResult(outputStream));
		} catch (TransformerException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, "could not write repositories", e));
			throw new IOException(e.getMessage());
		}

	}

	private static class TestResultsInputSource extends InputSource {

		private final Map<String, TestSuite> testResults;

		private final BambooBuild build;

		private final int failed;

		private final int success;

		public TestResultsInputSource(Map<String, TestSuite> testResults, BambooBuild build, int failed, int success) {
			this.build = build;
			this.testResults = testResults;
			this.failed = failed;
			this.success = success;
		}

		public Map<String, TestSuite> getTestResults() {
			return this.testResults;
		}

		public BambooBuild getBuild() {
			return build;
		}

		public int getFailed() {
			return failed;
		}

		public int getSuccess() {
			return success;
		}

	}

	private static class TestResultsWriter implements XMLReader {

		private ContentHandler handler;

		private ErrorHandler errorHandler;

		public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
			return false;
		}

		public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

		}

		public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
			return null;
		}

		public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		}

		public void setEntityResolver(EntityResolver resolver) {
		}

		public EntityResolver getEntityResolver() {
			return null;
		}

		public void setDTDHandler(DTDHandler dtdHandler) {
		}

		public DTDHandler getDTDHandler() {
			return null;
		}

		public void setContentHandler(ContentHandler dtdHandler) {
			this.handler = dtdHandler;

		}

		public ContentHandler getContentHandler() {
			return handler;
		}

		public void setErrorHandler(ErrorHandler dtdHandler) {
			this.errorHandler = dtdHandler;

		}

		public ErrorHandler getErrorHandler() {
			return errorHandler;
		}

		public void parse(InputSource input) throws IOException, SAXException {
			if (!(input instanceof TestResultsInputSource)) {
				throw new SAXException("Can only parse writable input sources");
			}

			Map<String, TestSuite> testSuites = ((TestResultsInputSource) input).getTestResults();
			BambooBuild build = ((TestResultsInputSource) input).getBuild();
			int failed = ((TestResultsInputSource) input).getFailed();
			int success = ((TestResultsInputSource) input).getSuccess();

			handler.startDocument();

			AttributesImpl rootAttributes = new AttributesImpl();
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_NAME,
					TestResultExternalizer.ATTRIBUTE_NAME, "", "{bamboo imported} " + build.getPlanKey() + "-"
							+ build.getNumber());
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_PROJECT,
					TestResultExternalizer.ATTRIBUTE_PROJECT, "", "{bamboo imported} " + build.getPlanKey() + "-"
							+ build.getNumber());
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_TESTS,
					TestResultExternalizer.ATTRIBUTE_TESTS, "", String.valueOf(failed + success));
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_STARTED,
					TestResultExternalizer.ATTRIBUTE_STARTED, "", String.valueOf(failed + success));
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_FAILURES,
					TestResultExternalizer.ATTRIBUTE_FAILURES, "", String.valueOf(failed));
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_ERRORS,
					TestResultExternalizer.ATTRIBUTE_ERRORS, "", "0");
			rootAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_IGNORED,
					TestResultExternalizer.ATTRIBUTE_IGNORED, "", "0");

			handler.startElement("", TestResultExternalizer.ELEMENT_TESTRUN, TestResultExternalizer.ELEMENT_TESTRUN,
					rootAttributes);

			for (TestSuite testsuite : testSuites.values()) {
				//add test suite	
				AttributesImpl suiteAttributes = new AttributesImpl();
				suiteAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_NAME,
						TestResultExternalizer.ATTRIBUTE_NAME, "", testsuite.getClassName());
				suiteAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_TIME,
						TestResultExternalizer.ATTRIBUTE_TIME, "", String.valueOf(testsuite.getTotalDuration()));

				handler.startElement("", TestResultExternalizer.ELEMENT_TESTSUITE,
						TestResultExternalizer.ELEMENT_TESTSUITE, suiteAttributes);
				//add test cases
				for (TestDetails test : testsuite.getTests()) {
					AttributesImpl testAttributes = new AttributesImpl();
					testAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_NAME,
							TestResultExternalizer.ATTRIBUTE_NAME, "", test.getTestMethodName());
					testAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_CLASS_NAME,
							TestResultExternalizer.ATTRIBUTE_CLASS_NAME, "", test.getTestClassName());
					testAttributes.addAttribute("", TestResultExternalizer.ATTRIBUTE_TIME,
							TestResultExternalizer.ATTRIBUTE_TIME, "", String.valueOf(test.getTestDuration()));
					handler.startElement("", TestResultExternalizer.ELEMENT_TESTCASE,
							TestResultExternalizer.ELEMENT_TESTCASE, testAttributes);
					//add failure if there is one
					if (test.getTestResult() == TestResult.TEST_FAILED) {
						handler.startElement("", TestResultExternalizer.ELEMENT_FAILURE,
								TestResultExternalizer.ELEMENT_FAILURE, new AttributesImpl());
						handler.characters(test.getErrors().toCharArray(), 0, test.getErrors().length());
						handler.endElement("", TestResultExternalizer.ELEMENT_FAILURE,
								TestResultExternalizer.ELEMENT_FAILURE);
					}
					handler.endElement("", TestResultExternalizer.ELEMENT_TESTCASE,
							TestResultExternalizer.ELEMENT_TESTCASE);
				}
				//close test suite
				handler.endElement("", TestResultExternalizer.ELEMENT_TESTSUITE,
						TestResultExternalizer.ELEMENT_TESTSUITE);
			}
			handler.endElement("", TestResultExternalizer.ELEMENT_TESTRUN, TestResultExternalizer.ELEMENT_TESTRUN);

			handler.endDocument();
		}

		public void parse(String systemId) throws IOException, SAXException {
			throw new SAXException("Can only parse writable input sources");
		}

	}
}
