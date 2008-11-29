/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;

/**
 * Bridge between Mylyn and the ACC API's
 * 
 * @author Shawn Minto
 */
public class CrucibleClient {

	private final CrucibleClientData clientData;

	private final AbstractWebLocation location;

	public CrucibleClient(AbstractWebLocation location, CrucibleClientData data) {
		this.location = location;
		this.clientData = data;
	}

	public void validate(IProgressMonitor monitor) {
		// TODO implement this to check that the credentials and urls, etc are valid
	}
}
