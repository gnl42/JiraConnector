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

package me.glindholm.jira.rest.client.api.domain.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class allows to register {@link ValueTransformer} objects and then perform value
 * transformation using registered transformers by invoking
 * {@link ValueTransformerManager#apply(Object)}.
 *
 * @since v1.0
 */
public class ValueTransformerManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public final List<ValueTransformer> valueTransformers = new ArrayList<>();

    public ValueTransformerManager() {
    }

    /**
     * Registers new transformer at the end of list so it will be processed after existing transformers.
     *
     * @param transformer Transformer to register
     * @return this
     */
    public ValueTransformerManager registerTransformer(final ValueTransformer transformer) {
        valueTransformers.add(transformer);
        return this;
    }

    /**
     * Registers new transformer at the beginning of list so it will be processed before existing
     * transformers.
     *
     * @param transformer Transformer to register
     * @return this
     */
    @SuppressWarnings("unused")
    public ValueTransformerManager registerTransformerAsFirst(final ValueTransformer transformer) {
        valueTransformers.add(0, transformer);
        return this;
    }

    /**
     * Use registered transformers to transform given value.
     *
     * @param rawInput Value to transform
     * @return transformed value
     * @throws CannotTransformValueException when any of available transformers was able to transform
     *                                       given value
     */
    public Object apply(@Nullable final Object rawInput) {
        if (rawInput instanceof List) {
            @SuppressWarnings("unchecked")
            final List<Object> rawInputObjects = (List<Object>) rawInput;
            return rawInputObjects.stream().map(this::apply).collect(Collectors.toUnmodifiableList());
            // return ImmutableList.copyOf(Lists.transform(rawInputObjects, this));
        }

        for (final ValueTransformer valueTransformer : valueTransformers) {
            final Object transformedValue = valueTransformer.apply(rawInput);
            if (!ValueTransformer.CANNOT_HANDLE.equals(transformedValue)) {
                return transformedValue;
            }
        }

        throw new CannotTransformValueException("Any of available transformers was able to transform given value. Value is: "
                + (rawInput == null ? "NULL" : rawInput.getClass().getName() + ": " + rawInput.toString()));
    }
}
