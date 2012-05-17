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
package com.atlassian.jira.rest.client.annotation;

import java.lang.annotation.Annotation;

public class Annotations {
	public static <T extends Annotation> T getAnnotationIncludingParents(final Class<?> aClass, final Class<T> annotationClass) {
		Class<?> classToScan = aClass;
		while (classToScan != null) {
			if (classToScan.isAnnotationPresent(annotationClass)) {
				return classToScan.getAnnotation(annotationClass);
			}
			classToScan = classToScan.getSuperclass();
		}
		return null;
	}
}
