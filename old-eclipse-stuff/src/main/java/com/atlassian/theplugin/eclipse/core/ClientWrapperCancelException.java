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
 * SVN client wrapper cancel exception
 * 
 * @author Alexander Gurov
 */
public class ClientWrapperCancelException extends ClientWrapperException {
	private static final long serialVersionUID = -1431358791852025035L;

	public ClientWrapperCancelException() {
		super();
	}

	public ClientWrapperCancelException(String message) {
		super(message);
	}

	public ClientWrapperCancelException(Throwable cause) {
		super(cause, false);
	}

	public ClientWrapperCancelException(String message, Throwable cause) {
		super(message, cause, false);
	}

}
