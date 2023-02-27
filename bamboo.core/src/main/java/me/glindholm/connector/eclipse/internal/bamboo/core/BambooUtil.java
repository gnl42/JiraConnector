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

package me.glindholm.connector.eclipse.internal.bamboo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooLocalConfiguration;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;

/**
 * Provides utility methods for Bamboo.
 *
 * @author Shawn Minto
 * @author Jacek Jaroczynski
 */
public final class BambooUtil {

    public interface BuildChangeAction {
        void run(BambooBuild build, TaskRepository repository);
    }

    private static final String KEY_SUBSCRIBED_PLANS = "me.glindholm.connector.eclipse.bamboo.subscribedPlans";

    public static final String KEY_USE_FAVOURITES = "me.glindholm.connector.eclipse.bamboo.useFavourites";

    private static final String KEY_PLAN_BRANCHES = "me.glindholm.connector.eclipse.bamboo.planBranches";

    private BambooUtil() {
    }

    public static void setSubcribedPlans(final TaskRepository repository, final Collection<SubscribedPlan> plans) {
        final StringBuilder sb = new StringBuilder();
        for (final SubscribedPlan plan : plans) {
            sb.append(plan.getKey());
            sb.append(",");
        }
        repository.setProperty(KEY_SUBSCRIBED_PLANS, sb.toString());
    }

    public static Collection<SubscribedPlan> getSubscribedPlans(final TaskRepository repository) {
        final Collection<SubscribedPlan> plans = new ArrayList<>();
        final String value = repository.getProperty(KEY_SUBSCRIBED_PLANS);
        if (value != null) {
            final StringTokenizer t = new StringTokenizer(value, ",");
            while (t.hasMoreTokens()) {
                plans.add(new SubscribedPlan(t.nextToken()));
            }
        }
        return plans;
    }

    public static boolean isSameBuildPlan(final BambooBuild buildOne, final BambooBuild buildTwo) {
        if (!buildOne.getServerUrl().equals(buildTwo.getServerUrl())) {
            return false;
        }
        // check if same planKey
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

    public static String getUrlFromBuild(final BambooBuild build) {
        return build.getBuildUrl() + "-" + build.getNumber();
//		StringBuilder builder = new StringBuilder();
//		builder.append(build.getServerUrl());
//		builder.append("/build/viewBuildResults.action?buildKey=");
//		builder.append(build.getPlanKey());
//		builder.append("&buildNumber=");
//		builder.append(build.getNumber());
//		// ignore
//		return builder.toString();
    }

    public static void runActionForChangedBuild(final BuildsChangedEvent event, final BambooUtil.BuildChangeAction buildChangeAction) {
        if (event.getChangedBuilds().size() > 0) {
            for (final TaskRepository key : event.getChangedBuilds().keySet()) {
                for (final BambooBuild build : event.getChangedBuilds().get(key)) {
                    // for each build get equivalent old build
                    for (final BambooBuild oldBuild : event.getOldBuilds().get(key)) {
                        if (isSameBuildPlan(build, oldBuild)) {
                            if (build.getStatus() != oldBuild.getStatus()) {
                                // build status changed
                                buildChangeAction.run(build, key);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isUseFavourites(final TaskRepository taskRepository) {
        if (taskRepository == null) {
            return false;
        }
        return Boolean.parseBoolean(taskRepository.getProperty(KEY_USE_FAVOURITES));
    }

    public static void setUseFavourites(final TaskRepository taskRepository, final boolean useFavourite) {
        taskRepository.setProperty(BambooUtil.KEY_USE_FAVOURITES, Boolean.toString(useFavourite));
    }

    public static void setPlanBranches(final TaskRepository taskRepository, final PlanBranches value) {
        taskRepository.setProperty(BambooUtil.KEY_PLAN_BRANCHES, value.getText());
    }

    public static PlanBranches getPlanBranches(final TaskRepository taskRepository) {
        if (taskRepository == null) {
            return PlanBranches.NO;
        }
        return PlanBranches.from(taskRepository.getProperty(BambooUtil.KEY_PLAN_BRANCHES));
    }

    public static BambooLocalConfiguration getLocalConfiguration(final TaskRepository repository) {
        final BambooLocalConfiguration configuration = new BambooLocalConfiguration();

        return configuration;
    }
}
