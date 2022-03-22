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

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class BambooConfigurationBean {

	private BambooTooltipOption bambooTooltipOption;
	private static final int DEFAULT_POLLING_INTERVAL_MIN = 10;
	private int pollTime = DEFAULT_POLLING_INTERVAL_MIN;
	private static final int HASHCODE_MAGIC = 31;
	private boolean onlyMyBuilds = false;

	public BambooConfigurationBean() {
    }

	public BambooConfigurationBean(BambooConfigurationBean cfg) {
        this.bambooTooltipOption = cfg.getBambooTooltipOption();
        this.pollTime = cfg.getPollTime();
		this.onlyMyBuilds = cfg.isOnlyMyBuilds();
	}

	public boolean isOnlyMyBuilds() {
		return onlyMyBuilds;
	}

	public void setOnlyMyBuilds(final boolean onlyMyBuilds) {
		this.onlyMyBuilds = onlyMyBuilds;
	}

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	public BambooTooltipOption getBambooTooltipOption() {
		return bambooTooltipOption;
	}

	public void setBambooTooltipOption(BambooTooltipOption bambooTooltipOption) {
		this.bambooTooltipOption = bambooTooltipOption;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BambooConfigurationBean)) {
			return false;
		}

		final BambooConfigurationBean that = (BambooConfigurationBean) o;

		if (pollTime != that.pollTime) {
			return false;
		}

		if (bambooTooltipOption != that.bambooTooltipOption) {
			return false;
		}

		if (onlyMyBuilds != that.onlyMyBuilds) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (bambooTooltipOption != null ? bambooTooltipOption.hashCode() : 0);
		result = HASHCODE_MAGIC * result + pollTime;
		result = HASHCODE_MAGIC * result + (onlyMyBuilds ? 1 : 0);

		return result;
	}
}
