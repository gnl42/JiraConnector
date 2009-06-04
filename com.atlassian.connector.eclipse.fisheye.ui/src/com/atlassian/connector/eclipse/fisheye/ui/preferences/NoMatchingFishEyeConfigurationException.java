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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class NoMatchingFishEyeConfigurationException extends CoreException {

	private final String scmPath;

	public NoMatchingFishEyeConfigurationException(IStatus status, String scmPath) {
		super(status);
		this.scmPath = scmPath;
	}

	public String getScmPath() {
		return scmPath;
	}

}
