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

/**
 * 
 */
package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Preferences;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.SubscribedPlanBean;

/**
 * @author Jacek
 *
 */
public class ProjectConfigurationWrapper {
	
	private Preferences preferences;

	public ProjectConfigurationWrapper(Preferences preferences) {
		this.preferences = preferences;
		//preferences.setValue("dupa", "maryni");
	}

	public EclipsePluginConfiguration getPluginConfiguration() {
		
		List<SubscribedPlan> subscribedPlans = new ArrayList<SubscribedPlan>();
		
		String[] plans = preferences.getString(PreferenceConstants.BAMBOO_BUILDS).split(" ");
		
		for (String plan : plans) {
			if (plan != null && plan.length() > 0) {
				SubscribedPlanBean subscribedPlan = new SubscribedPlanBean(plan);
				subscribedPlans.add(subscribedPlan);
			}
		}
		
		
		ServerBean bambooServer = new ServerBean();
		bambooServer.setEnabled(true);
		bambooServer.setUserName(preferences.getString(PreferenceConstants.BAMBOO_USER_NAME));
		bambooServer.transientSetPasswordString(preferences.getString(PreferenceConstants.BAMBOO_USER_PASSWORD), true);
		bambooServer.setUrlString(preferences.getString(PreferenceConstants.BAMBOO_URL));
		bambooServer.setSubscribedPlansData(subscribedPlans);
		bambooServer.setUseFavourite(new Boolean(preferences.getString(PreferenceConstants.BAMBOO_USE_FAVOURITES)));
		bambooServer.setName(preferences.getString(PreferenceConstants.BAMBOO_NAME));

		Collection<Server> bambooServers = new ArrayList<Server>();
		bambooServers.add(bambooServer);
		
		BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();
		bambooConfiguration.setServers(bambooServers);
		
		try {
			bambooConfiguration.setBambooTooltipOption(BambooTooltipOption.valueOf(preferences.getString(PreferenceConstants.BAMBOO_POPUP)));
		} catch (IllegalArgumentException e) {
			bambooConfiguration.setBambooTooltipOption(null);
		} catch (NullPointerException e) {
			bambooConfiguration.setBambooTooltipOption(null);
		}
//		
//		int pollTime = preferences.getInt(PreferenceConstants.BAMBOO_POLLING_TIME);
//		if (pollTime == 0) {
//			pollTime = 1;
//		}
		bambooConfiguration.setPollTime(preferences.getInt(PreferenceConstants.BAMBOO_POLLING_TIME));
		
		EclipsePluginConfiguration pluginConfiguration = new EclipsePluginConfiguration();
		pluginConfiguration.setBambooConfigurationData(bambooConfiguration);
		
		BambooTabConfiguration bambooTabConfiguration = new BambooTabConfiguration();
		bambooTabConfiguration.setColumnsOrderString(preferences.getString(PreferenceConstants.BAMBOO_TAB_COLUMNS_ORDER));
		bambooTabConfiguration.setColumnsWidthString(preferences.getString(PreferenceConstants.BAMBOO_TAB_COLUMNS_WIDTH));
		
		pluginConfiguration.setBambooTabConfiguration(bambooTabConfiguration);
		
		return pluginConfiguration;
	}
}
