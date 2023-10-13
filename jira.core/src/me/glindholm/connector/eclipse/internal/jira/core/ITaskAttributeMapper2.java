/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.util.Map;

import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

public interface ITaskAttributeMapper2 {

    Map<String, String> getRepositoryOptions(TaskAttribute attribute);

    BasicUser getRepositoryUser(TaskAttribute attribute);

    void setRepositoryUser(TaskAttribute attribute, BasicUser user);

    JiraClient getClient();

    IRepositoryPerson createPerson(BasicUser user);

    BasicUser lookupExternalId(TaskAttribute attribute, String externalId);

}
