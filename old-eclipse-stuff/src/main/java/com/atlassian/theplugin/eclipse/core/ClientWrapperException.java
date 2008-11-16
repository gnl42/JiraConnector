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

package com.atlassian.theplugin.eclipse.core;


/**
 * Basic SVN client wrapper exception
 * 
 * @author Alexander Gurov
 */
public class ClientWrapperException extends Exception {
	private static final long serialVersionUID = 6066882107735517763L;
	
	protected boolean runtime;
	protected int errorId;

	public ClientWrapperException() {
		super();
		this.runtime = false;
	}

	public ClientWrapperException(String message) {
		super(message);
		this.runtime = false;
	}

	public ClientWrapperException(Throwable cause, boolean runtime) {
		super(cause);
		this.runtime = runtime;
	}

	public ClientWrapperException(String message, Throwable cause, boolean runtime) {
		super(message, cause);
		this.runtime = runtime;
	}
	
	public ClientWrapperException(String message, int errorId, Throwable cause, boolean runtime) {
		super(message, cause);
		this.runtime = runtime;
		this.errorId = errorId;
	}

	public boolean isRuntime() {
		return this.runtime;
	}
	
	public int getErrorId() {
		return this.errorId;
	}
	
}
