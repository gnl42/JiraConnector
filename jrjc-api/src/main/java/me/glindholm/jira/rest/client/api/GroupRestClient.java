/*
 * Copyright (C) 2018 Atlassian
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

package me.glindholm.jira.rest.client.api;

import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import me.glindholm.jira.rest.client.api.domain.Group;

/**
 * The me.glindholm.jira.rest.client.api handling group resources.
 *
 * @since v5.1.0
 */
public interface GroupRestClient {

    /**
     * Find all groups, limited by the system property "jira.ajax.autocomplete.limit"
     *
     * @return list of groups
     * @throws URISyntaxException
     */
    CompletableFuture<List<Group>> findGroups() throws URISyntaxException;

    /**
     * Returns groups with substrings matching a given query.
     */
    CompletableFuture<List<Group>> findGroups(@Nullable String query, @Nullable String exclude, @Nullable Integer maxResults, @Nullable String userName)
            throws URISyntaxException;

}
