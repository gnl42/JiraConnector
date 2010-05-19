/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/Privilege.java,v 1.3 2004/07/28 09:31:40 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:40 $
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
 * This interface models a DAV ACE privilege.
 *
 * @version $Revision: 1.3 $
 */
public class Privilege {


    // -------------------------------------------------------------- Constants


    // Standard WebDAV ACP privileges
    public static final Privilege ALL =
        new Privilege(Constants.DAV, "all", null);
    public static final Privilege READ =
        new Privilege(Constants.DAV, "read", null);
    public static final Privilege WRITE =
        new Privilege(Constants.DAV, "write", null);
    public static final Privilege READ_ACL =
        new Privilege(Constants.DAV, "read-acl", null);
    public static final Privilege WRITE_ACL =
        new Privilege(Constants.DAV, "write-acl", null);

    // TODO: Add the Slide specific privileges ?


    // ----------------------------------------------------------- Constructors


    public Privilege(String namespace, String name, String parameter) {
        this.namespace = namespace;
        this.name = name;
        this.parameter = parameter;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Custom privilege namespace.
     */
    protected String namespace;


    /**
     * Custom privilege element name.
     */
    protected String name;


    /**
     * Additional parameter (usually, an URI).
     */
    protected String parameter;


    // ------------------------------------------------------------- Properties


    /**
     * Namespace accessor.
     */
    public String getNamespace() {
        return namespace;
    }


    /**
     * Name accessor.
     */
    public String getName() {
        return name;
    }


    /**
     * Parameter accessor.
     */
    public String getParameter() {
        return parameter;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Equals.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof Privilege)) {
            return false;
        } else {
            if (this == obj)
                return true;
            Privilege privilege = (Privilege) obj;
            if ((namespace.equals(privilege.getNamespace()))
                && (name.equals(privilege.getName()))) {
                if (parameter == null) {
                    if (privilege.getParameter() == null)
                        return true;
                } else {
                    if (privilege.getParameter() != null)
                        return (parameter.equals(privilege.getParameter()));
                }
            }
        }
        return false;
    }


}
