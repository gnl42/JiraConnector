/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Tasktop Technologies - initial API and implementation
 *******************************************************************************/
/**
 * JiraSoapService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.wsdl.soap;

@SuppressWarnings("all")
public interface JiraSoapService extends java.rmi.Remote {
	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment getComment(java.lang.String in0, long in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup createGroup(java.lang.String in0,
			java.lang.String in1, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteSecurityLevel getSecurityLevel(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteServerInfo getServerInfo(java.lang.String in0)
			throws java.rmi.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup getGroup(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser createUser(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser getUser(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public java.lang.String login(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue getIssue(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue createIssue(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteNamedObject[] getAvailableActions(
			java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue updateIssue(java.lang.String in0,
			java.lang.String in1, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteFieldValue[] in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteConfiguration getConfiguration(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComponent[] getComponents(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject updateProject(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject getProjectByKey(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePriority[] getPriorities(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteResolution[] getResolutions(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssueType[] getIssueTypes(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteStatus[] getStatuses(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssueType[] getSubTaskIssueTypes(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole[] getProjectRoles(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole getProjectRole(java.lang.String in0,
			long in1) throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRoleActors getProjectRoleActors(
			java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteRoleActors getDefaultRoleActors(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void removeAllRoleActorsByNameAndType(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void removeAllRoleActorsByProject(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void deleteProjectRole(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1, boolean in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void updateProjectRole(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole createProjectRole(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public boolean isProjectRoleNameUnique(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void addActorsToProjectRole(java.lang.String in0, java.lang.String[] in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in2,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in3, java.lang.String in4)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void removeActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in2,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in3, java.lang.String in4)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void addDefaultActorsToProjectRole(java.lang.String in0, java.lang.String[] in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in2, java.lang.String in3)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void removeDefaultActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in2, java.lang.String in3)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme[] getAssociatedNotificationSchemes(
			java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme[] getAssociatedPermissionSchemes(
			java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProjectRole in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteField[] getCustomFields(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment[] getComments(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteFilter[] getFavouriteFilters(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void archiveVersion(java.lang.String in0, java.lang.String in1, java.lang.String in2, boolean in3)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteVersion[] getVersions(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject createProject(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4,
			java.lang.String in5, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme in6,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme in7,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme in8) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void addComment(java.lang.String in0, java.lang.String in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteField[] getFieldsForEdit(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssueType[] getIssueTypesForProject(
			java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssueType[] getSubTaskIssueTypesForProject(
			java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void addUserToGroup(java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void removeUserFromGroup(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteUser in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public boolean logout(java.lang.String in0) throws java.rmi.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject getProjectById(java.lang.String in0, long in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject getProjectWithSchemesById(
			java.lang.String in0, long in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void deleteProject(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void releaseVersion(java.lang.String in0, java.lang.String in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteVersion in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteSecurityLevel[] getSecurityLevels(
			java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException;

	public void deleteIssue(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue createIssueWithSecurityLevel(
			java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue in1, long in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public boolean addAttachmentsToIssue(java.lang.String in0, java.lang.String in1, java.lang.String[] in2,
			byte[][] in3) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteAttachment[] getAttachmentsFromIssue(
			java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public boolean hasPermissionToEditComment(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment editComment(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteComment in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteField[] getFieldsForAction(java.lang.String in0,
			java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue progressWorkflowAction(java.lang.String in0,
			java.lang.String in1, java.lang.String in2,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteFieldValue[] in3) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue getIssueById(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog addWorklogWithNewRemainingEstimate(
			java.lang.String in0, java.lang.String in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in2, java.lang.String in3)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(
			java.lang.String in0, java.lang.String in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog addWorklogAndRetainRemainingEstimate(
			java.lang.String in0, java.lang.String in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void deleteWorklogWithNewRemainingEstimate(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void deleteWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void deleteWorklogAndRetainRemainingEstimate(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void updateWorklogWithNewRemainingEstimate(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in1, java.lang.String in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void updateWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public void updateWorklogAndRetainRemainingEstimate(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog[] getWorklogs(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public boolean hasPermissionToCreateWorklog(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public boolean hasPermissionToDeleteWorklog(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public boolean hasPermissionToUpdateWorklog(java.lang.String in0, java.lang.String in1)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme[] getNotificationSchemes(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme[] getPermissionSchemes(
			java.lang.String in0) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme createPermissionScheme(
			java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void deletePermissionScheme(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme addPermissionTo(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermission in2,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteEntity in3) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme deletePermissionFrom(
			java.lang.String in0, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermissionScheme in1,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermission in2,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteEntity in3) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemotePermission[] getAllPermissions(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public long getIssueCountForFilter(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue[] getIssuesFromTextSearch(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue[] getIssuesFromTextSearchWithProject(
			java.lang.String in0, java.lang.String[] in1, java.lang.String in2, int in3)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public void deleteUser(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup updateGroup(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteGroup in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void deleteGroup(java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public void refreshCustomFields(java.lang.String in0) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject[] getProjectsNoSchemes(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteVersion addVersion(java.lang.String in0,
			java.lang.String in1, org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteVersion in2)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteFilter[] getSavedFilters(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public boolean addBase64EncodedAttachmentsToIssue(java.lang.String in0, java.lang.String in1,
			java.lang.String[] in2, java.lang.String[] in3) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject createProjectFromObject(java.lang.String in0,
			org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteProject in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteValidationException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteScheme[] getSecuritySchemes(java.lang.String in0)
			throws java.rmi.RemoteException, org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemotePermissionException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteAuthenticationException;

	public org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteIssue[] getIssuesFromFilter(java.lang.String in0,
			java.lang.String in1) throws java.rmi.RemoteException,
			org.eclipse.mylyn.internal.jira.core.wsdl.soap.RemoteException;
}
