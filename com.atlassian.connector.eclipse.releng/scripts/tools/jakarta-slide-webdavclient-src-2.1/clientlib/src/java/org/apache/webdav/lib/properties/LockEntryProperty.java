/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/properties/LockEntryProperty.java,v 1.3 2004/07/28 09:30:35 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:35 $
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
package org.apache.webdav.lib.properties;

import org.apache.webdav.lib.Property;

/**
 * @version $Revision: 1.3 $
 */
public interface LockEntryProperty extends Property {

    public static final short TYPE_WRITE = 0;

    public static final short SCOPE_EXCLUSIVE = 0;
    public static final short SCOPE_SHARED = 1;

    public abstract short getScope();
    public abstract short getType();
}
