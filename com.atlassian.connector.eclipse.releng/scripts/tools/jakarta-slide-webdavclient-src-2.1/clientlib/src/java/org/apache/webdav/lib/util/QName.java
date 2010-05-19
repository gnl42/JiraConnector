/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/util/QName.java,v 1.1 2004/08/02 15:45:49 unico Exp $
 * $Revision: 1.1 $
 * $Date: 2004/08/02 15:45:49 $
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
 *
 */ 

package org.apache.webdav.lib.util;

import org.w3c.dom.Node;

/**
 * A <code>QName</code> represents a fully-qualified name.
 */
public class QName
{
	private String namespaceURI;
	private String localName;
	private int hashCode;

	public QName(String namespaceURI, String localName)
	{
		this.namespaceURI = (namespaceURI == null ? "" : namespaceURI).intern();
		this.localName = localName.intern();
		
		String hash1 = this.namespaceURI.hashCode() + "";
		String hash2 = this.localName.hashCode() + "";
		String hash3 = hash1 + '_' + hash2;
		this.hashCode=hash3.hashCode();
	}

	public String getNamespaceURI()
	{
		return this.namespaceURI;
	}

	public String getLocalName()
	{
		return this.localName;
	}

	public int hashCode()
	{
		return this.hashCode;
	}

	public boolean equals(Object obj)
	{
		return (obj != null
			&& (obj instanceof QName)
	        && namespaceURI == ((QName)obj).getNamespaceURI()
	        && localName == ((QName)obj).getLocalName());
	}

	public boolean matches(Node node)
	{
		return (node!=null)
					&& (node.getNamespaceURI()!=null)
					&& (node.getLocalName()!=null)
					&& (node.getNamespaceURI().intern()==this.namespaceURI)
					&& (node.getLocalName().intern()==this.localName);
	}

	public String toString()
	{
		return namespaceURI + ':' + localName;
	}
}
