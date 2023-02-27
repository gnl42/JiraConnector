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

package me.glindholm.connector.eclipse.internal.jira.ui;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivityListener;
import org.eclipse.mylyn.tasks.core.TaskActivityAdapter;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import me.glindholm.connector.eclipse.internal.branding.ui.RuntimeUtil;
import me.glindholm.connector.eclipse.internal.commons.ui.MigrateToSecureStorageJob;
import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraConstants;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraUiPlugin extends AbstractUIPlugin {

    public static final String ID_PLUGIN = "me.glindholm.connector.eclipse.jira.ui"; //$NON-NLS-1$

    public static final String PRODUCT_NAME = "JiraConnector JIRA Connector"; //$NON-NLS-1$

    private static JiraUiPlugin instance;

    public static JiraUiPlugin getDefault() {
        return instance;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative
     * path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.jira", path); //$NON-NLS-1$
    }

    private final ITaskActivityListener activityTimeListener = new TaskActivityAdapter() {

        private boolean initialized = false;

        @Override
        public void elapsedTimeUpdated(final ITask task, final long newElapsedTime) {
            if (initialized && newElapsedTime == 0) {
                // reset logged time activity
                JiraUiUtil.clearLoggedActivityTime(task);
            }
        }

        @Override
        public void activityReset() {
            // hack to prevent reseting activity time when Mylyn is starting and reloading
            // activity
            initialized = true;
        }

    };

    public JiraUiPlugin() {
    }

    @Override
    protected void initializeImageRegistry(final ImageRegistry reg) {
        reg.put("icons/obj16/comment.gif", getImageDescriptor("icons/obj16/comment.gif")); //$NON-NLS-1$ //$NON-NLS-2$
        reg.put("icons/obj16/jira.png", getImageDescriptor("icons/obj16/jira.png")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        instance = this;
        JiraClientFactory.getDefault().setTaskRepositoryLocationFactory(new JiraTaskRepositoryLocationUiFactory(), false);
        TasksUi.getRepositoryManager().addListener(JiraClientFactory.getDefault());

        if (!getPreferenceStore().getBoolean(JiraConstants.PREFERENCE_SECURE_STORAGE_MIGRATED) && !RuntimeUtil.suppressConfigurationWizards()) {
            final Job migrateJob = new MigrateToSecureStorageJob(JiraCorePlugin.CONNECTOR_KIND);
            migrateJob.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(final IJobChangeEvent event) {
                    super.done(event);
                    getPreferenceStore().setValue(JiraConstants.PREFERENCE_SECURE_STORAGE_MIGRATED, true);
                }
            });
            migrateJob.schedule();
        }

        TasksUiPlugin.getTaskActivityManager().addActivityListener(activityTimeListener);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        TasksUiPlugin.getTaskActivityManager().removeActivityListener(activityTimeListener);
        TasksUi.getRepositoryManager().removeListener(JiraClientFactory.getDefault());
        instance = null;
        super.stop(context);
    }

}
