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

public class CrucibleConfigurationBean {
	public static final int MIN_SCHEDULE_TIME_MINUTES = 1;
	private CrucibleTooltipOption crucibleTooltipOption;
	private static final int DEFAULT_POLLING_INTERVAL_MIN = 10;
    private int pollTime = DEFAULT_POLLING_INTERVAL_MIN;
	private static final int HASHCODE_MAGIC = 31;
    private static final int DEFAULT_REVIEW_CREATION_TIMEOUT = 5;
    private static final int MIN_REVIEW_CREATION_TIMEOUT = 1;
    private int reviewCreationTimeout = DEFAULT_REVIEW_CREATION_TIMEOUT;
    public static final int DEFAULT_REVIEW_FILE_CACHE_SIZE = 10;
    public static final int MIN_REVIEW_FILE_CACHE_SIZE = 10;
    public static final int MAX_REVIEW_FILE_CACHE_SIZE = 128;
    private int reviewFileCacheSize = DEFAULT_REVIEW_FILE_CACHE_SIZE;

    public CrucibleConfigurationBean() {
    }

	public CrucibleConfigurationBean(CrucibleConfigurationBean cfg) {
        crucibleTooltipOption = (cfg).getCrucibleTooltipOption();
		pollTime = cfg.getPollTime();
        reviewCreationTimeout = cfg.getReviewCreationTimeout();
        reviewFileCacheSize = cfg.getReviewFileCacheSize();
    }

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime > MIN_SCHEDULE_TIME_MINUTES ? pollTime : MIN_SCHEDULE_TIME_MINUTES;
	}

    public int getReviewCreationTimeout() {
        return reviewCreationTimeout;
    }

    public void setReviewCreationTimeout(int reviewCreationTimeout) {
        this.reviewCreationTimeout = reviewCreationTimeout > MIN_REVIEW_CREATION_TIMEOUT
                ? reviewCreationTimeout : MIN_REVIEW_CREATION_TIMEOUT;
    }

    public CrucibleTooltipOption getCrucibleTooltipOption() {
		return crucibleTooltipOption;
	}

	public void setCrucibleTooltipOption(CrucibleTooltipOption crucibleTooltipOption) {
		this.crucibleTooltipOption = crucibleTooltipOption;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CrucibleConfigurationBean)) {
			return false;
		}

		final CrucibleConfigurationBean that = (CrucibleConfigurationBean) o;

		if (pollTime != that.pollTime) {
			return false;
		}
        if (reviewCreationTimeout != that.reviewCreationTimeout) {
            return false;
        }
		if (crucibleTooltipOption != that.crucibleTooltipOption) {
			return false;
		}

        if (reviewFileCacheSize != that.reviewFileCacheSize) {
            return false;
        }

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (crucibleTooltipOption != null ? crucibleTooltipOption.hashCode() : 0);
		result = HASHCODE_MAGIC * result + pollTime;
        result = HASHCODE_MAGIC * result + reviewCreationTimeout;
        result = HASHCODE_MAGIC * result + reviewFileCacheSize;
		return result;
	}

    public void setReviewFileCacheSize(int cacheSize) {
        this.reviewFileCacheSize = cacheSize > MIN_REVIEW_FILE_CACHE_SIZE && cacheSize < MAX_REVIEW_FILE_CACHE_SIZE
                ? cacheSize : DEFAULT_REVIEW_FILE_CACHE_SIZE;
    }

    public int getReviewFileCacheSize() {
        return reviewFileCacheSize;
    }
}