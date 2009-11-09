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

/**
 * This is a class to encapsulate all of the constants used in the Bamboo connector
 * 
 * @author Thomas Ehrnhoefer
 */
public final class BambooConstants {

	public static final String BAMBOO_EDITOR_PAGE_ID = "com.atlassian.connector.eclipse.bamboo.build.editorpage";

	public static final Object FAMILY_REFRESH_OPERATION = new Object();

	public static final String PREFERENCE_REFRESH_INTERVAL = "prefs_refresh_interval";

	public static final String PREFERENCE_AUTO_REFRESH = "pref_auto_refresh";

	public static final int DEFAULT_REFRESH_INTERVAL = 15;

	public static final boolean DEFAULT_AUTO_REFRESH = true;

	public static final String PREFERENCE_PLAY_SOUND = "prefs_play_sound_build_failed";

	protected static final boolean DEFAULT_PLAY_SOUND = false;

	public static final String PREFERENCE_BUILD_SOUND = "pref_sound_build_failed";

	private BambooConstants() {
	}
}
