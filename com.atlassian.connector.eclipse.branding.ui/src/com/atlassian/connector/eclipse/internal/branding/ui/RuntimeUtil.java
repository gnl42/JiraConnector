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

package com.atlassian.connector.eclipse.internal.branding.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.commons.core.CoreUtil;

import java.util.Arrays;
import java.util.List;

public final class RuntimeUtil {
	private RuntimeUtil() {
	}

	public static boolean suppressConfigurationWizards() {
		final List<String> commandLineArgs = Arrays.asList(Platform.getCommandLineArgs());
		return commandLineArgs.contains("-testPluginName") || CoreUtil.TEST_MODE;
	}

}
