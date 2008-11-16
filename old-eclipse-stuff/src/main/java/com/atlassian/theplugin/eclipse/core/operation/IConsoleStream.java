/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.core.operation;

/**
 * Operation output console stream
 * 
 * @author Alexander Gurov
 */
public interface IConsoleStream {
	public static final int LEVEL_CMD = 0;
	public static final int LEVEL_OK = 1;
	public static final int LEVEL_WARNING = 2;
	public static final int LEVEL_ERROR = 3;
	
	public void markStart(String data);
	public void write(int severity, String data);
	public void markEnd();
	public void markCancelled();
	
	public void doComplexWrite(Runnable runnable);
}
