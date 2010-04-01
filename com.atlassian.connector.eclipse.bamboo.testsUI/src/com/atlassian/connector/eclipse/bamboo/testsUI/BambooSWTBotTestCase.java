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
package com.atlassian.connector.eclipse.bamboo.testsUI;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.mylyn.tests.util.TestUtil;
import org.eclipse.mylyn.tests.util.TestUtil.Credentials;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

/**
 * @author Jacek Jaroczynski
 *
 */
@SuppressWarnings("restriction")
public class BambooSWTBotTestCase extends TestCase {

	private static final String CREDENTIALS_PROPERTIES_FILE = "credentials.properties";
	
	protected static final String TEST_SEVER_INSTANCE = "https://gdansk.bamboo2.atlassian.com";
	protected static final Credentials CREDENTIALS = readCredentials();
	
	protected SWTWorkbenchBot bot = new SWTWorkbenchBot();

	public void setUp() {
		bot.viewByTitle("Welcome").close();
		SWTBotPreferences.TIMEOUT = 20000;
	}
	
	protected static Credentials readCredentials() {
		Properties properties = new Properties();
		try {
			File file = TestUtil.getFile(BambooSWTBotTestCase.class, CREDENTIALS_PROPERTIES_FILE);
			if (!file.exists()) {
				throw new AssertionFailedError("must define credentials in <plug-in dir>/credentials.properties");
			}
			properties.load(new FileInputStream(file));
		} catch (Exception e) {
			throw new AssertionFailedError("must define credentials in <plug-in dir>/credentials.properties");
		}

		return new Credentials(properties.getProperty("user"), properties.getProperty("pass"));
	}
}
