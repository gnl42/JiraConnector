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

package com.atlassian.connector.eclipse.team.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class Startup implements IStartup {

	public void earlyStartup() {
		Bundle javaTests = Platform.getBundle("org.eclipse.mylyn.java.tests");
		if (javaTests.getState() != Bundle.ACTIVE) {
			try {
				javaTests.start();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}
	}

}
