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

package me.glindholm.theplugin.commons.bamboo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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

    public BambooJobImpl(final String key, final String shortKey, final String name, final String shortName) {
        this.key = key;
        this.shortKey = shortKey;
        this.name = name;
        this.shortName = shortName;

        successfulTests = new ArrayList<>();
        failedTests = new ArrayList<>();
    }

    public void addFailedTest(final TestDetailsInfo tInfo) {
        getFailedTests().add(tInfo);
    }

    public void addSuccessfulTest(final TestDetailsInfo tInfo) {
        getSuccessfulTests().add(tInfo);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getShortKey() {
        // key: PROJECTKEY-PLANKEY-JOBKEY
        // shortKey: JOBKEY
        return shortKey != null && shortKey.length() > 0 ? shortKey : StringUtils.substringAfterLast(key, "-");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<TestDetails> getSuccessfulTests() {
        return successfulTests;
    }

    @Override
    public List<TestDetails> getFailedTests() {
        return failedTests;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BambooJobImpl [key=").append(key).append(", shortKey=").append(shortKey).append(", name=").append(name).append(", shortName=")
                .append(shortName).append(", enabled=").append(enabled).append(", successfulTests=").append(successfulTests).append(", failedTests=")
                .append(failedTests).append("]");
        return builder.toString();
    }
}
