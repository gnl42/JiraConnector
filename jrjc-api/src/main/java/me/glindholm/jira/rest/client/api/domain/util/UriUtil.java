/*
 * Copyright (C) 2014 Atlassian
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

package me.glindholm.jira.rest.client.api.domain.util;

import java.net.URI;

public class UriUtil {

    public static URI path(final URI uri, final String path) {
        final String uriString = uri.toString();
        final StringBuilder sb = new StringBuilder(uriString);
        if (!uriString.endsWith("/")) {
            sb.append('/');
        }
        sb.append(path.startsWith("/") ? path.substring(1) : path);
        return URI.create(sb.toString());
    }
}
