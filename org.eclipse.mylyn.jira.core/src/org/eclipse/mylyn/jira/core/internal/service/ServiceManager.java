/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.service;

import org.eclipse.mylar.jira.core.internal.service.soap.SoapJiraServiceFactory;

/**
 * TODO This is eclispe specific at the moment. Need to find a way to load a
 * JiraService instance based off some use preference. 
 * 
 * TODO mention that this
 * should really only be used by internal classes and tests
 */
public class ServiceManager {

	private static JiraServiceFactory factory;

	public static synchronized JiraService getJiraService(JiraServer server) {
		if (factory == null) {
			factory = loadServiceProviderFactories();
		}
		return factory.createService(server);
	}

	public static JiraServiceFactory loadServiceProviderFactories() {
//		JiraCorePlugin plugin = JiraCorePlugin.getDefault();
//		if (plugin != null) {
//			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(JiraCorePlugin.ID,
//					IJiraCoreExtensionConstants.SERVICE_PROVIDER_FACTORY);
//			if (extension != null) {
//				IExtension[] extensions = extension.getExtensions();
//				for (int i = 0; i < extensions.length; i++) {
//					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
//					for (int j = 0; j < configElements.length; j++) {
//						try {
//							return (JiraServiceFactory) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
//						} catch (CoreException e) {
//							plugin.getLog().log(e.getStatus());
//						} catch (ClassCastException e) {
//							String className = configElements[j].getAttribute("class"); //$NON-NLS-1$
//							JiraCorePlugin.log(IStatus.ERROR, "Must implement the correct class", e);
//						}
//						return null;
//					}
//				}
//			}
//		}
//		return null;
		
		return new SoapJiraServiceFactory();
	}
}
