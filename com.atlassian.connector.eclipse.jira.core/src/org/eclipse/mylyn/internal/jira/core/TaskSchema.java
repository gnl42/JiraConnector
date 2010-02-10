/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.EnumSet;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 * @author Steffen Pingel
 */
public class TaskSchema {

	public static class ExtTaskAttribute<T> {

		private final TaskAttribute attribute;

		public ExtTaskAttribute(TaskAttribute attribute) {
			this.attribute = attribute;
		}

		@SuppressWarnings("unchecked")
		public T getValue() {
			return (T) attribute.getValue();
		}

		public void setValue(T value) {
			attribute.setValue(value.toString());
		}

	}

	public static class TaskField<T> {

		private EnumSet<Flag> flags;

		private final String key;

		private final String label;

		private final String type;

		private final String javaKey;

		private final Class<T> clazz;

		public TaskField(Class<T> clazz, String key, String javaKey, String label, String type) {
			this(clazz, key, javaKey, label, type, null);
		}

		public TaskField(Class<T> clazz, String key, String javaKey, String label, String type, Flag firstFlag,
				Flag... moreFlags) {
			this.clazz = clazz;
			this.key = key;
			this.javaKey = javaKey;
			this.label = label;
			this.type = type;
			if (firstFlag == null) {
				this.flags = NO_FLAGS;
			} else {
				this.flags = EnumSet.of(firstFlag, moreFlags);
			}
		}

		public Class<T> getJavaClass() {
			return clazz;
		}

		String javaKey() {
			return javaKey;
		}

		public String key() {
			return key;
		}

		public String getKind() {
			if (flags.contains(Flag.ATTRIBUTE)) {
				return TaskAttribute.KIND_DEFAULT;
			} else if (flags.contains(Flag.PEOPLE)) {
				return TaskAttribute.KIND_PEOPLE;
			}
			return null;
		}

		public String getLabel() {
			return label;
		}

		public String getType() {
			return type;
		}

		public boolean isReadOnly() {
			return flags.contains(Flag.READ_ONLY);
		}

		@Override
		public String toString() {
			return getLabel();
		}

	};

	public enum Flag {
		ATTRIBUTE, EXISTING_ONLY, PEOPLE, READ_ONLY
	}

	public static final EnumSet<Flag> NO_FLAGS = EnumSet.noneOf(Flag.class);

}
