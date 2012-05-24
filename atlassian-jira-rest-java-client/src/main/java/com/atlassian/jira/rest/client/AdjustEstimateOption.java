/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client;

public class AdjustEstimateOption {
	public static enum AdjustEstimate {
		NEW,
		LEAVE,
		MANUAL,
		AUTO;

		public final String restValue;

		private AdjustEstimate() {
			restValue = this.name().toLowerCase();
		}
	}

	public final String newEstimate;
	public final String reduceBy;
	public final AdjustEstimate adjustEstimate;

	public static AdjustEstimateOption setNew(String estimate) {
		return new AdjustEstimateOption(AdjustEstimate.NEW, estimate, null);
	}

	public static AdjustEstimateOption leave() {
		return new AdjustEstimateOption(AdjustEstimate.LEAVE, null, null);
	}

	public static AdjustEstimateOption manual(String reduceBy) {
		return new AdjustEstimateOption(AdjustEstimate.MANUAL, null, reduceBy);
	}

	public static AdjustEstimateOption auto() {
		return new AdjustEstimateOption(AdjustEstimate.AUTO, null, null);
	}

	public AdjustEstimateOption(AdjustEstimate adjustEstimate, String newEstimate, String reduceBy) {
		this.newEstimate = newEstimate;
		this.reduceBy = reduceBy;
		this.adjustEstimate = adjustEstimate;
	}
}
