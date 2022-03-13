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
 * limitations under the License..
 */

package com.atlassian.theplugin.commons.bamboo;

public enum AdjustedBuildStatus {
	/** Build finished successfully */
    SUCCESS("Succeeded"),
	/** Build failed, e.g. due to compilation or unit test failure */
    FAILURE("Failed"),
	DISABLED("Disabled"),
	/** Status unknown due to data retrieval problem */
    UNKNOWN("Unknown");

	private String name;

	AdjustedBuildStatus(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
