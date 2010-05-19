/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavException.java,v 1.3 2004/07/28 09:31:39 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:39 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
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
 *
 */

package org.apache.webdav.lib;

/**
 * Wraps other Exceptions into RuntimeException. It indicates
 * some server access problems or implementation shortcomings
 *
 */
public class WebdavException extends RuntimeException {

  /** Method not implemented for Webdav */
  public static final String NOT_IMPLEMENTED = "this method is not implemented";

  /** Method not implemented for Webdav */
  public static final String RELATIVE_FILE = "this method is not supported with relative paths";

  public WebdavException(Exception e) {
    super(e.getMessage());
  }

  public WebdavException(String msg) {
    super(msg);
  }
}
