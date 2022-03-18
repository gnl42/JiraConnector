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

package com.atlassian.connector.commons.jira.beans;

import java.util.Calendar;

/**
 * User: kalamon
 * Date: Sep 2, 2009
 * Time: 4:31:44 PM
 */
public class JIRAAttachment {
    private final String id;
    private final String author;
    private final String filename;
    private final Long filesize;
    private final String mimetype;
    private final Calendar created;

    public JIRAAttachment(String id, String author, String filename, long filesize, String mimetype, Calendar created) {
        this.id = id;
        this.author = author;
        this.filename = filename;
        this.filesize = filesize;
        this.mimetype = mimetype;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getFilename() {
        return filename;
    }

    public Long getFilesize() {
        return filesize;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Calendar getCreated() {
        return created;
    }
}
