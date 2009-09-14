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

package com.atlassian.connector.eclipse.internal.core.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public abstract class JobWithStatus extends Job {

	private IStatus status;

	public JobWithStatus(String name) {
		super(name);
	}

	protected void setStatus(IStatus status) {
		this.status = status;
	}

	public IStatus getStatus() {
		return status;
	}

	@Override
	public abstract IStatus run(IProgressMonitor monitor);

}
