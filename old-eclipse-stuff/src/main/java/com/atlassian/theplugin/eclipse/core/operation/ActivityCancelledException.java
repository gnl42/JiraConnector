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



/**
 * Activity cancelled exception
 * 
 * @author Alexander Gurov
 */
public class ActivityCancelledException extends UnreportableException {
	private static final long serialVersionUID = 6390395981269341729L;
	
	public ActivityCancelledException() {
		super();
	}

	public ActivityCancelledException(String message) {
		super(message);
	}

	public ActivityCancelledException(Throwable cause) {
		super(cause);
	}

	public ActivityCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

}
