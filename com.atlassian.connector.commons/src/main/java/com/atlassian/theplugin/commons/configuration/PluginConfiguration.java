/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.configuration;

import com.atlassian.theplugin.commons.util.HttpConfigurableAdapter;


public interface PluginConfiguration {

	void setConfiguration(PluginConfiguration cfg);

	GeneralConfigurationBean getGeneralConfigurationData();

	void setGeneralConfigurationData(GeneralConfigurationBean generalConfigurationBean);

	//set should be applied either in IDEA an Eclipse environment
	void transientSetHttpConfigurable(HttpConfigurableAdapter httpConfigurableAdapter);
	HttpConfigurableAdapter transientGetHttpConfigurable();

	BambooConfigurationBean getBambooConfigurationData();

	CrucibleConfigurationBean getCrucibleConfigurationData();

	JiraConfigurationBean getJIRAConfigurationData();
}
