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

package com.atlassian.connector.eclipse.internal.jira.core.service;

import java.io.Serializable;

/**
 * @author Jacek Jaroczynski
 */
public class LocalIssueData implements Serializable {

	private static final long serialVersionUID = 1L;

	private long loggedActivityTime;

	public long getLoggedActivityTime() {
		return loggedActivityTime;
	}

	public void setLoggedActivityTime(long elapsedTime) {
		this.loggedActivityTime = elapsedTime;
	}

}
