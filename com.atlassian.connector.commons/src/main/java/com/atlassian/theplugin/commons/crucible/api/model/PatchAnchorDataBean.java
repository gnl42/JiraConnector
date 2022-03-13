/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.theplugin.commons.crucible.api.model;

public class PatchAnchorDataBean implements PatchAnchorData {
	private String path;
	private String stripCount;
	private String repositoryName;

	public PatchAnchorDataBean(String repoName, String path, String stripCount) {
		this.repositoryName = repoName;
		this.path = path;
		this.stripCount = stripCount;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStripCount() {
		return stripCount;
	}

	public void setStripCount(String stripCount) {
		this.stripCount = stripCount;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
}
