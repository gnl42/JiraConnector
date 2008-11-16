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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * Action operation interface
 * 
 * @author Alexander Gurov
 */
public interface IActionOperation {
	public static final int OK = 0;
	public static final int ERROR = 1;
	public static final int NOTEXECUTED = 2;
	
	public IActionOperation run(IProgressMonitor monitor);
	
	public IStatus getStatus();
	
	public int getExecutionState();
	
	public String getOperationName();
	
	public String getId();
	
	public ISchedulingRule getSchedulingRule();
	
	public void setConsoleStream(IConsoleStream stream);
	
	public IConsoleStream getConsoleStream();
}
