/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * RemoteConfiguration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.wsdl.beans;

@SuppressWarnings( { "unchecked", "serial" })
public class RemoteConfiguration implements java.io.Serializable {
	private boolean allowAttachments;

	private boolean allowIssueLinking;

	private boolean allowSubTasks;

	private boolean allowTimeTracking;

	private boolean allowUnassignedIssues;

	private boolean allowVoting;

	private boolean allowWatching;

	public RemoteConfiguration() {
	}

	public RemoteConfiguration(boolean allowAttachments, boolean allowIssueLinking, boolean allowSubTasks,
			boolean allowTimeTracking, boolean allowUnassignedIssues, boolean allowVoting, boolean allowWatching) {
		this.allowAttachments = allowAttachments;
		this.allowIssueLinking = allowIssueLinking;
		this.allowSubTasks = allowSubTasks;
		this.allowTimeTracking = allowTimeTracking;
		this.allowUnassignedIssues = allowUnassignedIssues;
		this.allowVoting = allowVoting;
		this.allowWatching = allowWatching;
	}

	/**
	 * Gets the allowAttachments value for this RemoteConfiguration.
	 * 
	 * @return allowAttachments
	 */
	public boolean isAllowAttachments() {
		return allowAttachments;
	}

	/**
	 * Sets the allowAttachments value for this RemoteConfiguration.
	 * 
	 * @param allowAttachments
	 */
	public void setAllowAttachments(boolean allowAttachments) {
		this.allowAttachments = allowAttachments;
	}

	/**
	 * Gets the allowIssueLinking value for this RemoteConfiguration.
	 * 
	 * @return allowIssueLinking
	 */
	public boolean isAllowIssueLinking() {
		return allowIssueLinking;
	}

	/**
	 * Sets the allowIssueLinking value for this RemoteConfiguration.
	 * 
	 * @param allowIssueLinking
	 */
	public void setAllowIssueLinking(boolean allowIssueLinking) {
		this.allowIssueLinking = allowIssueLinking;
	}

	/**
	 * Gets the allowSubTasks value for this RemoteConfiguration.
	 * 
	 * @return allowSubTasks
	 */
	public boolean isAllowSubTasks() {
		return allowSubTasks;
	}

	/**
	 * Sets the allowSubTasks value for this RemoteConfiguration.
	 * 
	 * @param allowSubTasks
	 */
	public void setAllowSubTasks(boolean allowSubTasks) {
		this.allowSubTasks = allowSubTasks;
	}

	/**
	 * Gets the allowTimeTracking value for this RemoteConfiguration.
	 * 
	 * @return allowTimeTracking
	 */
	public boolean isAllowTimeTracking() {
		return allowTimeTracking;
	}

	/**
	 * Sets the allowTimeTracking value for this RemoteConfiguration.
	 * 
	 * @param allowTimeTracking
	 */
	public void setAllowTimeTracking(boolean allowTimeTracking) {
		this.allowTimeTracking = allowTimeTracking;
	}

	/**
	 * Gets the allowUnassignedIssues value for this RemoteConfiguration.
	 * 
	 * @return allowUnassignedIssues
	 */
	public boolean isAllowUnassignedIssues() {
		return allowUnassignedIssues;
	}

	/**
	 * Sets the allowUnassignedIssues value for this RemoteConfiguration.
	 * 
	 * @param allowUnassignedIssues
	 */
	public void setAllowUnassignedIssues(boolean allowUnassignedIssues) {
		this.allowUnassignedIssues = allowUnassignedIssues;
	}

	/**
	 * Gets the allowVoting value for this RemoteConfiguration.
	 * 
	 * @return allowVoting
	 */
	public boolean isAllowVoting() {
		return allowVoting;
	}

	/**
	 * Sets the allowVoting value for this RemoteConfiguration.
	 * 
	 * @param allowVoting
	 */
	public void setAllowVoting(boolean allowVoting) {
		this.allowVoting = allowVoting;
	}

	/**
	 * Gets the allowWatching value for this RemoteConfiguration.
	 * 
	 * @return allowWatching
	 */
	public boolean isAllowWatching() {
		return allowWatching;
	}

	/**
	 * Sets the allowWatching value for this RemoteConfiguration.
	 * 
	 * @param allowWatching
	 */
	public void setAllowWatching(boolean allowWatching) {
		this.allowWatching = allowWatching;
	}

	private java.lang.Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof RemoteConfiguration))
			return false;
		RemoteConfiguration other = (RemoteConfiguration) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && this.allowAttachments == other.isAllowAttachments()
				&& this.allowIssueLinking == other.isAllowIssueLinking()
				&& this.allowSubTasks == other.isAllowSubTasks()
				&& this.allowTimeTracking == other.isAllowTimeTracking()
				&& this.allowUnassignedIssues == other.isAllowUnassignedIssues()
				&& this.allowVoting == other.isAllowVoting() && this.allowWatching == other.isAllowWatching();
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
		_hashCode += (isAllowAttachments() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowIssueLinking() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowSubTasks() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowTimeTracking() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowUnassignedIssues() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowVoting() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		_hashCode += (isAllowWatching() ? Boolean.TRUE : Boolean.FALSE).hashCode();
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			RemoteConfiguration.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com",
				"RemoteConfiguration"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowAttachments");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowAttachments"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowIssueLinking");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowIssueLinking"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowSubTasks");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowSubTasks"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowTimeTracking");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowTimeTracking"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowUnassignedIssues");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowUnassignedIssues"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowVoting");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowVoting"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("allowWatching");
		elemField.setXmlName(new javax.xml.namespace.QName("", "allowWatching"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
		elemField.setNillable(false);
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
