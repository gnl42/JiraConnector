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
import org.eclipse.core.runtime.NullProgressMonitor;

import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.util.ProgressMonitorUtility;

/**
 * This wrapper allows to run operations that do not modify workspace
 * 
 * @author Alexander Gurov
 */
public class DefaultCancellableOperationWrapper implements ICancellableOperationWrapper {
	protected IProgressMonitor attachedMonitor;
	protected IActionOperation operation;
	
	public DefaultCancellableOperationWrapper(IActionOperation operation) {
		this.operation = operation;
		this.attachedMonitor = new NullProgressMonitor();
	}
	
	public void setCancelled(boolean cancelled) {
		this.attachedMonitor.setCanceled(cancelled);
	}
	
	public boolean isCancelled() {
		return this.attachedMonitor.isCanceled();
	}
	
	public IActionOperation getOperation() {
		return this.operation;
	}
	
	public String getOperationName() {
		return this.operation.getOperationName();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.setCanceled(this.attachedMonitor.isCanceled());
		this.attachedMonitor = monitor;
		// wrap external monitor and make instance of SubProgressMonitorWithInfo
		ProgressMonitorUtility.doTaskExternal(this.operation, this.attachedMonitor, null);
	}

}
