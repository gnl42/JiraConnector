/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.theplugin.commons.SubscribedPlan;

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

	private static final String KEY_SUBSCRIBED_PLANS = "com.atlassian.connector.eclipse.bamboo.subscribedPlans";

	private BambooUtil() {
	}

	public static void setSubcribedPlans(TaskRepository repository, Collection<SubscribedPlan> plans) {
		StringBuffer sb = new StringBuffer();
		for (SubscribedPlan plan : plans) {
			sb.append(plan.getPlanId());
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

}
