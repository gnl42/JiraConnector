/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.tasks.core.AbstractRepositoryQuery;

/**
 * A JiraRepositoryQuery represents a server-side query for Jira repository.
 * 
 * @author Mik Kersten
 * @since 3.0
 */
public class JiraRepositoryQuery extends AbstractRepositoryQuery {

	protected NamedFilter filter = null;

	public JiraRepositoryQuery(String repositoryUrl, NamedFilter filter) {
		super(filter.getName());
		this.filter = filter;
		super.repositoryUrl = repositoryUrl;
		setUrl(repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&requestId=" + filter.getId());
	}

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	public NamedFilter getNamedFilter() {
		return filter;
	}
}
