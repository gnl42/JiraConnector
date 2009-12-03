/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.team.ui.exceptions;

/**
 * <code>UnsupportedTeamProviderException</code> is thrown when the user tries to open a file using an unsupported team
 * provider.
 * 
 * @author thomas
 */
public class UnsupportedTeamProviderException extends RuntimeException {

	private static final long serialVersionUID = 4980403261986245835L;

	public UnsupportedTeamProviderException(String message) {
		super(message);
	}

	public UnsupportedTeamProviderException() {
		super();
	}

	public UnsupportedTeamProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedTeamProviderException(Throwable cause) {
		super(cause);
	}

}
