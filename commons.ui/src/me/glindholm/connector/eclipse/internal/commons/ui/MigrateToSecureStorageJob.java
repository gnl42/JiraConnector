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

package me.glindholm.connector.eclipse.internal.commons.ui;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.progress.UIJob;

public class MigrateToSecureStorageJob extends UIJob {

    public static class MutexRule implements ISchedulingRule {
        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }
    }

    private final String kind;

    private static final MutexRule mutex = new MutexRule();

    public MigrateToSecureStorageJob(String kind) {
        super("Migrating passwords to secure storage");
        this.kind = kind;

        setRule(mutex);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        Set<TaskRepository> repos = TasksUiPlugin.getRepositoryManager().getRepositories(kind);
        if (repos != null) {
            for (TaskRepository repo : repos) {
                migrateToSecureStorage(repo);
            }
        }
        return Status.OK_STATUS;
    }

    public static boolean migrateToSecureStorage(TaskRepository repository) {
        if (!"local".equals(repository.getUrl())) { //$NON-NLS-1$
            AuthenticationCredentials creds = repository.getCredentials(AuthenticationType.REPOSITORY), httpCreds = repository.getCredentials(AuthenticationType.HTTP), proxyCreds = repository.getCredentials(AuthenticationType.PROXY);
            boolean savePassword = repository.getSavePassword(AuthenticationType.REPOSITORY), httpSavePassword = repository.getSavePassword(AuthenticationType.HTTP), proxySavePassword = repository.getSavePassword(AuthenticationType.PROXY);

            if (creds != null) {
                repository.setCredentials(AuthenticationType.REPOSITORY, creds, savePassword);
            }

            if (httpCreds != null) {
                repository.setCredentials(AuthenticationType.HTTP, httpCreds, httpSavePassword);
            }

            if (proxyCreds != null) {
                repository.setCredentials(AuthenticationType.PROXY, proxyCreds, proxySavePassword);
            }

            return true;
        }
        return false;
    }

}
