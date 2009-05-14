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
import com.atlassian.theplugin.commons.remoteapi.ServerData;

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
		return null;
	}

	public String getDurationDescription() {
		return null;
	}

	public String getPlanKey() {
		return key;
	}

	public String getPlanName() {
		return null;
	}

	public int getNumber() throws UnsupportedOperationException {
		return 0;
	}

	public String getReason() {
		// ignore
		return null;
	}

	public String getRelativeBuildDate() {
		return null;
	}

	public String getResultUrl() {
		return null;
	}

	public Date getStartDate() {
		return null;
	}

	public String getTestSummary() {
		return null;
	}

	public String getBuildUrl() {
		return null;
	}

	public Set<String> getCommiters() {
		return null;
	}

	public boolean getEnabled() {
		return false;
	}

	public String getErrorMessage() {
		return null;
	}

	public Date getPollingTime() {
		return null;
	}

	public String getProjectName() {
		return null;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public BuildStatus getStatus() {
		return null;
	}

	public int getTestsFailed() {
		return 0;
	}

	public int getTestsPassed() {
		return 0;
	}

	public boolean isMyBuild() {
		return false;
	}

	public boolean isValid() {
		return false;
	}

	public ServerData getServer() {
		return null;
	}

	public Throwable getException() {
		return null;
	}

}
