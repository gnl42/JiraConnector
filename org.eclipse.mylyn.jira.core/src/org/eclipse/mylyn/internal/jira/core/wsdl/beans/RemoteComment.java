/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * RemoteComment.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.wsdl.beans;

@SuppressWarnings( { "unchecked", "serial" })
public class RemoteComment implements java.io.Serializable {
	private java.lang.String author;

	private java.lang.String body;

	private java.util.Calendar created;

	private java.lang.String groupLevel;

	private java.lang.String id;

	private java.lang.String roleLevel;

	public RemoteComment() {
	}

	public RemoteComment(java.lang.String author, java.lang.String body, java.util.Calendar created,
			java.lang.String groupLevel, java.lang.String id, java.lang.String roleLevel) {
		this.author = author;
		this.body = body;
		this.created = created;
		this.groupLevel = groupLevel;
		this.id = id;
		this.roleLevel = roleLevel;
	}

	/**
	 * Gets the author value for this RemoteComment.
	 * 
	 * @return author
	 */
	public java.lang.String getAuthor() {
		return author;
	}

	/**
	 * Sets the author value for this RemoteComment.
	 * 
	 * @param author
	 */
	public void setAuthor(java.lang.String author) {
		this.author = author;
	}

	/**
	 * Gets the body value for this RemoteComment.
	 * 
	 * @return body
	 */
	public java.lang.String getBody() {
		return body;
	}

	/**
	 * Sets the body value for this RemoteComment.
	 * 
	 * @param body
	 */
	public void setBody(java.lang.String body) {
		this.body = body;
	}

	/**
	 * Gets the created value for this RemoteComment.
	 * 
	 * @return created
	 */
	public java.util.Calendar getCreated() {
		return created;
	}

	/**
	 * Sets the created value for this RemoteComment.
	 * 
	 * @param created
	 */
	public void setCreated(java.util.Calendar created) {
		this.created = created;
	}

	/**
	 * Gets the groupLevel value for this RemoteComment.
	 * 
	 * @return groupLevel
	 */
	public java.lang.String getGroupLevel() {
		return groupLevel;
	}

	/**
	 * Sets the groupLevel value for this RemoteComment.
	 * 
	 * @param groupLevel
	 */
	public void setGroupLevel(java.lang.String groupLevel) {
		this.groupLevel = groupLevel;
	}

	/**
	 * Gets the id value for this RemoteComment.
	 * 
	 * @return id
	 */
	public java.lang.String getId() {
		return id;
	}

	/**
	 * Sets the id value for this RemoteComment.
	 * 
	 * @param id
	 */
	public void setId(java.lang.String id) {
		this.id = id;
	}

	/**
	 * Gets the roleLevel value for this RemoteComment.
	 * 
	 * @return roleLevel
	 */
	public java.lang.String getRoleLevel() {
		return roleLevel;
	}

	/**
	 * Sets the roleLevel value for this RemoteComment.
	 * 
	 * @param roleLevel
	 */
	public void setRoleLevel(java.lang.String roleLevel) {
		this.roleLevel = roleLevel;
	}

	private java.lang.Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof RemoteComment))
			return false;
		RemoteComment other = (RemoteComment) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.author == null && other.getAuthor() == null) || (this.author != null && this.author.equals(other.getAuthor())))
				&& ((this.body == null && other.getBody() == null) || (this.body != null && this.body.equals(other.getBody())))
				&& ((this.created == null && other.getCreated() == null) || (this.created != null && this.created.equals(other.getCreated())))
				&& ((this.groupLevel == null && other.getGroupLevel() == null) || (this.groupLevel != null && this.groupLevel.equals(other.getGroupLevel())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.roleLevel == null && other.getRoleLevel() == null) || (this.roleLevel != null && this.roleLevel.equals(other.getRoleLevel())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	@Override
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getAuthor() != null) {
			_hashCode += getAuthor().hashCode();
		}
		if (getBody() != null) {
			_hashCode += getBody().hashCode();
		}
		if (getCreated() != null) {
			_hashCode += getCreated().hashCode();
		}
		if (getGroupLevel() != null) {
			_hashCode += getGroupLevel().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getRoleLevel() != null) {
			_hashCode += getRoleLevel().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			RemoteComment.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("author");
		elemField.setXmlName(new javax.xml.namespace.QName("", "author"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("body");
		elemField.setXmlName(new javax.xml.namespace.QName("", "body"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("created");
		elemField.setXmlName(new javax.xml.namespace.QName("", "created"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("groupLevel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "groupLevel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("id");
		elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("roleLevel");
		elemField.setXmlName(new javax.xml.namespace.QName("", "roleLevel"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(java.lang.String mechType,
			java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(java.lang.String mechType,
			java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

}
