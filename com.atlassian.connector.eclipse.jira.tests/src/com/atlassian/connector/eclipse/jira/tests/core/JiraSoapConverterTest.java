/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.core;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.service.soap.JiraSoapConverter;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteVersion;

public class JiraSoapConverterTest extends TestCase {

	public void testConvertVersions() {
		RemoteVersion[] versions = JiraSoapConverter.convert(new Version[] { new Version("11782"), new Version("11783") });
		assertNotNull(versions);
		assertEquals(2, versions.length);
		assertEquals("11782", versions[0].getId());
	}
}
