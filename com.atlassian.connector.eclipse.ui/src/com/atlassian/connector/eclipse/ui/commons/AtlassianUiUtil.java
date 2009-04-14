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

package com.atlassian.connector.eclipse.ui.commons;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

/**
 * Provides utility methods for the Atlassian Eclipse COnnector
 * 
 * @author Thomas Ehrnhoefer
 */
public final class AtlassianUiUtil {

	private AtlassianUiUtil() {
	}

	public static boolean isAnimationsEnabled() {
		IPreferenceStore store = PlatformUI.getPreferenceStore();
		return store.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
	}
}
