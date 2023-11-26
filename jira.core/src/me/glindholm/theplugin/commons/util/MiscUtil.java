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
package me.glindholm.theplugin.commons.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;

public final class MiscUtil {

    private MiscUtil() {
        // this is utility class
    }

    public static <K, V> HashMap<K, V> buildHashMap() {
        return new HashMap<>();
    }

    public static <K, V> AbstractMap<K, V> buildConcurrentHashMap(final int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    public static <T> boolean isModified(@Nullable final T a, @Nullable final T b) {
        return a != null ? !a.equals(b) : b != null;
    }

    public static <T> boolean isEqual(@Nullable final T a, @Nullable final T b) {
        return a != null ? a.equals(b) : b == null;
    }

    public static <T> Set<T> buildHashSet() {
        return new HashSet<>();
    }

    @SafeVarargs
    public static <T> Set<T> buildHashSet(final T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> Set<T> buildLinkedHashSet(final T... elements) {
        return new LinkedHashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> TreeSet<T> buildTreeSet(final T... elements) {
        return new TreeSet<>(Arrays.asList(elements));
    }

    public static <T> Set<T> buildLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    public static <T> TreeSet<T> buildTreeSet() {
        return new TreeSet<>();
    }

    public static <T> Set<T> buildHashSet(final Collection<T> elements) {
        return new HashSet<>(elements);
    }

    public static <T> TreeSet<T> buildTreeSet(final Collection<T> elements) {
        return new TreeSet<>(elements);
    }

    public static <T> ArrayList<T> buildArrayList() {
        return new ArrayList<>();
    }

    public static <T> ArrayList<T> buildArrayList(final int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    /**
     * Creates mutable {@link java.util.ArrayList} from given elements.
     *
     * @param elements elements which will be included in the newly created ArrayList
     * @return newly created ArrayList
     */
    public static <T> List<T> buildArrayList(final T... elements) {
        final List<T> tmp = new ArrayList<>(Arrays.asList(elements));
        return tmp;
    }

    public static <T> ArrayList<T> buildArrayList(final Collection<T> collection) {
        return new ArrayList<>(collection);
    }
}
