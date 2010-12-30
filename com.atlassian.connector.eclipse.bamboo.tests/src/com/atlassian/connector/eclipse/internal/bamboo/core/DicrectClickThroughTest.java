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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughUiPlugin;

import junit.framework.TestCase;

public class DicrectClickThroughTest extends TestCase {

	public void testDirectClickThroughPort() {
		String message = "DirectClickThrough port is set to " + DirectClickThroughUiPlugin.getDefault().getPortNumber();
		System.out.println(message);
		fail(message);
	}
}
