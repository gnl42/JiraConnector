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

package com.atlassian.theplugin.commons.cfg;

/**
 * This class is immutable
 */
public final class SubscribedPlan {
	private final String key;
    private boolean grouped;


    public SubscribedPlan(final SubscribedPlan cfg) {
        key = cfg.getKey();
        this.grouped = cfg.isGrouped();
	}

    public SubscribedPlan(final String key, final boolean grouped) {
        this.key = key;
        this.grouped = grouped;
    }

	public SubscribedPlan(final String key) {
		this(key, false);
	}

    public String getKey() {
		return key;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubscribedPlan that = (SubscribedPlan) o;

        if (grouped != that.grouped) {
            return false;
        }

        if (!key.equals(that.key)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (grouped ? 1 : 0);
        return result;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }
}
