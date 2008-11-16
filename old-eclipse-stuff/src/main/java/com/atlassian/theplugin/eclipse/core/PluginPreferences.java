/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Dann Martens - [patch] Text decorations 'ascendant' variable
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.core;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;

/**
 * SVN Team plugin preference names
 * 
 * @author Alexander Gurov 
 */
public final class PluginPreferences {
	public static final String DECORATION_BASE = "preference.decoration.";
	public static final String REPOSITORY_BASE = "preference.repository.";
	public static final String SYNCHRONIZE_BASE = "preference.synchronize.";
	public static final String HISTORY_BASE = "preference.history.";
	public static final String PROPERTIES_BASE = "preference.properties.";
	public static final String MAILREPORTER_BASE = "preference.mailreporter.";
	public static final String COMMENT_TEMPLATES_BASE = "preference.templates";
	public static final String COMMIT_DIALOG_BASE = "preference.commitDialog.";
	public static final String MERGE_BASE = "preference.merge.";
	public static final String CHECKOUT_BASE = "preference.checkout.";
	public static final String CONSOLE_BASE = "preference.console.";
	public static final String KEYWORDS_BASE = "preference.keywords.";
	public static final String SHARE_BASE = "preference.share.";
	public static final String CORE_BASE = "preference.core.";
	public static final String ANNOTATE_BASE = "preference.annotate.";
	public static final String TABLE_SORTING_BASE = "preference.sorting.";
	public static final String AUTO_PROPERTIES_BASE = "preference.autoproperties";
	public static final String RESOURCE_SELECTION_BASE = "preference.resourceSelection";
	
	public static final String ANNOTATE_CHANGE_PERSPECTIVE_NAME = "changePerspective";
	public static final String ANNOTATE_PERSPECTIVE_NAME = "perspective";
	public static final String ANNOTATE_USE_QUICK_DIFF_NAME = "useQuickDiff";
	public static final String ANNOTATE_USE_ONE_RGB_NAME = "useOneRGB";
	public static final String ANNOTATE_RGB_BASE_NAME = "rgbBase";
	
	public static final int ANNOTATE_DEFAULT_VIEW = 0;
	public static final int ANNOTATE_QUICK_DIFF_VIEW = 1;
	public static final int ANNOTATE_PROMPT_VIEW = 2;
	
	public static final int ANNOTATE_DEFAULT_PERSPECTIVE = 0;
	public static final int ANNOTATE_CURRENT_PERSPECTIVE = 1;
	public static final int ANNOTATE_PROMPT_PERSPECTIVE = 2;
	
	public static final boolean ANNOTATE_USE_ONE_RGB_DEFAULT = true;
	public static final RGB ANNOTATE_RGB_BASE_DEFAULT = new RGB(186, 186, 186);
	
	public static final String CONSOLE_AUTOSHOW_TYPE_NAME = "autoshow";
	public static final String CONSOLE_ENABLED_NAME = "enabled";
	public static final String CONSOLE_HYPERLINKS_ENABLED_NAME = "hyperlinksEnabled";
	public static final String CONSOLE_FONT_NAME = "font";
	public static final String CONSOLE_WRAP_ENABLED_NAME = "wrapEnabled";
	public static final String CONSOLE_WRAP_WIDTH_NAME = "wrapWidth";
	public static final String CONSOLE_LIMIT_ENABLED_NAME = "limitEnabled";
	public static final String CONSOLE_LIMIT_VALUE_NAME = "limitRange";
	
	public static final String CORE_SVNCLIENT_NAME = "svnclient";
	
	public static final String CONSOLE_ERR_COLOR_NAME = "error";
	public static final String CONSOLE_WRN_COLOR_NAME = "warning";
	public static final String CONSOLE_OK_COLOR_NAME = "ok";
	public static final String CONSOLE_CMD_COLOR_NAME = "command";
	
	public static final int CONSOLE_AUTOSHOW_TYPE_NEVER = 0;
	public static final int CONSOLE_AUTOSHOW_TYPE_ALWAYS = 1;
	public static final int CONSOLE_AUTOSHOW_TYPE_ERROR = 2;
	public static final int CONSOLE_AUTOSHOW_TYPE_WARNING_ERROR = 3;
	
	public static final boolean CONSOLE_ENABLED_DEFAULT = true;
	public static final boolean CONSOLE_HYPERLINKS_ENABLED_DEFAULT = true;
	
	public static final boolean CONSOLE_WRAP_ENABLED_DEFAULT = false;
	public static final int CONSOLE_WRAP_WIDTH_DEFAULT = 80;
	public static final boolean CONSOLE_LIMIT_ENABLED_DEFAULT = true;
	public static final int CONSOLE_LIMIT_VALUE_DEFAULT = 500000;
	
	public static final Color CONSOLE_ERR_COLOR_DEFAULT = new Color(null, 255, 0, 0);
	public static final Color CONSOLE_WRN_COLOR_DEFAULT = new Color(null, 128, 0, 0);
	public static final Color CONSOLE_OK_COLOR_DEFAULT = new Color(null, 0, 0, 255);
	public static final Color CONSOLE_CMD_COLOR_DEFAULT = new Color(null, 0, 0, 0);
	
	public static final String MAILREPORTER_ENABLED_NAME = "enabled";
	public static final String MAILREPORTER_ERRORS_ENABLED_NAME = "errorsEnabled";
	
