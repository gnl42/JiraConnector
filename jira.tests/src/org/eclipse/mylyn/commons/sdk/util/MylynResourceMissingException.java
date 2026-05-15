/*******************************************************************************
 * Copyright (c) 2025 George Lindholm
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html.
 *
 * Contributors:
 *      See git history
 *******************************************************************************/

package org.eclipse.mylyn.commons.sdk.util;

/**
 * FIXME: Should really be ResourceMissingException but that could break existing code. <br/>
 * Use to be abused junit.framework.AssertionFailedError
 *
 * @since 4.8
 */
public class MylynResourceMissingException extends AssertionError {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new MylynResourceMissingException with the specified detail message. A null message is replaced by an empty String.
	 *
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@code Throwable.getMessage()} method.
	 */
	public MylynResourceMissingException(final String message) {
		super(defaultString(message));
	}

	private static String defaultString(final String message) {
		return message == null ? "" : message; //$NON-NLS-1$
	}
}