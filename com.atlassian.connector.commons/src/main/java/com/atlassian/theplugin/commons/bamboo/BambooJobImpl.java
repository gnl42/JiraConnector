/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.bamboo;

import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jacek Jaroczynski
 */
public class BambooJobImpl implements BambooJob {

	private final String key;
	private final String shortKey;

	private final String name;
	private final String shortName;

	private boolean enabled;
	private final List<TestDetails> successfulTests;
	private final List<TestDetails> failedTests;

	public BambooJobImpl(String key, String shortKey, String name, String shortName) {
		this.key = key;
		this.shortKey = shortKey;
		this.name = name;
		this.shortName = shortName;

        successfulTests = new ArrayList<TestDetails>();
		failedTests = new ArrayList<TestDetails>();
	}

	public void addFailedTest(TestDetailsInfo tInfo) {
		getFailedTests().add(tInfo);
	}

	public void addSuccessfulTest(TestDetailsInfo tInfo) {
		getSuccessfulTests().add(tInfo);
	}

	public String getKey() {
		return key;
	}

	public String getShortKey() {
		// key: PROJECTKEY-PLANKEY-JOBKEY
		// shortKey: JOBKEY
		return (shortKey != null && shortKey.length() > 0) ? shortKey : StringUtils.substringAfterLast(key, "-");
	}
	
    public boolean isEnabled() {
        return enabled;
    }

    public List<TestDetails> getSuccessfulTests() {
		return successfulTests;
	}

	public List<TestDetails> getFailedTests() {
		return failedTests;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}
}
