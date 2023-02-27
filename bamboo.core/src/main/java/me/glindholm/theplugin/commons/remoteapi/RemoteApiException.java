/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.remoteapi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.connector.eclipse.internal.bamboo.core.BambooCorePlugin;

/**
 * Generic exception related to a remote session.
 */
public class RemoteApiException extends CoreException {

    /**
     *
     */
    private static final long serialVersionUID = -8518437777396192588L;

    @Nullable
    public String getServerStackTrace() {
        return serverStackTrace;
    }

    private final String serverStackTrace;

    public RemoteApiException(final String message) {
        super(new Status(IStatus.ERROR, BambooCorePlugin.ID_PLUGIN, message));
        serverStackTrace = null;
    }

    public RemoteApiException(final String message, @Nullable final String serverStackTrace) {
        super(new Status(IStatus.ERROR, BambooCorePlugin.ID_PLUGIN, message));
        this.serverStackTrace = serverStackTrace;
    }

    public RemoteApiException(final Throwable throwable) {
        super(new Status(IStatus.ERROR, BambooCorePlugin.ID_PLUGIN, "", throwable));
        serverStackTrace = null;
    }

    public RemoteApiException(final String message, final Throwable throwable) {
        super(new Status(IStatus.ERROR, BambooCorePlugin.ID_PLUGIN, message, throwable));
        serverStackTrace = null;
    }
}