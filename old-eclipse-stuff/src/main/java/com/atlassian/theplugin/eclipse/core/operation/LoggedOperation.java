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

package com.atlassian.theplugin.eclipse.core.operation;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.atlassian.theplugin.eclipse.core.ClientWrapperCancelException;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Logged operation allow us to safelly write to log and show error messages
 * 
 * @author Alexander Gurov
 */
public class LoggedOperation implements IActionOperation {
	protected IActionOperation op;

	public LoggedOperation(IActionOperation op) {
		this.op = op;
	}

	public final IActionOperation run(IProgressMonitor monitor) {
		IStatus status = this.op.run(monitor).getStatus();
		if (status.getSeverity() != IStatus.OK) {
			this.handleError(status);
		}
		return this.op;
	}
	
	public IConsoleStream getConsoleStream() {
		return this.op.getConsoleStream();
	}
	
	public void setConsoleStream(IConsoleStream stream) {
		this.op.setConsoleStream(stream);
	}

	public ISchedulingRule getSchedulingRule() {
		return this.op.getSchedulingRule();
	}
	
	public String getOperationName() {
		return this.op.getOperationName();
	}
	
	public String getId() {
		return this.op.getId();
	}
	
	public final IStatus getStatus() {
		return this.op.getStatus();
	}
	
	public int getExecutionState() {
		return this.op.getExecutionState();
	}
	
	public static void reportError(String where, Throwable t) {
		String errMessage = Activator.getDefault().getResource("Operation.Error.LogHeader");
	    MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, MessageFormat.format(errMessage, new Object[] {where}), null);
		Status st = 
			new Status(
					IStatus.ERROR, 
					Activator.PLUGIN_ID, 
					IStatus.OK, 
					status.getMessage() + ": " + t.getMessage(), 
					t);
		status.merge(st);
		LoggedOperation.logError(status);
	}
	
	protected void handleError(IStatus errorStatus) {
		if (!errorStatus.isMultiStatus()) {
			Throwable ex = errorStatus.getException();
			if (!(ex instanceof ClientWrapperCancelException) && !(ex instanceof ActivityCancelledException)) {
				LoggedOperation.logError(errorStatus);
			}
			return;
        }
		
		IStatus []children = errorStatus.getChildren();
		ArrayList<IStatus> statusesWithoutCancel = new ArrayList<IStatus>(); 
        for (int i = 0; i < children.length; i++) {
            Throwable exception = children[i].getException();
        	if (!(exception instanceof ClientWrapperCancelException) && !(exception instanceof ActivityCancelledException)) {
        		statusesWithoutCancel.add(children[i]);
            }
        }
        if (statusesWithoutCancel.size() > 0) {
		    IStatus newStatus = new MultiStatus(errorStatus.getPlugin(), 
		    		errorStatus.getCode(), 
		    		(IStatus[]) statusesWithoutCancel.toArray(new IStatus[statusesWithoutCancel.size()]),
		    		errorStatus.getMessage(),
		    		errorStatus.getException());
		    LoggedOperation.logError(newStatus);
        }
	}
	
	protected static void logError(IStatus errorStatus) {
		Activator.getDefault().getLog().log(errorStatus);
	}

}
