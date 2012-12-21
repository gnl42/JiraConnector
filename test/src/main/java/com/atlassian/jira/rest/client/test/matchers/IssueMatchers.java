/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.test.matchers;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.google.common.collect.Lists;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import java.util.Collection;

public class IssueMatchers {
	public static Matcher<? super BasicIssue> withIssueKey(String issueKey) {
		return new FeatureMatcher<BasicIssue, String>(Matchers.is(issueKey), "issue with key that", "key") {

			@Override
			protected String featureValueOf(BasicIssue basicIssue) {
				return basicIssue.getKey();
			}
		};
	}

	public static Matcher<Iterable<? extends BasicIssue>> issuesWithKeys(String... keys) {
		final Collection<Matcher<? super BasicIssue>> matchers = Lists.newArrayListWithCapacity(keys.length);
		for (String key : keys) {
			matchers.add(withIssueKey(key));
		}
		return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers);
	}
}