	public static final boolean MAILREPORTER_ENABLED_DEFAULT = true;
	public static final boolean MAILREPORTER_ERRORS_ENABLED_DEFAULT = true;

	public static final String MERGE_USE_JAVAHL_NAME = "useJavaHL";
	
	public static final boolean MERGE_USE_JAVAHL_DEFAULT = false;
	
	public static final String SHARE_ENABLE_AUTO_NAME = "enableAuto";
	
	public static final boolean SHARE_ENABLE_AUTO_DEFAULT = true;
	
	public static final String COMMIT_SELECT_NEW_RESOURCES_NAME = "selectNew";
	
	public static final boolean COMMIT_SELECT_NEW_RESOURCES_DEFAULT = true;
	
	public static final String USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME = "treatExternalAsLocal";
	
	public static final boolean USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT = true;
	
	public static final String DETECT_DELETED_PROJECTS_NAME = "detectDeleted";
	
	public static final boolean DETECT_DELETED_PROJECTS_DEFAULT = true;
	
	public static final String COMPUTE_KEYWORDS_NAME = "computeValues";
	
	public static final boolean COMPUTE_KEYWORDS_DEFAULT = true;
	
	public static final String CHECKOUT_USE_DOT_PROJECT_NAME = "useDotProject";
	
	public static final boolean CHECKOUT_USE_DOT_PROJECT_DEFAULT = true;
	
	public static final String HISTORY_PAGE_SIZE_NAME = "pageSize";
	public static final String HISTORY_PAGING_ENABLE_NAME = "pagingEnable";
	public static final String HISTORY_SHOW_MULTILINE_COMMENT_NAME = "multilineComment";
	public static final String HISTORY_SHOW_AFFECTED_PATHS_NAME = "affectedPaths";
	public static final String HISTORY_HIERARCHICAL_LAYOUT = "hierarchicalLayout";
	public static final String HISTORY_COMPARE_MODE = "compareMode";
	public static final String HISTORY_LINK_WITH_EDITOR_NAME = "linkWithEditor";

	public static final int HISTORY_PAGE_SIZE_DEFAULT = 25;
	public static final boolean HISTORY_PAGING_ENABLE_DEFAULT = true;
	public static final boolean HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT = true;
	public static final boolean HISTORY_SHOW_AFFECTED_PATHS_DEFAULT = true;
	public static final boolean HISTORY_HIERARCHICAL_LAYOUT_DEFAULT = true;
	public static final boolean HISTORY_COMPARE_MODE_DEFAULT = false;
	public static final boolean HISTORY_LINK_WITH_EDITOR_DEFAULT = false;
	
	public static final String PROPERTY_USE_VIEW_NAME = "useView";
	public static final String PROPERTY_LINK_WITH_EDITOR_NAME = "linkWithEditor";
	
	public static final boolean PROPERTY_USE_VIEW_DEFAULT = true;
	public static final boolean PROPERTY_LINK_WITH_EDITOR_DEFAULT = false;
	
	public static final String SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME = "reportRevisionChange";
	public static final String SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME = "fastReport";
	
	public static final boolean SYNCHRONIZE_REPORT_REVISION_CHANGE_DEFAULT = false;
	public static final boolean SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT = true;
	
	public static final String REPOSITORY_SHOW_BROWSER_NAME = "repositoryBrowser";
	public static final boolean REPOSITORY_SHOW_BROWSER_DEFAULT = true;
	public static final String REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME = "forceExternalsFreeze";
	public static final boolean REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT = true;
	public static final String REPOSITORY_HEAD_NAME = "head";
	public static final String REPOSITORY_BRANCHES_NAME = "branches";
	public static final String REPOSITORY_TAGS_NAME = "tags";
	public static final String REPOSITORY_SHOW_EXTERNALS_NAME = "showExternals";
	
	public static final String REPOSITORY_HEAD_DEFAULT = "trunk";
	public static final String REPOSITORY_BRANCHES_DEFAULT = "branches";
	public static final String REPOSITORY_TAGS_DEFAULT = "tags";
	public static final boolean REPOSITORY_SHOW_EXTERNALS_DEFAULT = true;
	
	public static final String BRANCH_TAG_CONSIDER_STRUCTURE_NAME = "tagConsideringProjectStructure";
	public static final boolean BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT = true;
	
	public static final String DECORATION_FORMAT_FILE_NAME = "format.file";
	public static final String DECORATION_FORMAT_FOLDER_NAME = "format.folder";
	public static final String DECORATION_FORMAT_PROJECT_NAME = "format.project";
	
	public static final String DECORATION_FLAG_OUTGOING_NAME = "flag.outgoing";
	public static final String DECORATION_FLAG_ADDED_NAME = "flag.added";
	
	public static final String DECORATION_FLAG_OUTGOING_DEFAULT = ">";
	public static final String DECORATION_FLAG_ADDED_DEFAULT = "*";
	
	public static final String DECORATION_TRUNK_PREFIX_NAME = "trunk.branch";
	public static final String DECORATION_BRANCH_PREFIX_NAME = "prefix.branch";
	public static final String DECORATION_TAG_PREFIX_NAME = "prefix.tag";
	
	public static final String DECORATION_TRUNK_PREFIX_DEFAULT = ", Trunk";
	public static final String DECORATION_BRANCH_PREFIX_DEFAULT = ", Branch";
	public static final String DECORATION_TAG_PREFIX_DEFAULT = ", Tag";
	
