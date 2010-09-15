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
 *******************************************************************************/

import org.eclipse.core.runtime.Assert;

public class TestCaseElement extends TestElement implements ITestCaseElement {

	public TestCaseElement(TestSuiteElement parent, String testName) {
		super(parent, testName);
		Assert.isNotNull(parent);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jdt.junit.model.ITestCaseElement#getTestMethodName()
	 * @see org.eclipse.jdt.internal.junit.runner.MessageIds#TEST_IDENTIFIER_MESSAGE_FORMAT
	 * @see org.eclipse.jdt.internal.junit.runner.MessageIds#IGNORED_TEST_PREFIX
	 */
	public String getTestMethodName() {
		String testName = getTestName();
		int index = testName.indexOf('(');
		if (index > 0) {
			return testName.substring(0, index);
		}
		index = testName.indexOf('@');
		if (index > 0) {
			return testName.substring(0, index);
		}
		return testName;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jdt.junit.model.ITestCaseElement#getTestClassName()
	 */
	public String getTestClassName() {
		return getClassName();
	}

	public String toString() {
		return "TestCase: " + getTestClassName() + "." + getTestMethodName() + " : " + super.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
