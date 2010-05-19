/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Move.java,v 1.3.2.1 2004/08/15 12:57:17 luetzkendorf Exp $
 * $Revision: 1.3.2.1 $
 * $Date: 2004/08/15 12:57:17 $
 * ========================================================================
 * Copyright 2004 The Apache Software Foundation
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
 * ========================================================================
 */
package org.apache.webdav.ant.taskdefs;

import java.io.IOException;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;

import org.apache.tools.ant.BuildException;
import org.apache.webdav.ant.Utils;

/**
 * WebDAV task for moving resources and collections.
 * 
 * @see <a href="../doc-files/tasks.htm#davmove">Task documentation</a>
 */
public class Move extends WebdavMatchingTask {

    private String destination;
    private boolean overwrite;
    private HttpURL destinationURL;

    /* 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        validate();
        try {
            log("Moving " + getUrl(), ifVerbose());
            Utils.moveResource(
                getHttpClient(), 
                getUrl(),
                this.destinationURL.getURI(),
                this.overwrite
            );
        }
        catch (IOException e) {
            throw Utils.makeBuildException("Can't move!", e);
        }
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setOverwrite(boolean value) {
        this.overwrite = value;
    }

    protected void validate() {
        super.validate();
        if (destination == null) {
            throw new BuildException("Missing required attribute destination");
        }
        
        try {
            this.destinationURL = Utils.createHttpURL(getUrl(), this.destination);
            this.destinationURL.setPath(removeDoubleSlashes(
                    this.destinationURL.getPath()));
        } catch (URIException e) {
            throw new BuildException("Invalid destination uri!", e);
        }
    }

}
