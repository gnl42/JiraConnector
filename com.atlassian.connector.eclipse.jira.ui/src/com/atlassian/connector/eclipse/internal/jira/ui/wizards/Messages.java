/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.jira.ui.wizards.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String GoogleAppsExtensionSettingsContribution_title;

	public static String GoogleAppsExtensionSettingsContribution_description;

	public static String GoogleAppsExtensionSettingsContribution_help_message;

	public static String JiraFilterDefinitionPage_Add_search_filters_to_define_query;

	public static String JiraFilterDefinitionPage_All_Projects;

	public static String JiraFilterDefinitionPage_Any;

	public static String JiraFilterDefinitionPage_Assigned_To;

	public static String JiraFilterDefinitionPage_Comments;

	public static String JiraFilterDefinitionPage_Created;

	public static String JiraFilterDefinitionPage_Current_User;

	public static String JiraFilterDefinitionPage_Description;

	public static String JiraFilterDefinitionPage_Due_Date;

	public static String JiraFilterDefinitionPage__end_date_;

	public static String JiraFilterDefinitionPage_Environment;

	public static String JiraFilterDefinitionPage_Error_updating_attributes_X;

	public static String JiraFilterDefinitionPage_FILEDS;

	public static String JiraFilterDefinitionPage_Fix_For;

	public static String JiraFilterDefinitionPage_In_Components;

	public static String JiraFilterDefinitionPage_JIRA_Query;

	public static String JiraFilterDefinitionPage_No_Component;

	public static String JiraFilterDefinitionPage_No_Fix_Version;

	public static String JiraFilterDefinitionPage_No_Project;

	public static String JiraFilterDefinitionPage_No_Reporter;

	public static String JiraFilterDefinitionPage_No_Version;

	public static String JiraFilterDefinitionPage_Priority;

	public static String JiraFilterDefinitionPage_Project;

	public static String JiraFilterDefinitionPage_Query;

	public static String JiraFilterDefinitionPage_Query_Title;

	public static String JiraFilterDefinitionPage_Released_Versions;

	public static String JiraFilterDefinitionPage_Reported_By;

	public static String JiraFilterDefinitionPage_Reported_In;

	public static String JiraFilterDefinitionPage_Resolution;

	public static String JiraFilterDefinitionPage_Specified_Group;

	public static String JiraFilterDefinitionPage_Specified_User;

	public static String JiraFilterDefinitionPage__start_date_;

	public static String JiraFilterDefinitionPage_Status;

	public static String JiraFilterDefinitionPage_Summary;

	public static String JiraFilterDefinitionPage_Type;

	public static String JiraFilterDefinitionPage_Unassigned;

	public static String JiraFilterDefinitionPage_Unreleased_Versions;

	public static String JiraFilterDefinitionPage_Unresolved;

	public static String JiraFilterDefinitionPage_Updated;

	public static String JiraFilterDefinitionPage_Update_Attributes_from_Repository;

	public static String JiraFilterDefinitionPage_From;

	public static String JiraFilterDefinitionPage_To;

	public static String JiraFilterDefinitionPage_And;

	public static String JiraIncorrectWdhmValue;

	public static String JiraWdhmExplanation;

	public static String JiraNamedFilterPage_Could_not_update_filters;

	public static String JiraNamedFilterPage_Create_query_using_form;

	public static String JiraNamedFilterPage_Predefined_filter_added_recently;

	public static String JiraNamedFilterPage_Predefined_filter_updated_recently;

	public static String JiraNamedFilterPage_Predefined_filter_resolved_recently;

	public static String JiraNamedFilterPage_Predefined_filter_assigned_to_me;

	public static String JiraNamedFilterPage_Predefined_filter_reported_by_me;

	public static String JiraNamedFilterPage_Downloading_;

	public static String JiraNamedFilterPage_Downloading_Filter_Names;

	public static String JiraNamedFilterPage_Downloading_list_of_filters;

	public static String JiraNamedFilterPage_New_Jira_Query;

	public static String JiraNamedFilterPage_No_filters_found;

	public static String JiraNamedFilterPage_No_saved_filters_found;

	public static String JiraNamedFilterPage_Please_select_a_query_type;

	public static String JiraNamedFilterPage_Update_from_Repository;

	public static String JiraNamedFilterPage_Use_saved_filter_from_the_repository;

	public static String JiraNamedFilterPage_Predefined_Filter;

	public static String JiraNamedFilterPage_Downloading_Projects;

	public static String JiraNamedFilterPage_Download_Projects_Failed;

	public static String JiraNamedFilterPage_Use_project_specific_predefined_filter;

	public static String JiraProjectPage_You_must_select_a_project;

	public static String JiraProjectPage_New_JIRA_Task;

	public static String JiraProjectPage_Pick_a_project_to_open_the_new_bug_editor;

	public static String JiraProjectPage_Update_Project_Listing;

	public static String JiraRepositorySettingsPage_Administration_priviliges_required;

	public static String JiraRepositorySettingsPage_Advanced_Configuration;

	public static String JiraRepositorySettingsPage_Authentication_credentials_are_valid_character_encodeing;

	public static String JiraRepositorySettingsPage_Authentication_credentials_are_valid_server_redirected;

	public static String JiraRepositorySettingsPage_Automatically;

	public static String JiraRepositorySettingsPage_Compression;

	public static String JiraRepositorySettingsPage_Date_Picker_Format;

	public static String JiraRepositorySettingsPage_Date_Time_Picker_Format;

	public static String JiraRepositorySettingsPage_Enabled;

	public static String JiraRepositorySettingsPage_Failed_retrieve_repository_configuration;

	public static String JiraRepositorySettingsPage_Follow_redirects;

	public static String JiraRepositorySettingsPage_Getting_repository_configuration;

	public static String JiraRepositorySettingsPage_If_checked_linked_tasks_show_as_subtasks_in_the_task_list;

	public static String JiraRepositorySettingsPage_If_checked_the_repository_configuration_will_be_periodically_updated;

	public static String JiraRepositorySettingsPage_JIRA_Repository_Settings;

	public static String JiraRepositorySettingsPage_Keep_current_location_;

	public static String JiraRepositorySettingsPage_Limit;

	public static String JiraRepositorySettingsPage_Locale;

	public static String JiraRepositorySettingsPage_Refresh_configuration;

	public static String JiraRepositorySettingsPage_remote_api_locked;

	public static String JiraRepositorySettingsPage_The_repository_location_reported_by_the_server_does_not_match_the_provided_location;

	public static String JiraRepositorySettingsPage_Reset_to_defaults;

	public static String JiraRepositorySettingsPage_Search_results;

	public static String JiraRepositorySettingsPage_Select_repository_location;

	public static String JiraRepositorySettingsPage_Show_linked_tasks;

	public static String JiraRepositorySettingsPage_Subtasks;

	public static String JiraRepositorySettingsPage_Time_tracking;

	public static String JiraRepositorySettingsPage_Use_server_location_;

	public static String JiraRepositorySettingsPage_use_server_settings;

	public static String JiraRepositorySettingsPage_working_days_per_week;

	public static String JiraRepositorySettingsPage_working_hours_per_day;

	public static String JiraRepositorySettingsPage_Validate_server_settings;

	public static String JiraProjectPage_This_project_has_details_missing;

	public static String NewJiraTaskWizard_New_Jira_Task_Title;
}
