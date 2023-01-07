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
package me.glindholm.connector.commons.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

/**
 * This class is immutable
 */
public class IntRanges {
    @NonNull
    private final ArrayList<IntRange> ranges;
    private static final Comparator<IntRange> COMPARATOR = new Comparator<>() {
        @Override
        public int compare(final IntRange o1, final IntRange o2) {
            return o1.getMin() - o2.getMin();
        }
    };

    /**
     * @param ranges list cannot be empty. Copy is made here, so you may freely
     *               modify the array afterwards
     */
    public IntRanges(@NonNull final List<IntRange> ranges) {
        this.ranges = new ArrayList<>(ranges);
        if (this.ranges.isEmpty()) {
            throw new IllegalArgumentException("Cannot create ranges object from the empty list");
        }
        Collections.sort(this.ranges, COMPARATOR);
    }

    public IntRanges(@NonNull final IntRange... ranges) {
        if (ranges.length == 0) {
            throw new IllegalArgumentException("Cannot create ranges object from the empty list");
        }
        this.ranges = new ArrayList<>(Arrays.asList(ranges));
        Collections.sort(this.ranges, COMPARATOR);
    }

    public int getTotalMin() {
        return ranges.get(0).getMin();
    }

    public int getTotalMax() {
        return ranges.get(ranges.size() - 1).getMax();
    }

    @NonNull
    public List<IntRange> getRanges() {
        return Collections.unmodifiableList(ranges);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final IntRanges intRanges = (IntRanges) o;

        // noinspection RedundantIfStatement
        if (!ranges.equals(intRanges.ranges)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ranges.hashCode();
    }

    @Override
    public String toString() {
        return "[" + StringUtils.join(ranges, ',') + "]";
    }

    public String toNiceString() {
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<IntRange> it = ranges.iterator(); it.hasNext();) {
            sb.append(it.next().toNiceString());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
