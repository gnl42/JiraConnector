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

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 * @author Steffen Pingel
 */
public class JiraUiPlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "com.atlassian.connector.eclipse.jira.ui"; //$NON-NLS-1$

	public static final String PRODUCT_NAME = "Atlassian JIRA Connector"; //$NON-NLS-1$

	private static JiraUiPlugin instance;

	public static JiraUiPlugin getDefault() {
		return instance;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.jira", path); //$NON-NLS-1$
	}

	public JiraUiPlugin() {
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put("icons/obj16/comment.gif", getImageDescriptor("icons/obj16/comment.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		reg.put("icons/obj16/jira.png", getImageDescriptor("icons/obj16/jira.png")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		JiraClientFactory.getDefault().setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory(), false);
		TasksUi.getRepositoryManager().addListener(JiraClientFactory.getDefault());

//		final IPartListener listener = new IPartListener() {
//
//			public void partOpened(IWorkbenchPart part) {
//				System.out.println("dupa"); //$NON-NLS-1$
//			}
//
//			public void partDeactivated(IWorkbenchPart part) {
//				// ignore
//
//			}
//
//			public void partClosed(IWorkbenchPart part) {
//				// ignore
//
//			}
//
//			public void partBroughtToTop(IWorkbenchPart part) {
//				// ignore
//
//			}
//
//			public void partActivated(IWorkbenchPart part) {
//				// ignore
//
//			}
//		};
//		getWorkbench().getActiveWorkbenchWindow().addPageListener(new IPageListener() {
//
//			public void pageOpened(IWorkbenchPage page) {
//				page.addPartListener(listener);
//			}
//
//			public void pageClosed(IWorkbenchPage page) {
//				page.removePartListener(listener);
//			}
//
//			public void pageActivated(IWorkbenchPage page) {
//				// ignore
//
//			}
//		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		TasksUi.getRepositoryManager().removeListener(JiraClientFactory.getDefault());
		JiraClientFactory.getDefault().logOutFromAll();
		instance = null;
		super.stop(context);
	}

}
