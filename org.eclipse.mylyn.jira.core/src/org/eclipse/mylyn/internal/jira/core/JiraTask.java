/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.internal.tasks.core.AbstractTask;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
@Deprecated
public class JiraTask extends AbstractTask {

	private final String key = null;

	/**
	 * The handle is also the task's Jira url
	 */
	public JiraTask(String repositoryUrl, String id, String label) {
		super(repositoryUrl, id, label);
	}

	@Override
	public String getConnectorKind() {
		return JiraCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public String getTaskKey() {
		return key;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

}
