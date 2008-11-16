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
 * The exception will be never shown to user
 * 
 * @author Alexander Gurov
 */
public class HiddenException extends UnreportableException {
	private static final long serialVersionUID = -7093439079259787375L;

	public HiddenException() {
		super();
	}

	public HiddenException(String message) {
		super(message);
	}

	public HiddenException(Throwable cause) {
		super(cause);
	}

	public HiddenException(String message, Throwable cause) {
		super(message, cause);
	}

}
