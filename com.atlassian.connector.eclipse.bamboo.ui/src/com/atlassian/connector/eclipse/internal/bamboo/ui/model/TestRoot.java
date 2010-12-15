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

package com.atlassian.connector.eclipse.internal.bamboo.ui.model;

import com.atlassian.theplugin.commons.bamboo.BuildDetails;

/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

public class TestRoot extends TestSuiteElement {

//	private final BuildDetails fSession;

	public TestRoot(String buildKey, BuildDetails session) {
		super(null, buildKey, 1);
//		fSession = session;
	}

	public TestRoot getRoot() {
		return this;
	}

//	public BuildDetails getTestRunSession() {
//		return fSession;
//	}
}
