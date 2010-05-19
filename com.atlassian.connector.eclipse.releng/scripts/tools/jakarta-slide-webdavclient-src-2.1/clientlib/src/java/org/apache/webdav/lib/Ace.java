/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/Ace.java,v 1.4.2.1 2004/11/23 12:57:16 unico Exp $
 * $Revision: 1.4.2.1 $
 * $Date: 2004/11/23 12:57:16 $
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

import java.util.Enumeration;
import java.util.Vector;

/**
 * This interface models a DAV Access control entry.
 *
 * @version $Revision: 1.4.2.1 $
 */
public class Ace {
    private static final PropertyName DEFAULT_PROPERTY
        = new PropertyName("DAV:", "owner");

    // ----------------------------------------------------------- Constructors


    public Ace(String principal) {
        this.principal = principal;
    }


    public Ace(String principal, boolean negative, boolean protectedAce,
               boolean inherited, String inheritedFrom) {
        this(principal);
        this.negative = negative;
        this.protectedAce = protectedAce;
        this.inherited = inherited;
        this.inheritedFrom = inheritedFrom;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Principal.
     */
    protected String principal;


    /**
     * Negative (deny) flag.
     */
    protected boolean negative = false;


    /**
     * Privileges this ACE grants or denies.
     */
    protected Vector privileges = new Vector();


    /**
     * Protected.
     */
    protected boolean protectedAce = false;


    /**
     * Inherited.
     */
    protected boolean inherited = false;


    /**
     * Inherited from.
     */
    protected String inheritedFrom = null;


    /**
     * Property. Only used if principal.equals("property").
     */
    protected PropertyName property = null;


    // ------------------------------------------------------------- Properties


    /**
     * Principal accessor.
     */
    public String getPrincipal() {
        return principal;
    }


    /**
     * Principal mutator.
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }


    /**
     * Negative accessor.
     */
    public boolean isNegative() {
        return (negative);
    }


    /**
     * Negative mutator.
     */
    public void setNegative(boolean negative) {
        this.negative = negative;
    }


    /**
     * Protected accessor.
     */
    public boolean isProtected() {
        return (protectedAce);
    }


    /**
     * Protected mutator.
     */
    public void setProtected(boolean protectedAce) {
        this.protectedAce = protectedAce;
    }


    /**
     * Inherited accessor.
     */
    public boolean isInherited() {
        return (inherited);
    }


    /**
     * Inherited mutator.
     */
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }


    /**
     * Inherited from accessor.
     */
    public String getInheritedFrom() {
        return inheritedFrom;
    }


    /**
     * Inherited from mutator.
     */
    public void setInheritedFrom(String inheritedFrom) {
        this.inheritedFrom = inheritedFrom;
    }


    /**
     * Property accessor.
     *
     * @return the property to compare if the pricipal is "property".
     *         If the property has not been set or has been set to null
     *         return "DAV:owner".
     * @see #setProperty(PropertyName)
     */
    public PropertyName getProperty() {
        return property != null ? property : DEFAULT_PROPERTY;
    }


    /**
     * Property mutator.
     *
     * @param property the property to compare if the principal is "property"
     * @see #getProperty()
     */
    public void setProperty(PropertyName property) {
        this.property = property;
    }


    /**
     * Enumerate privileges.
     */
    public Enumeration enumeratePrivileges() {
        return privileges.elements();
    }


    /**
     * Add privilege.
     */
    public void addPrivilege(Privilege privilege) {
        privileges.addElement(privilege);
    }


    /**
     * Remove privilege.
     */
    public boolean removePrivilege(Privilege privilege) {
        return privileges.removeElement(privilege);
    }


    /**
     * Clear privileges.
     */
    public void clearPrivileges() {
        privileges.clear();
    }

    public int hashCode() {
        return toString().hashCode() 
            + (getPrincipal().equals("property") 
                    ? getProperty().hashCode() 
                    : 0);
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof Ace) {
            Ace otherAce = (Ace) o;
            boolean equals = true;
            equals &= isNegative() == otherAce.isNegative();
            equals &= isProtected() == otherAce.isProtected();
            equals &= isInherited() == otherAce.isInherited();
            if (equals && isInherited()) {
                equals = getInheritedFrom().equals(otherAce.getInheritedFrom());
            }
            equals &= getPrincipal().equals(otherAce.getPrincipal());
            if (equals && getPrincipal().equals("property")) {
                equals = getProperty().equals(otherAce.getProperty());
            }
            if (equals) {
                Enumeration privileges = enumeratePrivileges();
                Enumeration otherPrivileges = otherAce.enumeratePrivileges();
                while (equals && privileges.hasMoreElements()) {
                    equals = otherPrivileges.hasMoreElements();
                    // Only access otherPrivileges if there are more elements
                    if (equals)
                    {
                        equals = privileges.nextElement().equals(otherPrivileges.nextElement());
                    }
                }
                if (equals)
                {
                    // No more elements in privileges, so there should be no
                    // more elements in otherPrivileges
                    equals = !otherPrivileges.hasMoreElements();
                }
            }
            return equals;
        }
        return false;
    }

    public String toString() {
        return ((!isNegative()?"granted":"denied") +
        " to " + getPrincipal() +
        " (" + (isProtected()?"protected":"not protected") + ")" +
        " (" + (isInherited()? ("inherited from '" + getInheritedFrom() + "'"): "not inherited") +")");
    }

}
