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

	public Date getCompletionDate() {
		// ignore
		return null;
	}

	public String getDurationDescription() {
		// ignore
		return null;
	}

	public String getPlanKey() {
		return key;
	}

	public String getPlanName() {
		// ignore
		return null;
	}

	public int getNumber() throws UnsupportedOperationException {
		// ignore
		return 0;
	}

	public String getReason() {
		// ignore
		return null;
	}

	public String getRelativeBuildDate() {
		// ignore
		return null;
	}

	public String getResultUrl() {
		// ignore
		return null;
	}

	public Date getStartDate() {
		// ignore
		return null;
	}

	public String getTestSummary() {
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
