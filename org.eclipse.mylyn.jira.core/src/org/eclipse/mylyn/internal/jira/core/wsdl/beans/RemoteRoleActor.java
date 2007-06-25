/**
 * RemoteRoleActor.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.wsdl.beans;

@SuppressWarnings({ "unchecked", "serial" })
public class RemoteRoleActor  implements java.io.Serializable {
    private java.lang.String descriptor;

    private java.lang.String parameter;

    private java.lang.String prettyName;

    private org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole;

    private java.lang.String type;

    private org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users;

    public RemoteRoleActor() {
    }

    public RemoteRoleActor(
           java.lang.String descriptor,
           java.lang.String parameter,
           java.lang.String prettyName,
           org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole,
           java.lang.String type,
           org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users) {
           this.descriptor = descriptor;
           this.parameter = parameter;
           this.prettyName = prettyName;
           this.projectRole = projectRole;
           this.type = type;
           this.users = users;
    }


    /**
     * Gets the descriptor value for this RemoteRoleActor.
     * 
     * @return descriptor
     */
    public java.lang.String getDescriptor() {
        return descriptor;
    }


    /**
     * Sets the descriptor value for this RemoteRoleActor.
     * 
     * @param descriptor
     */
    public void setDescriptor(java.lang.String descriptor) {
        this.descriptor = descriptor;
    }


    /**
     * Gets the parameter value for this RemoteRoleActor.
     * 
     * @return parameter
     */
    public java.lang.String getParameter() {
        return parameter;
    }


    /**
     * Sets the parameter value for this RemoteRoleActor.
     * 
     * @param parameter
     */
    public void setParameter(java.lang.String parameter) {
        this.parameter = parameter;
    }


    /**
     * Gets the prettyName value for this RemoteRoleActor.
     * 
     * @return prettyName
     */
    public java.lang.String getPrettyName() {
        return prettyName;
    }


    /**
     * Sets the prettyName value for this RemoteRoleActor.
     * 
     * @param prettyName
     */
    public void setPrettyName(java.lang.String prettyName) {
        this.prettyName = prettyName;
    }


    /**
     * Gets the projectRole value for this RemoteRoleActor.
     * 
     * @return projectRole
     */
    public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole getProjectRole() {
        return projectRole;
    }


    /**
     * Sets the projectRole value for this RemoteRoleActor.
     * 
     * @param projectRole
     */
    public void setProjectRole(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole projectRole) {
        this.projectRole = projectRole;
    }


    /**
     * Gets the type value for this RemoteRoleActor.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this RemoteRoleActor.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the users value for this RemoteRoleActor.
     * 
     * @return users
     */
    public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] getUsers() {
        return users;
    }


    /**
     * Sets the users value for this RemoteRoleActor.
     * 
     * @param users
     */
    public void setUsers(org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser[] users) {
        this.users = users;
    }

    private java.lang.Object __equalsCalc = null;
    @Override
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RemoteRoleActor)) return false;
        RemoteRoleActor other = (RemoteRoleActor) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.descriptor==null && other.getDescriptor()==null) || 
             (this.descriptor!=null &&
              this.descriptor.equals(other.getDescriptor()))) &&
            ((this.parameter==null && other.getParameter()==null) || 
             (this.parameter!=null &&
              this.parameter.equals(other.getParameter()))) &&
            ((this.prettyName==null && other.getPrettyName()==null) || 
             (this.prettyName!=null &&
              this.prettyName.equals(other.getPrettyName()))) &&
            ((this.projectRole==null && other.getProjectRole()==null) || 
             (this.projectRole!=null &&
              this.projectRole.equals(other.getProjectRole()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.users==null && other.getUsers()==null) || 
             (this.users!=null &&
              java.util.Arrays.equals(this.users, other.getUsers())));
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
        if (getDescriptor() != null) {
            _hashCode += getDescriptor().hashCode();
        }
        if (getParameter() != null) {
            _hashCode += getParameter().hashCode();
        }
        if (getPrettyName() != null) {
            _hashCode += getPrettyName().hashCode();
        }
        if (getProjectRole() != null) {
            _hashCode += getProjectRole().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getUsers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUsers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUsers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RemoteRoleActor.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteRoleActor"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descriptor");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descriptor"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameter");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prettyName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "prettyName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("projectRole");
        elemField.setXmlName(new javax.xml.namespace.QName("", "projectRole"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
