/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import me.glindholm.connector.eclipse.jira.tests.client.JiraClientTest;
import me.glindholm.connector.eclipse.jira.tests.core.FilterDefinitionConverterTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraClientCacheTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraCommentDateComparatorTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraRemoteMessageExceptionTest;
import me.glindholm.connector.eclipse.jira.tests.core.JiraTimeFormatTest;
import me.glindholm.connector.eclipse.jira.tests.model.ComponentFilterTest;
import me.glindholm.connector.eclipse.jira.tests.model.JiraVersionTest;
import me.glindholm.connector.eclipse.jira.tests.model.VersionFilterTest;
import me.glindholm.connector.eclipse.jira.tests.ui.JiraUiUtilTest;
import me.glindholm.connector.eclipse.jira.tests.ui.WdhmUtilTest;

/**
 * @author Steffen Pingel
 */
@Suite
@SelectClasses({ JiraTimeFormatTest.class, FilterDefinitionConverterTest.class,
	JiraVersionTest.class, JiraClientCacheTest.class, WdhmUtilTest.class,
	VersionFilterTest.class, ComponentFilterTest.class, JiraCommentDateComparatorTest.class,
	JiraRemoteMessageExceptionTest.class, JiraUiUtilTest.class,
	// Need fixture
	JiraClientTest.class})

public class AllJiraHeadlessStandaloneTests {
}
