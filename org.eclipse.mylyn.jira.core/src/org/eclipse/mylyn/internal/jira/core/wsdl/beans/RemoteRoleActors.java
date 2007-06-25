/**
 * RemoteRoleActors.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.wsdl.beans;

@SuppressWarnings( { "unchecked", "serial" })
public class RemoteRoleActors implements java.io.Serializable {
	private org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole;

	private org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteRoleActor[] roleActors;

	private org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users;

	public RemoteRoleActors() {
	}

	public RemoteRoleActors(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteRoleActor[] roleActors,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users) {
		this.projectRole = projectRole;
		this.roleActors = roleActors;
		this.users = users;
	}

	/**
	 * Gets the projectRole value for this RemoteRoleActors.
	 * 
	 * @return projectRole
	 */
	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole getProjectRole() {
		return projectRole;
	}

	/**
	 * Sets the projectRole value for this RemoteRoleActors.
	 * 
	 * @param projectRole
	 */
	public void setProjectRole(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole) {
		this.projectRole = projectRole;
	}

	/**
	 * Gets the roleActors value for this RemoteRoleActors.
	 * 
	 * @return roleActors
	 */
	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteRoleActor[] getRoleActors() {
		return roleActors;
	}

	/**
	 * Sets the roleActors value for this RemoteRoleActors.
	 * 
	 * @param roleActors
	 */
	public void setRoleActors(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteRoleActor[] roleActors) {
		this.roleActors = roleActors;
	}

	/**
	 * Gets the users value for this RemoteRoleActors.
	 * 
	 * @return users
	 */
	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] getUsers() {
		return users;
	}

	/**
	 * Sets the users value for this RemoteRoleActors.
	 * 
	 * @param users
	 */
	public void setUsers(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users) {
		this.users = users;
	}

	private java.lang.Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof RemoteRoleActors))
			return false;
		RemoteRoleActors other = (RemoteRoleActors) obj;
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
				&& ((this.projectRole == null && other.getProjectRole() == null) || (this.projectRole != null && this.projectRole.equals(other.getProjectRole())))
				&& ((this.roleActors == null && other.getRoleActors() == null) || (this.roleActors != null && java.util.Arrays.equals(
						this.roleActors, other.getRoleActors())))
				&& ((this.users == null && other.getUsers() == null) || (this.users != null && java.util.Arrays.equals(
						this.users, other.getUsers())));
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
		if (getProjectRole() != null) {
			_hashCode += getProjectRole().hashCode();
		}
		if (getRoleActors() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getRoleActors()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getRoleActors(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getUsers() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getUsers()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(getUsers(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			RemoteRoleActors.class, true);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com",
				"RemoteRoleActors"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("projectRole");
		elemField.setXmlName(new javax.xml.namespace.QName("", "projectRole"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com",
				"RemoteProjectRole"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("roleActors");
		elemField.setXmlName(new javax.xml.namespace.QName("", "roleActors"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com",
				"RemoteRoleActor"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("users");
		elemField.setXmlName(new javax.xml.namespace.QName("", "users"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"));
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
