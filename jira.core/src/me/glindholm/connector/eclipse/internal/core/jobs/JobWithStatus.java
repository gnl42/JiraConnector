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

package me.glindholm.connector.eclipse.internal.core.jobs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;

public abstract class JobWithStatus extends Job {

    private IStatus status = Status.OK_STATUS;

    public JobWithStatus(final String name) {
        super(name);
    }

    protected void setStatus(@NonNull final IStatus status) {
        this.status = status;
    }

    /**
     * @return if run did not set status it will return {@link Status#OK_STATUS} just to make using this
     *         method easier
     */
    @NonNull
    public IStatus getStatus() {
        return status;
    }

    @Override
    @NonNull
    public IStatus run(final IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            setStatus(Status.CANCEL_STATUS);
            return Status.CANCEL_STATUS;
        }

        try {
            runImpl(monitor);
            return Status.OK_STATUS;
        } catch (final CoreException e) {
            setStatus(e.getStatus());
            return Status.OK_STATUS;
        }
    }

    protected abstract void runImpl(IProgressMonitor monitor) throws CoreException;
}
