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

package me.glindholm.jira.rest.client.api.domain;

import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Helper class for entities.
 *
 * @since v1.0
 */
public class EntityHelper {

    public static Function<IdentifiableEntity<String>, String> GET_ENTITY_STRING_ID_FUNCTION = new Function<>() {
        @Override
        public String apply(IdentifiableEntity<String> entity) {
            return entity.getId();
        }
    };

    public static Function<NamedEntity, String> GET_ENTITY_NAME_FUNCTION = new Function<>() {
        @Override
        public String apply(NamedEntity entity) {
            return entity.getName();
        }
    };

    public static List<String> toNamesList(List<? extends NamedEntity> items) {
        return Lists.transform(items, GET_ENTITY_NAME_FUNCTION);
    }

    public static List<String> toFileNamesList(List<? extends Attachment> attachments) {
        return Lists.transform(attachments, new Function<Attachment, String>() {
            @Override
            public String apply(Attachment a) {
                return a.getFilename();
            }
        });
    }

    public static <T extends IdentifiableEntity<K>, K> T findEntityById(List<T> entities, final K id) {
        T namedEntity = entities.stream().filter(entity -> entity.getId().equals(id)).findAny().orElse(null);
        if (namedEntity == null) {
            throw new NoSuchElementException(String.format("Entity with id \"%s\" not found. Entities: %s", id, entities.toString()));
        }
        return namedEntity;
    }

    @SuppressWarnings("unused")
    public static <T> List<String> toStringIdList(List<IdentifiableEntity<T>> items) {
        return Lists.transform(items, new Function<IdentifiableEntity<T>, String>() {
            @Override
            public String apply(IdentifiableEntity<T> from) {
                return from.getId() == null ? null : from.getId().toString();
            }
        });
    }

    public static <T extends NamedEntity> T findEntityByName(List<T> entities, final String name) {
        //            return Lists.find(entities, HasNamePredicate.forName(name));
        T namedEntity = entities.stream().filter(entity -> entity.getName().equals(name)).findAny().orElse(null);
        if (namedEntity == null) {
            throw new NoSuchElementException(String.format("Entity with name \"%s\" not found. Entities: %s", name, entities
                    .toString()));
        }
        return namedEntity;
    }

    public static Attachment findAttachmentByFileName(List<Attachment> attachments, final String fileName) {
        return attachments.stream().filter(attachment -> attachment.getFilename().equals(fileName)).findAny().orElse(null);
        //        return Lists.find(attachments, HasFileNamePredicate.forFileName(fileName));
    }



    //    public static class HasIdPredicate<T extends IdentifiableEntity<K>, K> implements Serializable, Predicate<T> {
    //        private static final long serialVersionUID = 1L;
    //
    //        private final K id;
    //
    //        public static <X extends IdentifiableEntity<Y>, Y> HasIdPredicate<X, Y> forId(Y id) {
    //            return new HasIdPredicate<>(id);
    //        }
    //
    //        private HasIdPredicate(K id) {
    //            this.id = id;
    //        }
    //
    //        @Override
    //        public boolean apply(T input) {
    //            return id.equals(input.getId());
    //        }
    //    }
    //
    //    public static class AddressEndsWithPredicate implements Serializable, Predicate<AddressableEntity> {
    //        private static final long serialVersionUID = 1L;
    //
    //        private final String stringEnding;
    //
    //        public AddressEndsWithPredicate(String stringEnding) {
    //            this.stringEnding = stringEnding;
    //        }
    //
    //        @Override
    //        public boolean apply(final AddressableEntity input) {
    //            return input.getSelf().getPath().endsWith(stringEnding);
    //        }
    //    }
}
