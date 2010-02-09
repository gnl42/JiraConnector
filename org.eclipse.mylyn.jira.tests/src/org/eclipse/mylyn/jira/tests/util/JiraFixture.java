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

import org.eclipse.mylyn.internal.jira.core.JiraClientFactory;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tests.util.TestFixture;
import org.eclipse.mylyn.tests.util.TestUtil.PrivilegeLevel;

/**
 * @author Steffen Pingel
 */
public class JiraFixture extends TestFixture {

	public static final String SERVER = System.getProperty("mylyn.jira.server", "ecit.atlassian.com");

	private static final String getServerUrl(String version) {
		return "http://" + SERVER + "/" + version;
	}

	public static JiraFixture ENTERPRISE_3_13 = new JiraFixture(getServerUrl("jira-enterprise-3.13.5"), //
			"3.13.5", "360", "Enterprise");

	public static JiraFixture ENTERPRISE_3_13_BASIC_AUTH = new JiraFixture(getServerUrl("jira-basic-auth"), //
			"3.13.5", "360", "Enterprise/BasicAuth");

	public static JiraFixture ENTERPRISE_4_0 = new JiraFixture(getServerUrl("jira-enterprise-4.0"), //
			"4.0", "466", "Enterprise");

	public static final JiraFixture[] ALL = new JiraFixture[] { ENTERPRISE_3_13, ENTERPRISE_4_0 };

	public static JiraFixture DEFAULT = ENTERPRISE_3_13;

//	public static JiraFixture DEFAULT = ENTERPRISE_4_0;

	private static JiraFixture current;

	public static JiraFixture current() {
		return current(DEFAULT);
	}

	public static JiraFixture current(JiraFixture fixture) {
		if (current == null) {
			fixture.activate();
		}
		return current;
	}

	private final String buildNumber;

	private final String version;

	public JiraFixture(String url, String version, String buildNumber, String info) {
		super(JiraCorePlugin.CONNECTOR_KIND, url);
		this.version = version;
		this.buildNumber = buildNumber;
		setInfo("JIRA", version, info);
	}

	@Override
	public JiraFixture activate() {
		current = this;
		setUpFramework();
		return this;
	}

	@Override
	protected TestFixture getDefault() {
		return DEFAULT;
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

	@Override
	protected void resetRepositories() {
		// TODO bug 184806 need to manually remove stale clients
		JiraClientFactory.getDefault().clearClients();
	}

}
