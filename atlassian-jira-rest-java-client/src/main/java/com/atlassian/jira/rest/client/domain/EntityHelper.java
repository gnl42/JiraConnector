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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.IdentifiedEntity;
import com.atlassian.jira.rest.client.NamedEntity;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Helper class for entities.
 *
 * @since v1.0
 */
public class EntityHelper {

	public static Iterable<String> toNamesList(Iterable<? extends NamedEntity> items) {
		return Iterables.transform(items, new Function<NamedEntity, String>() {
			@Override
			public String apply(NamedEntity from) {
				return from.getName();
			}
		});
	}

	public static <T> Iterable<String> toStringIdList(Iterable<IdentifiedEntity<T>> items) {
		return Iterables.transform(items, new Function<IdentifiedEntity<T>, String>() {
			@Override
			public String apply(IdentifiedEntity<T> from) {
				return from.getId() == null ? null : from.getId().toString();
			}
		});
	}

	public static <T extends NamedEntity> T findEntityByName(Iterable<T> entities, final String name) {
		return Iterables.find(entities, HasNamePredicate.forName(name));
	}

	public static <T extends IdentifiedEntity<K>, K> T findEntityById(Iterable<T> entities, final K id) {
		return Iterables.find(entities, HasIdPredicate.forId(id));
	}


	public static class HasNamePredicate<T extends NamedEntity> implements Predicate<T> {

		private final String name;

		public static <K extends NamedEntity> HasNamePredicate<K> forName(String name) {
			return new HasNamePredicate<K>(name);
		}

		private HasNamePredicate(String name) {
			this.name = name;
		}

		@Override
		public boolean apply(T input) {
			return name.equals(input.getName());
		}
	}

	public static class HasIdPredicate<T extends IdentifiedEntity<K>, K> implements Predicate<T> {

		private final K id;

		public static <X extends IdentifiedEntity<Y>, Y> HasIdPredicate<X, Y> forId(Y id) {
			return new HasIdPredicate<X, Y>(id);
		}

		private HasIdPredicate(K id) {
			this.id = id;
		}

		@Override
		public boolean apply(T input) {
			return id.equals(input.getId());
		}
	}
}
