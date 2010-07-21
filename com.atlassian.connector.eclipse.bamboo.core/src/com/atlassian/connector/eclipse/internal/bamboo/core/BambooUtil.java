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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Provides utility methods for Bamboo.
 * 
 * @author Shawn Minto
 */
public final class BambooUtil {

	public interface BuildChangeAction {
		void run(BambooBuild build, TaskRepository repository);
	}

	private static final String KEY_SUBSCRIBED_PLANS = "com.atlassian.connector.eclipse.bamboo.subscribedPlans";

	public static final String KEY_USE_FAVOURITES = "com.atlassian.connector.eclipse.bamboo.useFavourites";

	private BambooUtil() {
	}

	public static void setSubcribedPlans(TaskRepository repository, Collection<SubscribedPlan> plans) {
		StringBuffer sb = new StringBuffer();
		for (SubscribedPlan plan : plans) {
			sb.append(plan.getKey());
			sb.append(",");
		}
		repository.setProperty(KEY_SUBSCRIBED_PLANS, sb.toString());
	}

	public static Collection<SubscribedPlan> getSubscribedPlans(TaskRepository repository) {
		Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		String value = repository.getProperty(KEY_SUBSCRIBED_PLANS);
		if (value != null) {
			StringTokenizer t = new StringTokenizer(value, ",");
			while (t.hasMoreTokens()) {
				plans.add(new SubscribedPlan(t.nextToken()));
			}
		}
		return plans;
	}

	public static boolean isSameBuildPlan(BambooBuild buildOne, BambooBuild buildTwo) {
		if (!buildOne.getServerUrl().equals(buildTwo.getServerUrl())) {
			return false;
		}
		//check if same planKey
		return buildOne.getPlanKey().equals(buildTwo.getPlanKey());

//		//check if from same server
//		String[] keyElementsOne = buildOne.getBuildKey().split("-");
//		String[] keyElementsTwo = buildTwo.getBuildKey().split("-");

//		//check if at least 2 elements
//		if (keyElementsOne.length < 2) {
//			StatusHandler.log(new Status(IStatus.WARNING, BambooCorePlugin.PLUGIN_ID, "Invalid Bamboo Build Key: "
//					+ buildOne.getBuildKey()));
//			return false;
//		}
//		if (keyElementsTwo.length < 2) {
//			StatusHandler.log(new Status(IStatus.WARNING, BambooCorePlugin.PLUGIN_ID, "Invalid Bamboo Build Key: "
//					+ buildTwo.getBuildKey()));
//			return false;
//		}
//		//check if same project and same build plan
//		return keyElementsOne[0].equals(keyElementsTwo[0]) && keyElementsOne[1].equals(keyElementsTwo[1]);
	}

	public static String getUrlFromBuild(BambooBuild build) {
		StringBuilder builder = new StringBuilder();
		builder.append(build.getServerUrl());
		builder.append("/build/viewBuildResults.action?buildKey=");
		builder.append(build.getPlanKey());
		builder.append("&buildNumber=");
		builder.append(build.getNumber());
		// ignore
		return builder.toString();
	}

	public static void runActionForChangedBuild(BuildsChangedEvent event, BambooUtil.BuildChangeAction buildChangeAction) {
		if (event.getChangedBuilds().size() > 0) {
			for (TaskRepository key : event.getChangedBuilds().keySet()) {
				for (BambooBuild build : event.getChangedBuilds().get(key)) {
					//for each build get equivalent old build
					for (BambooBuild oldBuild : event.getOldBuilds().get(key)) {
						if (isSameBuildPlan(build, oldBuild)) {
							if (build.getStatus() != oldBuild.getStatus()) {
								//build status changed
								buildChangeAction.run(build, key);
							}
						}
					}
				}
			}
		}
	}

	public static boolean isUseFavourites(TaskRepository taskRepository) {
		return Boolean.valueOf(taskRepository.getProperty(KEY_USE_FAVOURITES));
	}

	public static void setUseFavourites(TaskRepository taskRepository, boolean useFavourite) {
		taskRepository.setProperty(BambooUtil.KEY_USE_FAVOURITES, Boolean.toString(useFavourite));
	}
}
