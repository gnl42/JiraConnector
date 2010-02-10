/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.osgi.util.NLS;

/**
 * @author Steffen Pingel
 */
public class Statistics {

	private long startTime;

	private final MultiStatus status;

	public Statistics() {
		status = new MultiStatus(JiraCorePlugin.ID_PLUGIN, 0, "", null); //$NON-NLS-1$
	}

	public MultiStatus getStatus() {
		return status;
	}

	public void mark() {
		startTime = System.currentTimeMillis();
	}

	public void record(String message) {
		long t = System.currentTimeMillis() - startTime;
		status.add(new Status(IStatus.INFO, JiraCorePlugin.ID_PLUGIN, NLS.bind(message, t + " ms"))); //$NON-NLS-1$
	}

}
