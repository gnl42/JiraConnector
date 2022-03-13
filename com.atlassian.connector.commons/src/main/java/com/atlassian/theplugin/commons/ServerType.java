/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons;

/**
 * Represents server types
 */
public enum ServerType {
	BAMBOO_SERVER("Bamboo Servers", "Bamboo", "http://www.atlassian.com/software/bamboo/"),
	CRUCIBLE_SERVER("Crucible/FishEye Servers", "Crucible/FishEye", "http://www.atlassian.com/software/crucible/"),
	JIRA_SERVER("JIRA Servers", "JIRA", "http://www.atlassian.com/software/jira/"),
	FISHEYE_SERVER("FishEye Servers", "FishEye", "http://www.atlassian.com/software/fisheye/"),
    JIRA_STUDIO_SERVER("JIRA Studio Servers", "JIRA Studio", "http://www.atlassian.com/studio/", true);
//    STASH_SERVER("Stash Servers", "STASH", "http://www.atlassian.com/software/stash/");
//
	private final String name;
	private String shortName;
	private final String infoUrl;
    private boolean pseudoServer;

    ServerType(final String name, final String shortName, final String infoUrl) {
        this(name, shortName, infoUrl, false);
	}

    ServerType(final String name, final String shortName, final String infoUrl, boolean pseudoServer) {
        this.name = name;
        this.shortName = shortName;
        this.infoUrl = infoUrl;
        this.pseudoServer = pseudoServer;
    }

    public String getShortName() {
		return shortName;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

    public boolean isPseudoServer() {
        return pseudoServer;
    }

    @Override
	public String toString() {
		return name;
	}
}
