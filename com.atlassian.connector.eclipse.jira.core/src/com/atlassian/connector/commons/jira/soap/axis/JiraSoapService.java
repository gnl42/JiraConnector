/**
 * JiraSoapService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.connector.commons.jira.soap.axis;

public interface JiraSoapService extends java.rmi.Remote {
	public com.atlassian.connector.commons.jira.soap.axis.RemoteComment getComment(java.lang.String in0, long in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteGroup createGroup(java.lang.String in0, java.lang.String in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteUser in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteSecurityLevel getSecurityLevel(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteServerInfo getServerInfo(java.lang.String in0)
			throws java.rmi.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteGroup getGroup(java.lang.String in0, java.lang.String in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteUser createUser(java.lang.String in0, java.lang.String in1,
			java.lang.String in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteUser getUser(java.lang.String in0, java.lang.String in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public java.lang.String login(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue getIssue(java.lang.String in0, java.lang.String in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue createIssue(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteIssue in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteNamedObject[] getAvailableActions(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue updateIssue(java.lang.String in0, java.lang.String in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteFieldValue[] in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteConfiguration getConfiguration(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteComponent[] getComponents(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject updateProject(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProject in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject getProjectByKey(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePriority[] getPriorities(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteResolution[] getResolutions(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssueType[] getIssueTypes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteStatus[] getStatuses(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssueType[] getSubTaskIssueTypes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole[] getProjectRoles(java.lang.String in0)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole getProjectRole(java.lang.String in0, long in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRoleActors getProjectRoleActors(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProject in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteRoleActors getDefaultRoleActors(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void removeAllRoleActorsByNameAndType(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void removeAllRoleActorsByProject(java.lang.String in0, com.atlassian.connector.commons.jira.soap.axis.RemoteProject in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteProjectRole(java.lang.String in0, com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1,
			boolean in2) throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void updateProjectRole(java.lang.String in0, com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole createProjectRole(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean isProjectRoleNameUnique(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void addActorsToProjectRole(java.lang.String in0, java.lang.String[] in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in2,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProject in3, java.lang.String in4)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void removeActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in2,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProject in3, java.lang.String in4)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void addDefaultActorsToProjectRole(java.lang.String in0, java.lang.String[] in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in2, java.lang.String in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void removeDefaultActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in2, java.lang.String in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteScheme[] getAssociatedNotificationSchemes(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteScheme[] getAssociatedPermissionSchemes(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProjectRole in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteField[] getCustomFields(java.lang.String in0)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteComment[] getComments(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteFilter[] getFavouriteFilters(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void archiveVersion(java.lang.String in0, java.lang.String in1, java.lang.String in2, boolean in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteVersion[] getVersions(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject createProject(java.lang.String in0, java.lang.String in1,
			java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme in6,
			com.atlassian.connector.commons.jira.soap.axis.RemoteScheme in7,
			com.atlassian.connector.commons.jira.soap.axis.RemoteScheme in8) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void addComment(java.lang.String in0, java.lang.String in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteComment in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteField[] getFieldsForEdit(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssueType[] getIssueTypesForProject(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssueType[] getSubTaskIssueTypesForProject(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException;

	public void addUserToGroup(java.lang.String in0, com.atlassian.connector.commons.jira.soap.axis.RemoteGroup in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteUser in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void removeUserFromGroup(java.lang.String in0, com.atlassian.connector.commons.jira.soap.axis.RemoteGroup in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteUser in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean logout(java.lang.String in0) throws java.rmi.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject getProjectById(java.lang.String in0, long in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject getProjectWithSchemesById(java.lang.String in0, long in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteProject(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void releaseVersion(java.lang.String in0, java.lang.String in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteVersion in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteSecurityLevel[] getSecurityLevels(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteIssue(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue createIssueWithSecurityLevel(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteIssue in1, long in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean addAttachmentsToIssue(java.lang.String in0, java.lang.String in1, java.lang.String[] in2, byte[][] in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteAttachment[] getAttachmentsFromIssue(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean hasPermissionToEditComment(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteComment in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteComment editComment(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteComment in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteField[] getFieldsForAction(java.lang.String in0,
			java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue progressWorkflowAction(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, com.atlassian.connector.commons.jira.soap.axis.RemoteFieldValue[] in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue getIssueById(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog addWorklogWithNewRemainingEstimate(java.lang.String in0,
			java.lang.String in1, com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in2, java.lang.String in3) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(
			java.lang.String in0, java.lang.String in1, com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in2) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog addWorklogAndRetainRemainingEstimate(java.lang.String in0,
			java.lang.String in1, com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteWorklogWithNewRemainingEstimate(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0, java.lang.String in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteWorklogAndRetainRemainingEstimate(java.lang.String in0, java.lang.String in1) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void updateWorklogWithNewRemainingEstimate(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in1, java.lang.String in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void updateWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void updateWorklogAndRetainRemainingEstimate(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteWorklog[] getWorklogs(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean hasPermissionToCreateWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean hasPermissionToDeleteWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean hasPermissionToUpdateWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteScheme[] getNotificationSchemes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme[] getPermissionSchemes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme createPermissionScheme(java.lang.String in0,
			java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deletePermissionScheme(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme addPermissionTo(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme in1,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermission in2,
			com.atlassian.connector.commons.jira.soap.axis.RemoteEntity in3) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme deletePermissionFrom(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionScheme in1,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermission in2,
			com.atlassian.connector.commons.jira.soap.axis.RemoteEntity in3) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemotePermission[] getAllPermissions(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public long getIssueCountForFilter(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue[] getIssuesFromTextSearch(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue[] getIssuesFromTextSearchWithProject(java.lang.String in0,
			java.lang.String[] in1, java.lang.String in2, int in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteUser(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteGroup updateGroup(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteGroup in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void deleteGroup(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public void refreshCustomFields(java.lang.String in0)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject[] getProjectsNoSchemes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteVersion addVersion(java.lang.String in0, java.lang.String in1,
			com.atlassian.connector.commons.jira.soap.axis.RemoteVersion in2)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteFilter[] getSavedFilters(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public boolean addBase64EncodedAttachmentsToIssue(java.lang.String in0, java.lang.String in1, java.lang.String[] in2,
			java.lang.String[] in3) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteProject createProjectFromObject(java.lang.String in0,
			com.atlassian.connector.commons.jira.soap.axis.RemoteProject in1) throws java.rmi.RemoteException,
			com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteValidationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteScheme[] getSecuritySchemes(java.lang.String in0) throws
			java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemotePermissionException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteAuthenticationException,
			com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue[] getIssuesFromFilter(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue[] getIssuesFromFilterWithLimit(java.lang.String in0,
			java.lang.String in1, int in2, int in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;

	public com.atlassian.connector.commons.jira.soap.axis.RemoteIssue[] getIssuesFromTextSearchWithLimit(java.lang.String in0,
			java.lang.String in1, int in2, int in3)
			throws java.rmi.RemoteException, com.atlassian.connector.commons.jira.soap.axis.RemoteException;
}