	public static final String DECORATION_ICON_CONFLICTED_NAME = "icon.conflicted";
	public static final String DECORATION_ICON_MODIFIED_NAME = "icon.modified";
	public static final String DECORATION_ICON_REMOTE_NAME = "icon.remote";
	public static final String DECORATION_ICON_ADDED_NAME = "icon.added";
	public static final String DECORATION_ICON_NEW_NAME = "icon.new";
	public static final String DECORATION_ICON_LOCKED_NAME = "icon.locked";
	public static final String DECORATION_ICON_NEEDS_LOCK_NAME = "icon.needslock";
	public static final String DECORATION_ICON_SWITCHED_NAME = "icon.switched";
	
	public static final boolean DECORATION_ICON_CONFLICTED_DEFAULT = true;
	public static final boolean DECORATION_ICON_MODIFIED_DEFAULT = false;
	public static final boolean DECORATION_ICON_REMOTE_DEFAULT = true;
	public static final boolean DECORATION_ICON_ADDED_DEFAULT = true;
	public static final boolean DECORATION_ICON_NEW_DEFAULT = true;
	public static final boolean DECORATION_ICON_LOCKED_DEFAULT = true;
	public static final boolean DECORATION_ICON_NEEDS_LOCK_DEFAULT = false;
	public static final boolean DECORATION_ICON_SWITCHED_DEFAULT = true;
	
	public static final String DECORATION_COMPUTE_DEEP_NAME = "compute.deep";
	public static final boolean DECORATION_COMPUTE_DEEP_DEFAULT = true;
	
	public static final String DECORATION_ENABLE_CACHE_NAME = "enable.cache";
	public static final boolean DECORATION_ENABLE_CACHE_DEFAULT = true;
	
	public static final String DECORATION_USE_FONT_COLORS_DECOR_NAME = "use.fontdecor";
	public static final boolean DECORATION_USE_FONT_COLORS_DECOR_DEFAULT = false;
	
	public static final String NAME_OF_OUTGOING_FOREGROUND_COLOR = "outgoing_change_foreground_color";
	public static final String NAME_OF_OUTGOING_BACKGROUND_COLOR = "outgoing_change_background_color";
	public static final String NAME_OF_OUTGOING_FONT = "outgoing_change_font";
	public static final String NAME_OF_IGNORED_FOREGROUND_COLOR = "ignored_resource_foreground_color";
	public static final String NAME_OF_IGNORED_BACKGROUND_COLOR = "ignored_resource_background_color";
	public static final String NAME_OF_IGNORED_FONT = "ignored_resource_font";
	public static final String NAME_OF_NOT_RELATED_NODES_FOREGROUND_COLOR = "not_related_nodes_foreground_color";
	public static final String NAME_OF_NOT_RELATED_NODES_BACKGROUND_COLOR = "not_related_nodes_background_color";
	public static final String NAME_OF_NOT_RELATED_NODES_FONT = "not_related_nodes_font";
	public static final String NAME_OF_STRUCTURE_NODES_FOREGROUND_COLOR = "structure_nodes_foreground_color";
	public static final String NAME_OF_STRUCTURE_NODES_BACKGROUND_COLOR = "structure_nodes_background_color";
	public static final String NAME_OF_STRUCTURE_NODES_FONT = "structure_nodes_font";
	
	public static final String COMMENT_TEMPLATES_LIST_NAME = "comment.templates";
	public static final String COMMENT_TEMPLATES_LIST_ENABLED_NAME = "comment.templates.enabled";
	public static final String COMMENT_LOG_TEMPLATES_ENABLED_NAME = "comment.logTemplates.enabled";
	public static final String COMMENT_SAVED_COMMENTS_COUNT_NAME = "savedCommentsCount";
	
	public static final String COMMENT_TEMPLATES_LIST_DEFAULT = "";
	public static final boolean COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT = true;
	public static final boolean COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT = true;
	public static final int COMMENT_SAVED_COMMENTS_COUNT_DEFAULT = 5;
	
	public static final String COMMIT_DIALOG_WEIGHT_NAME = "weight";
	public static final int COMMIT_DIALOG_WEIGHT_DEFAULT = 50;
	
	public static final String COMMIT_DIALOG_WIDTH_NAME = "width";
	public static final int COMMIT_DIALOG_WIDTH_DEFAULT = 600;
	
	public static final String COMMIT_DIALOG_HEIGHT_NAME = "height";
	public static final int COMMIT_DIALOG_HEIGHT_DEFAULT = SWT.DEFAULT;
	
	public static final String COMPARE_DIALOG_WIDTH_NAME = "compare.width";
	public static final int COMPARE_DIALOG_WIDTH_DEFAULT = 650;
	
	public static final String COMPARE_DIALOG_HEIGHT_NAME = "compare.height";
	public static final int COMPARE_DIALOG_HEIGHT_DEFAULT = 500;
	
	public static final String TABLE_SORTING_CASE_INSENSITIVE_NAME = "case.insensitive";
	public static final boolean TABLE_SORTING_CASE_INSENSITIVE_DEFAULT = true;
	
	public static final String AUTO_PROPERTIES_LIST_NAME = "autoproperties";
	public static final String AUTO_PROPERTIES_LIST_DEFAULT = "";
	
