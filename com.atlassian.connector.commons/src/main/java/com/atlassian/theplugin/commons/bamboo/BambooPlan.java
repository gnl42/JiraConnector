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

package com.atlassian.theplugin.commons.bamboo;

import java.io.Serializable;

/**
 * This class is immutable
 */
public class BambooPlan implements Serializable {
	private final String name;
	private final String key;
    private final String materPlanKey;
	private final boolean favourite;
	private final boolean enabled;
	private final String projectName;
	private final String projectKey;
	private final Integer averageBuildTime;
	private final PlanState state;

	public BambooPlan(String name, String key, String masterPlanKey) {
		this(name, key, masterPlanKey, true);
	}

	public BambooPlan(String name, String key, String masterPlanKey, boolean isEnabled) {
		this(name, key, masterPlanKey, isEnabled, false);
	}

	public BambooPlan(String name, String key, String masterPlanKey, boolean isEnabled, boolean isFavourite) {
		this(name, key, masterPlanKey, isEnabled, isFavourite, "", "", null, false, false);
	}

	public BambooPlan(final String name, final String key, final String masterPlanKey, final boolean isEnabled, final Boolean isFavourite,
			final String projectName, final String projectKey, final Integer averageBuildTime, final boolean inQueue,
			final boolean building) {
		this.name = name;
		this.key = key;
        this.materPlanKey = masterPlanKey;
		this.enabled = isEnabled;
		this.favourite = isFavourite;
		this.projectName = projectName;
		this.projectKey = projectKey;
		this.averageBuildTime = averageBuildTime;

		if (building) {
			this.state = PlanState.BUILDING;
		} else if (inQueue) {
			this.state = PlanState.IN_QUEUE;
		} else {
			this.state = PlanState.STANDING;
		}
	}


	public String getName() {
		return this.name;
	}

	public String getKey() {
		return this.key;
	}

    public String getMaterPlanKey() {
        return materPlanKey;
    }

    public boolean isFavourite() {
		return favourite;
	}

	/**
	 * Returns copy of this object with favourite information set.
	 *
	 * @param isFavourite
	 *            requested favourite state
	 * @return copy of this object
	 */
	public BambooPlan withFavourite(boolean isFavourite) {
		return new BambooPlan(name, key, materPlanKey, enabled, isFavourite);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public Integer getAverageBuildTime() {
		return averageBuildTime;
	}

	public PlanState getState() {
		return state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BambooPlan)) {
			return false;
		}

		BambooPlan that = (BambooPlan) o;

		//noinspection RedundantIfStatement
		if (key != null ? !key.equals(that.key) : that.key != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (key != null ? key.hashCode() : 0);
	}
}
