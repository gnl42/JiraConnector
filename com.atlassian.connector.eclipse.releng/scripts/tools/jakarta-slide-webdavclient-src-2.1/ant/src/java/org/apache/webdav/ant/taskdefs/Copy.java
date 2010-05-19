/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Copy.java,v 1.2.2.1 2004/08/15 12:57:17 luetzkendorf Exp $
 * $Revision: 1.2.2.1 $
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
import org.apache.webdav.lib.methods.DepthSupport;

/**
 * WebDAV task for copying resources and collections.
 * 
 * @see <a href="../doc-files/tasks.htm#davcopy">Task documentation</a>
 */
public class Copy extends WebdavMatchingTask {

    private String destination;
    private int depth = DepthSupport.DEPTH_INFINITY;
    private boolean overwrite = false;
    private HttpURL destinationURL;

    /* 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        validate();
        try {
            // TODO assure that the colletion to which we copy does exist
            log("Copying " + getUrl(), ifVerbose());
            Utils.copyResource(
                getHttpClient(), 
                getUrl(),
                this.destinationURL.getURI(),
                this.depth,
                this.overwrite
            );
        }
        catch (IOException e) {
            throw Utils.makeBuildException("Can't copy!", e);
        }
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDepth(String value) {
        if ("0".trim().equals(value)) {
           this.depth = DepthSupport.DEPTH_0;
        }
        else if ("infinity".trim().toLowerCase().equals(value)) {
           this.depth = DepthSupport.DEPTH_INFINITY;
        }
        else {
            throw new BuildException("Invalid value of depth attribute." 
                 + " (One of '0' or 'infinity' expected)");
        }
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
