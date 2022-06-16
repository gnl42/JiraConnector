/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;

import me.glindholm.connector.eclipse.internal.core.CoreMessages;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraAuthenticationException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraCaptchaRequiredException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import me.glindholm.jira.rest.client.api.RestClientException;
import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;

/**
 * @author Brock Janiczak
 */
public class JiraCorePlugin extends Plugin {

    public static final String ID_PLUGIN = "me.glindholm.connector.eclipse.internal.jira.core"; //$NON-NLS-1$

    private static JiraCorePlugin plugin;

    private static JiraClientManager clientManager;

    public final static String CONNECTOR_KIND = "jira"; //$NON-NLS-1$

    public final static String LABEL = NLS.bind(Messages.JiraCorePlugin_JIRA_description, "5.0"); //$NON-NLS-1$

    private static boolean initialized;

    /**
     * The constructor.
     */
    public JiraCorePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        File serverCache = getStateLocation().append("serverCache").toFile(); //$NON-NLS-1$
        initialize(serverCache);
    }

    public static void initialize(File serverCacheDirectory) {
        if (initialized) {
            throw new IllegalStateException("Already initialized"); //$NON-NLS-1$
        }
        initialized = true;

        clientManager = new JiraClientManager(serverCacheDirectory);
        clientManager.start();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);

        if (clientManager != null) {
            clientManager.stop();
        }
        plugin = null;
        clientManager = null;
    }

    /**
     * Returns the shared instance.
     */
    public static JiraCorePlugin getDefault() {
        return plugin;
    }

    public static JiraClientManager getClientManager() {
        if (!initialized) {
            throw new IllegalStateException("Not yet initialized"); //$NON-NLS-1$
        }
        return clientManager;
    }

    public static IStatus toStatus(TaskRepository repository, Throwable e) {
        String url = repository.getRepositoryUrl();
        if (e instanceof JiraCaptchaRequiredException) {
            return new RepositoryStatus(repository.getRepositoryUrl(), IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY_LOGIN,
                    CoreMessages.Captcha_authentication_required);
        } else if (e instanceof JiraAuthenticationException) {
            return RepositoryStatus.createLoginError(url, ID_PLUGIN);
        } else if (e instanceof JiraServiceUnavailableException) {
            return new RepositoryStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_IO, e.getMessage(), e);
        } else if (e instanceof JiraRemoteMessageException) {
            return RepositoryStatus.createHtmlStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY, e.getMessage(),
                    ((JiraRemoteMessageException) e).getHtmlMessage());
        } else if (e instanceof JiraException) {
            return new RepositoryStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY, rootCause(e), e);
        } else if (e instanceof InvalidJiraQueryException) {
            return new RepositoryStatus(url, IStatus.ERROR, ID_PLUGIN, RepositoryStatus.ERROR_REPOSITORY, NLS.bind(CoreMessages.Invalid_query, e.getMessage()),
                    e);
        } else {
            return RepositoryStatus.createInternalError(ID_PLUGIN, "Unexpected error", e); //$NON-NLS-1$
        }
    }

    private static String rootCause(final Throwable t) {
        if (t.getCause() == null) {
            final List<String> errorMsg = new ArrayList<>();
            if (t instanceof RestClientException) {
                RestClientException restException = (RestClientException) t;
                for (ErrorCollection errors : restException.getErrorCollections()) {
                    if (errors.getErrorMessages() != null) {
                        for (String msg : errors.getErrorMessages()) {
                            errorMsg.add(errors.getStatus() + ": " + msg);
                        }
                    } else {
                        for (Entry<String, String> msgs : errors.getErrors().entrySet()) {
                            errorMsg.add(errors.getStatus() + ": " + msgs.getValue());
                        }
                    }
                }
            } else {
                return t.getMessage();
            }
            return String.join("\n", errorMsg);
        }
        return rootCause(t.getCause());
    }

}
