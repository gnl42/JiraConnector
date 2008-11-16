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
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.ui.IWorkbenchPart;


/**
 * This wrapper provide ability to run ICancellableOperationWrapper scheduled 
 * 
 * @author Alexander Gurov
 */
public class SVNTeamOperationWrapper extends TeamOperation {
	
	protected ICancellableOperationWrapper operationWrapper;

	public SVNTeamOperationWrapper(IWorkbenchPart part, ICancellableOperationWrapper operationWrapper) {
		super(part);
		this.operationWrapper = operationWrapper;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		this.operationWrapper.run(monitor);
	}
	
	protected boolean canRunAsJob() {
		return true;
	}
	
	protected String getJobName() {
		return this.operationWrapper.getOperationName();
	}
	
}
