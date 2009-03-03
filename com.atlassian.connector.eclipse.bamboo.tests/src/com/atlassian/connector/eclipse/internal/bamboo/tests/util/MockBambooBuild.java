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

package com.atlassian.connector.eclipse.internal.bamboo.tests.util;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;
import java.util.Set;

/**
 * Mock implementation of a BambooBuild
 * 
 * @author Thomas Ehrnhoefer
 */
public class MockBambooBuild implements BambooBuild {

	private final String key;

	private final String serverUrl;

	public MockBambooBuild(String key, String serverUrl) {
		this.key = key;
		this.serverUrl = serverUrl;
	}

	public Date getBuildCompletedDate() {
		// ignore
		return null;
	}

	public String getBuildDurationDescription() {
		// ignore
		return null;
	}

	public String getBuildKey() {
		return key;
	}

	public String getBuildName() {
		// ignore
		return null;
	}

	public int getBuildNumber() throws UnsupportedOperationException {
		// ignore
		return 0;
	}

	public String getBuildReason() {
		// ignore
		return null;
	}

	public String getBuildRelativeBuildDate() {
		// ignore
		return null;
	}

	public String getBuildResultUrl() {
		// ignore
		return null;
	}

	public Date getBuildStartedDate() {
		// ignore
		return null;
	}

	public String getBuildTestSummary() {
		// ignore
		return null;
	}

	public String getBuildUrl() {
		// ignore
		return null;
	}

	public Set<String> getCommiters() {
		// ignore
		return null;
	}

	public boolean getEnabled() {
		// ignore
		return false;
	}

	public String getErrorMessage() {
		// ignore
		return null;
	}

	public Date getPollingTime() {
		// ignore
		return null;
	}

	public String getProjectName() {
		// ignore
		return null;
	}

	public BambooServerCfg getServer() {
		// ignore
		return null;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public BuildStatus getStatus() {
		// ignore
		return null;
	}

	public int getTestsFailed() {
		// ignore
		return 0;
	}

	public int getTestsPassed() {
		// ignore
		return 0;
	}

	public boolean isMyBuild() {
		// ignore
		return false;
	}

	public boolean isValid() {
		// ignore
		return false;
	}

}
