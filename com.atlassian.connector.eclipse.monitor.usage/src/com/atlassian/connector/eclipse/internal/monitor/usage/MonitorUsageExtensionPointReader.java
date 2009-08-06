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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

import com.atlassian.connector.eclipse.monitor.usage.IMonitorActivator;

class MonitorUsageExtensionPointReader {

	private static final long HOUR = 3600 * 1000;

	public static final String EXTENSION_ID_STUDY = "com.atlassian.connector.eclipse.monitor.usage.study"; //$NON-NLS-1$

	public static final String ELEMENT_COLLECTOR = "usageCollector"; //$NON-NLS-1$

	public static final String ELEMENT_COLLECTOR_UPLOAD_URL = "uploadUrl"; //$NON-NLS-1$

	public static final String ELEMENT_COLLECTOR_QUESTIONNAIRE = "questionnaire"; //$NON-NLS-1$

	public static final String ELEMENT_COLLECTOR_DETAILS_URL = "detailsUrl"; //$NON-NLS-1$

	public static final String ELEMENT_UI = "ui"; //$NON-NLS-1$

	public static final String ELEMENT_UI_TITLE = "title"; //$NON-NLS-1$

	public static final String ELEMENT_UI_DESCRIPTION = "description"; //$NON-NLS-1$

	public static final String ELEMENT_UI_UPLOAD_PROMPT = "daysBetweenUpload"; //$NON-NLS-1$

	public static final String ELEMENT_UI_QUESTIONNAIRE_PAGE = "questionnairePage"; //$NON-NLS-1$

	public static final String ELEMENT_UI_BACKGROUND_PAGE = "backgroundPage"; //$NON-NLS-1$

	public static final String ELEMENT_UI_CONSENT_FORM = "consentForm"; //$NON-NLS-1$

	public static final String ELEMENT_UI_CONTACT_CONSENT_FIELD = "useContactField"; //$NON-NLS-1$

	public static final String ELEMENT_MONITOR = "monitor"; //$NON-NLS-1$

	private static final String ELEMENT_COLLECTOR_EVENT_FILTERS = "eventFilters"; //$NON-NLS-1$

	private static final String ELEMENT_MONITOR_ACTIVATOR = "activator"; //$NON-NLS-1$

	private boolean extensionsRead = false;

	private final Collection<UsageCollector> usageCollectors = new ArrayList<UsageCollector>();

	private Collection<IMonitorActivator> monitors;

	public Collection<UsageCollector> getUsageCollectors() {
		if (!extensionsRead) {
			readExtensions();
		}
		return usageCollectors;
	}

	public void readExtensions() {
		try {
			if (!extensionsRead) {
				IExtensionRegistry registry = Platform.getExtensionRegistry();
				IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_STUDY);
				if (extensionPoint != null) {
					IExtension[] extensions = extensionPoint.getExtensions();
					for (IExtension extension : extensions) {
						IConfigurationElement[] elements = extension.getConfigurationElements();
						for (IConfigurationElement element : elements) {
							if (element.getName().compareTo(ELEMENT_COLLECTOR) == 0) {
								readUsageCollector(element);
							} else if (element.getName().compareTo(ELEMENT_MONITOR) == 0) {
								readMonitor(element);
							}
						}
					}
					extensionsRead = true;
				}
			}
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					Messages.UiUsageMonitorPlugin_49, t));
		}
	}

	private void readUsageCollector(IConfigurationElement element) {
		String uploadUrl = element.getAttribute(ELEMENT_COLLECTOR_UPLOAD_URL);
		String detailsUrl = element.getAttribute(ELEMENT_COLLECTOR_DETAILS_URL);
		String eventFilters = element.getAttribute(ELEMENT_COLLECTOR_EVENT_FILTERS);
		Collection<String> filters = new ArrayList<String>();

		if (eventFilters != null) {
			filters.addAll(Arrays.asList(eventFilters.split(",")));
		}

		usageCollectors.add(new UsageCollector(element.getContributor().getName(), uploadUrl, detailsUrl, filters));
	}

	private void readMonitor(IConfigurationElement element) throws CoreException {
		if (element.getAttribute(ELEMENT_MONITOR_ACTIVATOR) != null) {
			Object activator = element.createExecutableExtension(ELEMENT_MONITOR_ACTIVATOR);
			if (activator instanceof IMonitorActivator) {
				monitors.add((IMonitorActivator) activator);
			}
		}
	}

	public Collection<IMonitorActivator> getMonitors() {
		return monitors;
	}
}
