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

	public static final String OPEN_REPOSITORY_PROPERTIES_ACTION_LABEL = "Properties...";

	public static final String ADD_COMMENT_TO_BUILD_ACTION_LABEL = "Add Comment to Build...";

	public static final String ADD_LABEL_TO_BUILD_ACTION_LABEL = "Add Label to Build...";

	public static final String SHOW_TEST_RESULTS_ACTION_LABEL = "Show Test Results";

	public static final String OPEN_WITH_BROWSER_ACTION_LABEL = "Open with Browser";

	public static final String SHOW_BUILD_LOG_ACTION_LABEL = "Show Build Log";

	public static final String RUN_BUILD_ACTION_TOOLTIP = "Run Build on Server";

	public static final String RUN_BUILD_ACTION_LABEL = "Run Build";

	public static final String OPEN_BUILD_ACTION_LABEL = "Open";

	private BambooConstants() {
	}
}
