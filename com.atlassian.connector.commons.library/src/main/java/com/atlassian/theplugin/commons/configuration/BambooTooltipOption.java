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
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-25
 * Time: 11:50:29
 * To change this template use File | Settings | File Templates.
 */
public enum BambooTooltipOption {
	ALL_FAULIRES_AND_FIRST_SUCCESS {
		public String toString() {
			return "All build failures and first build success";
		}
	},

	FIRST_FAILURE_AND_FIRST_SUCCESS {
		public String toString() {
			return "First build failure and first build success";
		}
	},

	NEVER {
		public String toString() {
			return "Never";
		}
	};

	public static BambooTooltipOption valueOfAlias(String optionText) {
		for (BambooTooltipOption option : BambooTooltipOption.values()) {
			if (option.toString().equals(optionText)) {
				return option;
			}
		}
		return null;
	}
}
