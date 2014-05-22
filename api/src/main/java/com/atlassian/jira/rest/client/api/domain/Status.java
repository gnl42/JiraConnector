/*
 * Copyright (C) 2010 Atlassian
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

package com.atlassian.jira.rest.client.api.domain;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * Complete information about a JIRA issue status.
 *
 * @since v0.1
 */
public class Status extends BasicStatus {
	public Status(URI self, @Nullable final Long id, final String name,
                  @Nullable final String description, @Nullable final URI iconUrl) {
		super(self, id, name, description, iconUrl);
	}

    /**
     * Backward compatible constructor
     * @deprecated
     */
    @Deprecated
    public Status(URI self, final String name, final String description, final URI iconUrl) {
        this(self, null, name, description, iconUrl);
    }
}
