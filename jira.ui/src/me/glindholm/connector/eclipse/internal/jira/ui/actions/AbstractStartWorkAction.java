/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.actions;

import java.util.List;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import me.glindholm.connector.eclipse.internal.jira.core.JiraAttribute;
import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import me.glindholm.connector.eclipse.internal.jira.ui.IJiraTask;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraImages;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractStartWorkAction extends AbstractJiraAction {

    public AbstractStartWorkAction() {
        super("Start/Stop Work Action"); //$NON-NLS-1$

        setImageDescriptor(JiraImages.START_PROGRESS);
    }

    @Override
    protected void doAction(final List<IJiraTask> tasks) {
    }

    protected static boolean isAssignedToMe(final TaskData taskData, final ITask task) {

        if (taskData == null || task == null) {
            return false;
        }

        final TaskRepository repository = TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());

        if (repository == null) {
            return false;
        }

        final TaskAttribute rootAttribute = taskData.getRoot();

        if (rootAttribute == null) {
            return false;
        }

        // TaskAttribute assigneeAttribute = rootAttribute.getAttribute(JiraAttribute.USER_ASSIGNED.id());

        return repository.getUserName() != null && repository.getUserName().equals(task.getOwner());
    }

    protected static boolean isTaskInProgress(final TaskData taskData, final ITask task) {
        return isAssignedToMe(taskData, task) && isInProgressState(taskData);// && haveStopProgressOperation(taskData);
    }

    protected static boolean isTaskInStop(final TaskData taskData, final ITask task) {
        // if (isAssignedToMe(taskData, task) && haveStartProgressOperation(taskData)) {
        // return true;
        // } else if (!isAssignedToMe(taskData, task) && isInOpenState(taskData)) {
        // return true;
        // }

        if ((isAssignedToMe(taskData, task) && haveStartProgressOperation(taskData)) || isInOpenState(taskData)) {
            return true;

        }

        return false;
    }

    protected static boolean haveStopProgressOperation(final TaskData taskData) {
        return haveOperation(taskData, JiraTaskDataHandler.STOP_PROGRESS_OPERATION);
    }

    protected static boolean haveStartProgressOperation(final TaskData taskData) {
        return haveOperation(taskData, JiraTaskDataHandler.START_PROGRESS_OPERATION);
    }

    private static boolean haveOperation(final TaskData taskData, final String operationId) {

        if (taskData == null) {
            return false;
        }

        final TaskAttribute selectedOperationAttribute = taskData.getRoot().getMappedAttribute(TaskAttribute.OPERATION);

        final List<TaskOperation> operations = taskData.getAttributeMapper().getTaskOperations(selectedOperationAttribute);

        for (final TaskOperation operation : operations) {
            if (operationId.equals(operation.getOperationId())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInOpenState(final TaskData taskData) {

        if (taskData == null) {
            return false;
        }

        final String statusId = taskData.getRoot().getAttribute(JiraAttribute.STATUS.id()).getValue();

        return statusId != null && (statusId.equals(JiraTaskDataHandler.OPEN_STATUS) || statusId.equals(JiraTaskDataHandler.REOPEN_STATUS));
    }

    private static boolean isInProgressState(final TaskData taskData) {

        if (taskData == null) {
            return false;
        }

        final String statusId = taskData.getRoot().getAttribute(JiraAttribute.STATUS.id()).getValue();

        return statusId != null && statusId.equals(JiraTaskDataHandler.IN_PROGRESS_STATUS);
    }

}
