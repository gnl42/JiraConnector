/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * Basic information about JIRA server
 *
 * @since v0.1
 */
public class ServerInfo {
	private final URI baseUri;
	private final String version;
	private final int buildNumber;
	private final DateTime buildDate;
	private final DateTime serverTime;
	private final int svnRevision;
	private final String serverTitle;

	public ServerInfo(URI baseUri, String version, int buildNumber, DateTime buildDate, DateTime serverTime, int svnRevision, String serverTitle) {
		this.baseUri = baseUri;
		this.version = version;
		this.buildNumber = buildNumber;
		this.buildDate = buildDate;
		this.serverTime = serverTime;
		this.svnRevision = svnRevision;
		this.serverTitle = serverTitle;
	}

	public URI getBaseUri() {
		return baseUri;
	}

	public String getVersion() {
		return version;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	public DateTime getBuildDate() {
		return buildDate;
	}

	public DateTime getServerTime() {
		return serverTime;
	}

	public int getSvnRevision() {
		return svnRevision;
	}

	public String getServerTitle() {
		return serverTitle;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(super.toString()).
				add("baseUri", baseUri).
				add("version", version).
				add("buildNumber", buildNumber).
				add("buildDate", buildDate).
				add("serverTime", serverTime).
				add("svnRevision", svnRevision).
				add("serverTitle", serverTitle).
				toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServerInfo) {
			ServerInfo that = (ServerInfo) obj;
			return Objects.equal(this.baseUri, that.baseUri)
					&& Objects.equal(this.version, that.version)
					&& Objects.equal(this.buildNumber, that.buildNumber)
					&& Objects.equal(this.buildDate, that.buildDate)
					&& Objects.equal(this.serverTime, that.serverTime)
					&& Objects.equal(this.svnRevision, that.svnRevision)
					&& Objects.equal(this.serverTitle, that.serverTitle);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(baseUri, version, buildNumber, buildDate, serverTime, svnRevision, serverTitle);
	}

}