	public static void setDefaultValues(IPreferenceStore store) {
		PluginPreferences.setDefaultRepositoryValues(store);
		PluginPreferences.setDefaultDecorationValues(store);
		PluginPreferences.setDefaultPerformanceValues(store);
		PluginPreferences.setDefaultSynchronizeValues(store);
		PluginPreferences.setDefaultMailReporterValues(store);
		PluginPreferences.setDefaultHistoryValues(store);
		PluginPreferences.setDefaultPropertiesValues(store);
		PluginPreferences.setDefaultCommentTemplatesValues(store);
		PluginPreferences.setDefaultResourceSelectionValues(store);
		PluginPreferences.setDefaultMergeValues(store);
		PluginPreferences.setDefaultCheckoutValues(store);
		PluginPreferences.setDefaultConsoleValues(store);
		PluginPreferences.setDefaultKeywordsValues(store);
		PluginPreferences.setDefaultShareValues(store);
		PluginPreferences.setDefaultAnnotateValues(store);
		PluginPreferences.setDefaultTableSortingValues(store);
		PluginPreferences.setDefaultCommitDialogValues(store);
		PluginPreferences.setDefaultAutoPropertiesValues(store);
	}
	
	public static void setDefaultAutoPropertiesValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullAutoPropertiesName(PluginPreferences.AUTO_PROPERTIES_LIST_NAME), PluginPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void setDefaultCommitDialogValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullCommitDialogName(PluginPreferences.COMMIT_DIALOG_WEIGHT_NAME), PluginPreferences.COMMIT_DIALOG_WEIGHT_DEFAULT);
		store.setDefault(PluginPreferences.fullCommitDialogName(PluginPreferences.COMMIT_DIALOG_HEIGHT_NAME), PluginPreferences.COMMIT_DIALOG_HEIGHT_DEFAULT);
		store.setDefault(PluginPreferences.fullCommitDialogName(PluginPreferences.COMMIT_DIALOG_WIDTH_NAME), PluginPreferences.COMMIT_DIALOG_WIDTH_DEFAULT);
		store.setDefault(PluginPreferences.fullCommitDialogName(PluginPreferences.COMPARE_DIALOG_WIDTH_NAME), PluginPreferences.COMPARE_DIALOG_WIDTH_DEFAULT);
		store.setDefault(PluginPreferences.fullCommitDialogName(PluginPreferences.COMPARE_DIALOG_HEIGHT_NAME), PluginPreferences.COMPARE_DIALOG_HEIGHT_DEFAULT);
	}
	
	public static void setDefaultTableSortingValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullTableSortingName(PluginPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME), PluginPreferences.TABLE_SORTING_CASE_INSENSITIVE_DEFAULT);
	}
	
	public static void setDefaultCheckoutValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullCheckoutName(PluginPreferences.CHECKOUT_USE_DOT_PROJECT_NAME), PluginPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT);
	}
	
	public static void setDefaultMergeValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullMergeName(PluginPreferences.MERGE_USE_JAVAHL_NAME), PluginPreferences.MERGE_USE_JAVAHL_DEFAULT);
	}
	
	public static void setDefaultHistoryValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_PAGE_SIZE_NAME), PluginPreferences.HISTORY_PAGE_SIZE_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_PAGING_ENABLE_NAME), PluginPreferences.HISTORY_PAGING_ENABLE_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME), PluginPreferences.HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME), PluginPreferences.HISTORY_SHOW_AFFECTED_PATHS_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_HIERARCHICAL_LAYOUT), PluginPreferences.HISTORY_HIERARCHICAL_LAYOUT_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_COMPARE_MODE), PluginPreferences.HISTORY_COMPARE_MODE_DEFAULT);
		store.setDefault(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_LINK_WITH_EDITOR_NAME), PluginPreferences.HISTORY_LINK_WITH_EDITOR_DEFAULT);
	}
	
	public static void setDefaultSynchronizeValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullSynchronizeName(PluginPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME), PluginPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_DEFAULT);
		store.setDefault(PluginPreferences.fullSynchronizeName(PluginPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME), PluginPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT);
	}
	
	public static void setDefaultPropertiesValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullPropertiesName(PluginPreferences.PROPERTY_USE_VIEW_NAME), PluginPreferences.PROPERTY_USE_VIEW_DEFAULT);
		store.setDefault(PluginPreferences.fullPropertiesName(PluginPreferences.PROPERTY_LINK_WITH_EDITOR_NAME), PluginPreferences.PROPERTY_LINK_WITH_EDITOR_DEFAULT);
	}
	
	public static void setDefaultRepositoryValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_HEAD_NAME), PluginPreferences.REPOSITORY_HEAD_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_BRANCHES_NAME), PluginPreferences.REPOSITORY_BRANCHES_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_TAGS_NAME), PluginPreferences.REPOSITORY_TAGS_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_SHOW_EXTERNALS_NAME), PluginPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_SHOW_BROWSER_NAME), PluginPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME), PluginPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT);
		store.setDefault(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME), PluginPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT);
	}
	
	public static void setDefaultDecorationValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_FLAG_OUTGOING_NAME), PluginPreferences.DECORATION_FLAG_OUTGOING_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_FLAG_ADDED_NAME), PluginPreferences.DECORATION_FLAG_ADDED_DEFAULT);
		
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_TRUNK_PREFIX_NAME), PluginPreferences.DECORATION_TRUNK_PREFIX_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_BRANCH_PREFIX_NAME), PluginPreferences.DECORATION_BRANCH_PREFIX_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_TAG_PREFIX_NAME), PluginPreferences.DECORATION_TAG_PREFIX_DEFAULT);
		
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_CONFLICTED_NAME), PluginPreferences.DECORATION_ICON_CONFLICTED_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_MODIFIED_NAME), PluginPreferences.DECORATION_ICON_MODIFIED_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_REMOTE_NAME), PluginPreferences.DECORATION_ICON_REMOTE_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_ADDED_NAME), PluginPreferences.DECORATION_ICON_ADDED_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_NEW_NAME), PluginPreferences.DECORATION_ICON_NEW_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_LOCKED_NAME), PluginPreferences.DECORATION_ICON_LOCKED_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_NEEDS_LOCK_NAME), PluginPreferences.DECORATION_ICON_NEEDS_LOCK_DEFAULT);
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_SWITCHED_NAME), PluginPreferences.DECORATION_ICON_SWITCHED_DEFAULT);
		
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME), PluginPreferences.DECORATION_USE_FONT_COLORS_DECOR_DEFAULT);
	}
	
	public static void setDefaultPerformanceValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_COMPUTE_DEEP_NAME), PluginPreferences.DECORATION_COMPUTE_DEEP_DEFAULT);		
		store.setDefault(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ENABLE_CACHE_NAME), PluginPreferences.DECORATION_ENABLE_CACHE_DEFAULT);		
	}
	
	public static void setDefaultMailReporterValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullMailReporterName(PluginPreferences.MAILREPORTER_ENABLED_NAME), PluginPreferences.MAILREPORTER_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullMailReporterName(PluginPreferences.MAILREPORTER_ERRORS_ENABLED_NAME), PluginPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT);
	}
	
	public static void setDefaultCommentTemplatesValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_TEMPLATES_LIST_NAME), PluginPreferences.COMMENT_TEMPLATES_LIST_DEFAULT);
		store.setDefault(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME), PluginPreferences.COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME), PluginPreferences.COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME), PluginPreferences.COMMENT_SAVED_COMMENTS_COUNT_DEFAULT);
	}
	
	public static void setDefaultConsoleValues(final IPreferenceStore store) {
		UIMonitorUtil.getDisplay().asyncExec(new Runnable() {
			public void run() {
				PreferenceConverter.setDefault(store, PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_ERR_COLOR_NAME), PluginPreferences.CONSOLE_ERR_COLOR_DEFAULT.getRGB());
				PreferenceConverter.setDefault(store, PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_WRN_COLOR_NAME), PluginPreferences.CONSOLE_WRN_COLOR_DEFAULT.getRGB());
				PreferenceConverter.setDefault(store, PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_OK_COLOR_NAME), PluginPreferences.CONSOLE_OK_COLOR_DEFAULT.getRGB());
				PreferenceConverter.setDefault(store, PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_CMD_COLOR_NAME), PluginPreferences.CONSOLE_CMD_COLOR_DEFAULT.getRGB());
			}
		});
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_ENABLED_NAME), PluginPreferences.CONSOLE_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_HYPERLINKS_ENABLED_NAME), PluginPreferences.CONSOLE_HYPERLINKS_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_WRAP_ENABLED_NAME), PluginPreferences.CONSOLE_WRAP_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_LIMIT_ENABLED_NAME), PluginPreferences.CONSOLE_LIMIT_ENABLED_DEFAULT);
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_WRAP_WIDTH_NAME), PluginPreferences.CONSOLE_WRAP_WIDTH_DEFAULT);
		store.setDefault(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_LIMIT_VALUE_NAME), PluginPreferences.CONSOLE_LIMIT_VALUE_DEFAULT);
	}
	
	public static void setDefaultAnnotateValues(IPreferenceStore store) {
		PreferenceConverter.setDefault(store, PluginPreferences.fullAnnotateName(PluginPreferences.ANNOTATE_RGB_BASE_NAME), PluginPreferences.ANNOTATE_RGB_BASE_DEFAULT);
		store.setDefault(PluginPreferences.fullAnnotateName(PluginPreferences.ANNOTATE_USE_ONE_RGB_NAME), PluginPreferences.ANNOTATE_USE_ONE_RGB_DEFAULT);
	}
	
	public static void setDefaultResourceSelectionValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullResourceSelectionName(PluginPreferences.DETECT_DELETED_PROJECTS_NAME), PluginPreferences.DETECT_DELETED_PROJECTS_DEFAULT);
		store.setDefault(PluginPreferences.fullResourceSelectionName(PluginPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME), PluginPreferences.COMMIT_SELECT_NEW_RESOURCES_DEFAULT);
		store.setDefault(PluginPreferences.fullResourceSelectionName(PluginPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME), PluginPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT);
	}
	
	public static void setDefaultKeywordsValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullKeywordsName(PluginPreferences.COMPUTE_KEYWORDS_NAME), PluginPreferences.COMPUTE_KEYWORDS_DEFAULT);
	}
	
	public static void setDefaultShareValues(IPreferenceStore store) {
		store.setDefault(PluginPreferences.fullShareName(PluginPreferences.SHARE_ENABLE_AUTO_NAME), PluginPreferences.SHARE_ENABLE_AUTO_DEFAULT);
	}
	
	public static void resetToDefaultAutoPropsValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullAutoPropertiesName(PluginPreferences.AUTO_PROPERTIES_LIST_NAME), PluginPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void resetToDefaultTableSortingValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullTableSortingName(PluginPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME), PluginPreferences.TABLE_SORTING_CASE_INSENSITIVE_DEFAULT);
	}
	
	public static void resetToDefaultHistoryValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_PAGE_SIZE_NAME), PluginPreferences.HISTORY_PAGE_SIZE_DEFAULT);
		store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_PAGING_ENABLE_NAME), PluginPreferences.HISTORY_PAGING_ENABLE_DEFAULT);
		store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME), PluginPreferences.HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT);
		store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME), PluginPreferences.HISTORY_SHOW_AFFECTED_PATHS_DEFAULT);
		//FIXME uncomment this line when this options are added to plugin preferences 
		//store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_HIERARCHICAL_LAYOUT), PluginPreferences.HISTORY_HIERARCHICAL_LAYOUT_DEFAULT);
		//store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_SHOW_BROWSER_NAME), PluginPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		//store.setValue(PluginPreferences.fullHistoryName(PluginPreferences.HISTORY_COMPARE_MODE), PluginPreferences.HISTORY_COMPARE_MODE_DEFAULT);
	}
	
	public static void resetToDefaultMailReporterValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullMailReporterName(PluginPreferences.MAILREPORTER_ENABLED_NAME), PluginPreferences.MAILREPORTER_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullMailReporterName(PluginPreferences.MAILREPORTER_ERRORS_ENABLED_NAME), PluginPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT);
	}
	
	public static void resetToDefaultCheckoutValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullCheckoutName(PluginPreferences.CHECKOUT_USE_DOT_PROJECT_NAME), PluginPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT);
	}
	
	public static void resetToDefaultMergeValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullMergeName(PluginPreferences.MERGE_USE_JAVAHL_NAME), PluginPreferences.MERGE_USE_JAVAHL_DEFAULT);
	}
	
	public static void resetToDefaultResourceSelectionValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullResourceSelectionName(PluginPreferences.DETECT_DELETED_PROJECTS_NAME), PluginPreferences.DETECT_DELETED_PROJECTS_DEFAULT);
		store.setValue(PluginPreferences.fullResourceSelectionName(PluginPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME), PluginPreferences.COMMIT_SELECT_NEW_RESOURCES_DEFAULT);
		store.setValue(PluginPreferences.fullResourceSelectionName(PluginPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME), PluginPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT);
	}
	
	public static void resetToDefaultKeywordsValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullKeywordsName(PluginPreferences.COMPUTE_KEYWORDS_NAME), PluginPreferences.COMPUTE_KEYWORDS_DEFAULT);
	}
	
	public static void resetToDefaultSynchronizeValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullSynchronizeName(PluginPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_NAME), PluginPreferences.SYNCHRONIZE_REPORT_REVISION_CHANGE_DEFAULT);
		store.setValue(PluginPreferences.fullSynchronizeName(PluginPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME), PluginPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT);
	}
	
	public static void resetToDefaultPropertiesValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullPropertiesName(PluginPreferences.PROPERTY_USE_VIEW_NAME), PluginPreferences.PROPERTY_USE_VIEW_DEFAULT);
	}
	
	public static void resetToDefaultRepositoryValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_HEAD_NAME), PluginPreferences.REPOSITORY_HEAD_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_BRANCHES_NAME), PluginPreferences.REPOSITORY_BRANCHES_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_TAGS_NAME), PluginPreferences.REPOSITORY_TAGS_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_SHOW_EXTERNALS_NAME), PluginPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_SHOW_BROWSER_NAME), PluginPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME), PluginPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT);
		store.setValue(PluginPreferences.fullRepositoryName(PluginPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME), PluginPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT);
	}
	
	public static void resetToDefaultDecorationValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_FLAG_OUTGOING_NAME), PluginPreferences.DECORATION_FLAG_OUTGOING_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_FLAG_ADDED_NAME), PluginPreferences.DECORATION_FLAG_ADDED_DEFAULT);
		
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_TRUNK_PREFIX_NAME), PluginPreferences.DECORATION_TRUNK_PREFIX_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_BRANCH_PREFIX_NAME), PluginPreferences.DECORATION_BRANCH_PREFIX_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_TAG_PREFIX_NAME), PluginPreferences.DECORATION_TAG_PREFIX_DEFAULT);

		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_CONFLICTED_NAME), PluginPreferences.DECORATION_ICON_CONFLICTED_DEFAULT);		
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_MODIFIED_NAME), PluginPreferences.DECORATION_ICON_MODIFIED_DEFAULT);		
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_REMOTE_NAME), PluginPreferences.DECORATION_ICON_REMOTE_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_ADDED_NAME), PluginPreferences.DECORATION_ICON_ADDED_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_NEW_NAME), PluginPreferences.DECORATION_ICON_NEW_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_LOCKED_NAME), PluginPreferences.DECORATION_ICON_LOCKED_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_NEEDS_LOCK_NAME), PluginPreferences.DECORATION_ICON_NEEDS_LOCK_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ICON_SWITCHED_NAME), PluginPreferences.DECORATION_ICON_SWITCHED_DEFAULT);

		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME), PluginPreferences.DECORATION_USE_FONT_COLORS_DECOR_DEFAULT);
	}
	
	public static void resetToDefaultPerformanceValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_COMPUTE_DEEP_NAME), PluginPreferences.DECORATION_COMPUTE_DEEP_DEFAULT);
		store.setValue(PluginPreferences.fullDecorationName(PluginPreferences.DECORATION_ENABLE_CACHE_NAME), PluginPreferences.DECORATION_ENABLE_CACHE_DEFAULT);
	}
	
	public static void resetToDefaultCommentTemplatesValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_TEMPLATES_LIST_NAME), PluginPreferences.COMMENT_TEMPLATES_LIST_DEFAULT);
		store.setValue(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME), PluginPreferences.COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME), PluginPreferences.COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullCommentTemplatesName(PluginPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME), PluginPreferences.COMMENT_SAVED_COMMENTS_COUNT_DEFAULT);
	}
	
	public static void resetToDefaultConsoleValues(IPreferenceStore store) {
		PluginPreferences.setConsoleColor(store, PluginPreferences.CONSOLE_ERR_COLOR_NAME, PluginPreferences.CONSOLE_ERR_COLOR_DEFAULT);
		PluginPreferences.setConsoleColor(store, PluginPreferences.CONSOLE_WRN_COLOR_NAME, PluginPreferences.CONSOLE_WRN_COLOR_DEFAULT);
		PluginPreferences.setConsoleColor(store, PluginPreferences.CONSOLE_OK_COLOR_NAME, PluginPreferences.CONSOLE_OK_COLOR_DEFAULT);
		PluginPreferences.setConsoleColor(store, PluginPreferences.CONSOLE_CMD_COLOR_NAME, PluginPreferences.CONSOLE_CMD_COLOR_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_ENABLED_NAME), PluginPreferences.CONSOLE_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_HYPERLINKS_ENABLED_NAME), PluginPreferences.CONSOLE_HYPERLINKS_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_WRAP_ENABLED_NAME), PluginPreferences.CONSOLE_WRAP_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_LIMIT_ENABLED_NAME), PluginPreferences.CONSOLE_LIMIT_ENABLED_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_WRAP_WIDTH_NAME), PluginPreferences.CONSOLE_WRAP_WIDTH_DEFAULT);
		store.setValue(PluginPreferences.fullConsoleName(PluginPreferences.CONSOLE_LIMIT_VALUE_NAME), PluginPreferences.CONSOLE_LIMIT_VALUE_DEFAULT);
	}
	
	public static void resetToDefaultShareValues(IPreferenceStore store) {
		store.setValue(PluginPreferences.fullShareName(PluginPreferences.SHARE_ENABLE_AUTO_NAME), PluginPreferences.SHARE_ENABLE_AUTO_DEFAULT);
	}
	
	public static int getCommitDialogInt(IPreferenceStore store, String shortName) {
		return store.getInt(PluginPreferences.fullCommitDialogName(shortName));
	}
	
	public static boolean getTableSortingBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullTableSortingName(shortName));
	}
	
	public static boolean getCheckoutBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullCheckoutName(shortName));
	}
	
	public static boolean getMergeBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullMergeName(shortName));
	}
	
	public static boolean getSynchronizeBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullSynchronizeName(shortName));
	}
	
	public static boolean getPropertiesBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullPropertiesName(shortName));
	}
	
	public static String getRepositoryString(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullRepositoryName(shortName));
	}
	
	public static boolean getRepositoryBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullRepositoryName(shortName));
	}
	
	public static String getDecorationString(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullDecorationName(shortName));
	}
	
	public static int getHistoryInt(IPreferenceStore store, String shortName) {
		return store.getInt(PluginPreferences.fullHistoryName(shortName));
	}
	
	public static boolean getHistoryBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullHistoryName(shortName));
	}
	
	public static boolean getResourceSelectionBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullResourceSelectionName(shortName));
	}
	
	public static boolean getKeywordsBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullKeywordsName(shortName));
	}
	
	public static boolean getDecorationBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullDecorationName(shortName));
	}
	
	public static String getCommentTemplatesString(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static int getCommentTemplatesInt(IPreferenceStore store, String shortName) {
		return store.getInt(PluginPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static boolean getCommentTemplatesBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static String getAutoPropertiesList(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullAutoPropertiesName(shortName));
	}
	
	public static void setCommitDialogInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(PluginPreferences.fullCommitDialogName(shortName), value);
	}
	
	public static void setTableSortingBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullTableSortingName(shortName), value);
	}
	
	public static void setCheckoutBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullCheckoutName(shortName), value);
	}
	
	public static void setMergeBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullMergeName(shortName), value);
	}
	
	public static void setSynchronizeBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullSynchronizeName(shortName), value);
	}
	
	public static void setPropertiesBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullPropertiesName(shortName), value);		
	}
	
	public static void setRepositoryString(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullRepositoryName(shortName), value);
	}
	
	public static void setRepositoryBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullRepositoryName(shortName), value);
	}

	public static void setDecorationString(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullDecorationName(shortName), value);
	}
	
	public static void setDecorationBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullDecorationName(shortName), value);
	}
	
	public static void setHistoryBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullHistoryName(shortName), value);
	}
	
	public static void setResourceSelectionBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullResourceSelectionName(shortName), value);
	}
	
	public static void setKeywordsBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullKeywordsName(shortName), value);
	}
	
	public static void setHistoryInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(PluginPreferences.fullHistoryName(shortName), value);
	}
	
	public static boolean getMailReporterBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullMailReporterName(shortName));
	}
	
	public static void setMailReporterBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullMailReporterName(shortName), value);
	}
	
	public static void setCommentTemplatesInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(PluginPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setCommentTemplatesString(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setCommentTemplatesBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setAutoPropertiesList(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullAutoPropertiesName(shortName), value);
	}
	
	public static Color getConsoleColor(IPreferenceStore store, String shortName) {
		return new Color(UIMonitorUtil.getDisplay(), PreferenceConverter.getColor(store, PluginPreferences.fullConsoleName(shortName)));
	}
	
	public static void setConsoleColor(IPreferenceStore store, String shortName, Color value) {
		PreferenceConverter.setValue(store, PluginPreferences.fullConsoleName(shortName), value.getRGB());
	}
	
	public static int getConsoleInt(IPreferenceStore store, String shortName) {
		return store.getInt(PluginPreferences.fullConsoleName(shortName));
	}
	
	public static void setConsoleInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(PluginPreferences.fullConsoleName(shortName), value);
	}
	
	public static boolean getConsoleBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullConsoleName(shortName));
	}
	
	public static void setConsoleBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullConsoleName(shortName), value);
	}
	
	public static boolean getShareBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullShareName(shortName));
	}
	
	public static void setShareBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullShareName(shortName), value);
	}
	
	public static String getCoreString(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullCoreName(shortName));
	}
	
	public static void setCoreString(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullCoreName(shortName), value);
	}
	
	public static RGB getAnnotateRGB(IPreferenceStore store, String shortName) {
		return PreferenceConverter.getColor(store, PluginPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateRGB(IPreferenceStore store, String shortName, RGB value) {
		PreferenceConverter.setValue(store, PluginPreferences.fullAnnotateName(shortName), value);
	}
	
	public static int getAnnotateInt(IPreferenceStore store, String shortName) {
		return store.getInt(PluginPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(PluginPreferences.fullAnnotateName(shortName), value);
	}
	
	public static String getAnnotateString(IPreferenceStore store, String shortName) {
		return store.getString(PluginPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateString(IPreferenceStore store, String shortName, String value) {
		store.setValue(PluginPreferences.fullAnnotateName(shortName), value);
	}
	
	public static boolean getAnnotateBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(PluginPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(PluginPreferences.fullAnnotateName(shortName), value);
	}
	
	public static String fullCommitDialogName(String shortName) {
		return PluginPreferences.COMMIT_DIALOG_BASE + shortName;
	}
	
	public static String fullTableSortingName(String shortName) {
		return PluginPreferences.TABLE_SORTING_BASE + shortName;
	}
	
	public static String fullCheckoutName(String shortName) {
		return PluginPreferences.CHECKOUT_BASE + shortName;
	}
	
	public static String fullMergeName(String shortName) {
		return PluginPreferences.MERGE_BASE + shortName;
	}
	
	public static String fullDecorationName(String shortName) {
		return PluginPreferences.DECORATION_BASE + shortName;
	}
	
	public static String fullRepositoryName(String shortName) {
		return PluginPreferences.REPOSITORY_BASE + shortName;
	}
	
	public static String fullSynchronizeName(String shortName) {
		return PluginPreferences.SYNCHRONIZE_BASE + shortName;
	}
	
	public static String fullPropertiesName(String shortName) {
		return PluginPreferences.PROPERTIES_BASE + shortName;
	}
	
	public static String fullHistoryName(String shortName) {
		return PluginPreferences.HISTORY_BASE + shortName;
	}
	
	public static String fullResourceSelectionName(String shortName) {
		return PluginPreferences.RESOURCE_SELECTION_BASE + shortName;
	}
	
	public static String fullMailReporterName(String shortName) {
		return PluginPreferences.MAILREPORTER_BASE + shortName;
	}
	
	public static String fullCommitSelectName(String shortName) {
		return PluginPreferences.COMMENT_TEMPLATES_BASE + shortName;
	}
	
	public static String fullCommentTemplatesName(String shortName) {
		return PluginPreferences.COMMENT_TEMPLATES_BASE + shortName;
	}
	
	public static String fullKeywordsName(String shortName) {
		return PluginPreferences.KEYWORDS_BASE + shortName;
	}
	
	public static String fullConsoleName(String shortName) {
		return PluginPreferences.CONSOLE_BASE + shortName;
	}
	
	public static String fullShareName(String shortName) {
		return PluginPreferences.SHARE_BASE + shortName;
	}
	
	public static String fullCoreName(String shortName) {
		return PluginPreferences.CORE_BASE + shortName;
	}
	
	public static String fullAnnotateName(String shortName) {
		return PluginPreferences.ANNOTATE_BASE + shortName;
	}
	
	public static String fullAutoPropertiesName(String shortName) {
		return PluginPreferences.AUTO_PROPERTIES_BASE + shortName;
	}
	
	private PluginPreferences() {
		
	}
	
}
