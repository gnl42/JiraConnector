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

import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface BuildDetails {
	/**
	 * @deprecated in Bamboo 2.4+ (new more Restish API) the concept of vcs revision key does not exist
	 * @return
	 */
	@Deprecated
	@Nullable
	String getVcsRevisionKey();
	List<TestDetails> getSuccessfulTestDetails();
	List<TestDetails> getFailedTestDetails();
	List<BambooChangeSet> getCommitInfo();

	List<BambooJob> getJobs();
	List<BambooJob> getEnabledJobs();
}
