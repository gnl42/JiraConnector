/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.utility;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Scheduled
 * 
 * @author Alexander Gurov
 */
public class ScheduledOperationWrapper extends Job {

	protected ICancellableOperationWrapper operationWrapper;

	public ScheduledOperationWrapper(
			ICancellableOperationWrapper operationWrapper) {
		super(operationWrapper.getOperationName());
		this.operationWrapper = operationWrapper;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			this.operationWrapper.run(monitor);
		} catch (InterruptedException e) {
			this.operationWrapper.setCancelled(true);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return Status.OK_STATUS; //this.operationWrapper.getOperation().getStatus
								// ()
	}

}
