/*
 * Copyright (C) 2013 Atlassian
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

import com.atlassian.jira.rest.client.api.NamedEntity;
import com.google.common.collect.Lists;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import java.util.Collection;

public class NamedEntityMatchers {
	public static Matcher<? super NamedEntity> withName(String name) {
		return new FeatureMatcher<NamedEntity, String>(Matchers.is(name), "entity with name that", "name") {

			@Override
			protected String featureValueOf(NamedEntity namedEntity) {
				return namedEntity.getName();
			}
		};
	}

	public static Matcher<Iterable<? extends NamedEntity>> entitiesWithNames(String... names) {
		final Collection<Matcher<? super NamedEntity>> matchers = Lists.newArrayListWithCapacity(names.length);
		for (String key : names) {
			matchers.add(withName(key));
		}
		return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers);
	}
}
