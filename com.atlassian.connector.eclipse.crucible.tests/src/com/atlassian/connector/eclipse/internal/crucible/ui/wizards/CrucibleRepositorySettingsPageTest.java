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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewRepositoryWizard;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class CrucibleRepositorySettingsPageTest extends TestCase {

	public void testInvalidUrl() throws Exception {
		String url = "http://no such host/crucible";
		int messageType = IMessageProvider.ERROR;
		String message = "Malformed server URL: http://no such host/crucible";

		testValidateSettings(url, messageType, message);
	}

	public void testNotFound() throws Exception {
		String message = "https://studio.atlassian.com/dupa:HTTP 404 (Not Found)";
		testValidateSettings("https://studio.atlassian.com/dupa", IMessageProvider.ERROR, message);
	}

	private void testValidateSettings(String url, int messageType, String message) throws Exception {
		NewRepositoryWizard wizard = new NewRepositoryWizard(CrucibleCorePlugin.CONNECTOR_KIND);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		try {
			Field settingsPage = NewRepositoryWizard.class.getDeclaredField("settingsPage");
			settingsPage.setAccessible(true);
			CrucibleRepositorySettingsPage page = (CrucibleRepositorySettingsPage) settingsPage.get(wizard);

			page.createControl(shell);
			page.setVisible(true);

			page.setUrl(url);
			page.setUserId("user");
			page.setPassword("password");

			Method validateSettings = AbstractRepositorySettingsPage.class.getDeclaredMethod("validateSettings");
			validateSettings.setAccessible(true);
			validateSettings.invoke(page);

			assertNotNull(page.getMessage());
			assertEquals(messageType, page.getMessageType());
			assertEquals(message, page.getMessage());
		} finally {
			dialog.close();
		}
	}
}
