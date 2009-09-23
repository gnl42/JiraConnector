/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.util;

import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tests.util.TestFixture;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

/**
 * @author Steffen Pingel
 */
public class JiraFixture extends TestFixture {

	public static JiraFixture ENTERPRISE_3_13_1 = new JiraFixture(JiraTestConstants.JIRA_3_13_1_URL, //
			"3.13.1", "333", "Enterprise");

	public static JiraFixture ENTERPRISE_3_13_1_BASIC_AUTH = new JiraFixture(JiraTestConstants.JIRA_3_13_1_URL, //
			"3.13.1", "333", "Enterprise/BasicAuth");

	public static JiraFixture ENTERPRISE_4_0_0 = new JiraFixture(JiraTestConstants.JIRA_4_0_0_URL, //
			"4.0.0-Beta2", "432", "Enterprise");

	public static final JiraFixture[] ALL = new JiraFixture[] { ENTERPRISE_3_13_1, ENTERPRISE_4_0_0 };

	public static JiraFixture DEFAULT = ENTERPRISE_3_13_1;

	private static JiraFixture current;

	public static JiraFixture current() {
		return current(DEFAULT);
	}

	public static JiraFixture current(JiraFixture fixture) {
		if (current == null) {
			current = fixture;
		}
		return current;
	}

	private final String buildNumber;

	private final String version;

	public JiraFixture(String url, String version, String buildNumber, String info) {
		super(JiraCorePlugin.CONNECTOR_KIND, url);
		this.version = version;
		this.buildNumber = buildNumber;
		setInfo("JIRA " + version + ((info.length() > 0) ? "/" + info : ""));
	}

	@Override
	public JiraFixture activate() {
		current = this;
		return this;
	}

	public JiraClient client() throws Exception {
		return client(PrivilegeLevel.USER);
	}

	public JiraClient client(PrivilegeLevel level) throws Exception {
		JiraClient client = new JiraClient(location(level));
		JiraTestUtil.refreshDetails(client);
		return client;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public JiraRepositoryConnector connector() {
		return (JiraRepositoryConnector) super.connector();
	}

}
