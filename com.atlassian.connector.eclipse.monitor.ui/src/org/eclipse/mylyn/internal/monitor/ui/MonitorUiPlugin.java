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

package org.eclipse.mylyn.internal.monitor.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.monitor.core.IInteractionEventListener;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.ui.AbstractUserActivityMonitor;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.mylyn.monitor.ui.IActivityContextManager;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Mik Kersten
 * @author Shawn Minto
 * @since 3.0
 */
public class MonitorUiPlugin extends AbstractUIPlugin {

	private static final int DEFAULT_ACTIVITY_TIMEOUT = 180000;

	public static final String ID_PLUGIN = "org.eclipse.mylyn.monitor.ui"; //$NON-NLS-1$

	private static MonitorUiPlugin INSTANCE;

	private ShellLifecycleListener shellLifecycleListener;

	private final List<AbstractUserInteractionMonitor> selectionMonitors = new ArrayList<AbstractUserInteractionMonitor>();

	/**
	 * TODO: this could be merged with context interaction events rather than requiring update from the monitor.
	 */
	private final List<IInteractionEventListener> interactionListeners = new ArrayList<IInteractionEventListener>();

	private ActivityContextManager activityContextManager;

	private final ArrayList<AbstractUserActivityMonitor> monitors = new ArrayList<AbstractUserActivityMonitor>();

	protected Set<IPartListener> partListeners = new HashSet<IPartListener>();

	protected Set<IPageListener> pageListeners = new HashSet<IPageListener>();

	protected Set<IPerspectiveListener> perspectiveListeners = new HashSet<IPerspectiveListener>();

	protected Set<ISelectionListener> postSelectionListeners = new HashSet<ISelectionListener>();

	private final Set<IWorkbenchWindow> monitoredWindows = new HashSet<IWorkbenchWindow>();

	public static final String OBFUSCATED_LABEL = "[obfuscated]"; //$NON-NLS-1$

	public static final String PREF_USER_ACTIVITY_ENABLED = "org.eclipse.mylyn.monitor.user.activity.enabled"; //$NON-NLS-1$

	private IWorkbenchWindow launchingWorkbenchWindow = null;

	private final org.eclipse.jface.util.IPropertyChangeListener PROPERTY_LISTENER = new org.eclipse.jface.util.IPropertyChangeListener() {

		public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
			if (event.getProperty().equals(ActivityContextManager.ACTIVITY_TIMEOUT)
					|| event.getProperty().equals(ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED)) {
				updateActivityTimout();
			} else if (event.getProperty().equals(PREF_USER_ACTIVITY_ENABLED)) {
				if (getPreferenceStore().getBoolean(PREF_USER_ACTIVITY_ENABLED)) {
					activityContextManager.start();
				} else {
					activityContextManager.stop();
				}

			}
		}

	};

	protected IWindowListener WINDOW_LISTENER = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow window) {
			// ignore
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			// ignore
		}

		public void windowOpened(IWorkbenchWindow window) {
			if (getWorkbench().isClosing()) {
				return;
			}

			if (window instanceof IMonitoredWindow) {
				IMonitoredWindow awareWindow = (IMonitoredWindow) window;
				if (!awareWindow.isMonitored()) {
					return;
				}
			}

			addListenersToWindow(window);
		}

		public void windowClosed(IWorkbenchWindow window) {
			removeListenersFromWindow(window);
			if (window == launchingWorkbenchWindow) {
				launchingWorkbenchWindow = null;
			}
		}
	};

	public MonitorUiPlugin() {
		INSTANCE = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		getPreferenceStore().setDefault(ActivityContextManager.ACTIVITY_TIMEOUT, DEFAULT_ACTIVITY_TIMEOUT);
		getPreferenceStore().setDefault(ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED, true);

		if (CoreUtil.TEST_MODE) {
			init();
		} else {
			// FIXME: use UIJob
			// delay initialization until workbench is realized
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					init();
				}
			});
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		try {
			if (Platform.isRunning()) {
				if (activityContextManager != null) {
					activityContextManager.stop();
				}
				getPreferenceStore().removePropertyChangeListener(PROPERTY_LISTENER);
				if (getWorkbench() != null && !getWorkbench().isClosing()) {
					getWorkbench().removeWindowListener(WINDOW_LISTENER);

					if (getWorkbench().getActiveWorkbenchWindow() != null
							&& getWorkbench().getActiveWorkbenchWindow().getShell() != null
							&& !getWorkbench().getActiveWorkbenchWindow().getShell().isDisposed()) {
						getWorkbench().getActiveWorkbenchWindow()
								.getShell()
								.removeShellListener(shellLifecycleListener);
					}

					for (IWorkbenchWindow window : monitoredWindows) {
						removeListenersFromWindow(window);
					}
				}

			}
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN, "Monitor UI stop failed", e)); //$NON-NLS-1$
		}
		INSTANCE = null;
	}

	public ShellLifecycleListener getShellLifecycleListener() {
		return shellLifecycleListener;
	}

	public void addWindowPartListener(IPartListener listener) {
		partListeners.add(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.getPartService().addPartListener(listener);
		}
	}

	public void removeWindowPartListener(IPartListener listener) {
		partListeners.remove(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.getPartService().removePartListener(listener);
		}
	}

	public void addWindowPageListener(IPageListener listener) {
		pageListeners.add(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.addPageListener(listener);
		}
	}

	public void removeWindowPageListener(IPageListener listener) {
		pageListeners.remove(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.removePageListener(listener);
		}
	}

	public void addWindowPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.add(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.addPerspectiveListener(listener);
		}
	}

	public void removeWindowPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.remove(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			window.removePerspectiveListener(listener);
		}
	}

	public void addWindowPostSelectionListener(ISelectionListener listener) {
		postSelectionListeners.add(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			ISelectionService service = window.getSelectionService();
			service.addPostSelectionListener(listener);
		}
	}

	public void removeWindowPostSelectionListener(ISelectionListener listener) {
		getDefault().postSelectionListeners.remove(listener);
		for (IWorkbenchWindow window : monitoredWindows) {
			ISelectionService service = window.getSelectionService();
			service.removePostSelectionListener(listener);
		}
	}

	public static MonitorUiPlugin getDefault() {
		return INSTANCE;
	}

	public List<AbstractUserInteractionMonitor> getSelectionMonitors() {
		return selectionMonitors;
	}

	public void addInteractionListener(IInteractionEventListener listener) {
		interactionListeners.add(listener);
	}

	public void removeInteractionListener(IInteractionEventListener listener) {
		interactionListeners.remove(listener);
	}

	/**
	 * TODO: refactor this, it's awkward
	 */
	public void notifyInteractionObserved(InteractionEvent interactionEvent) {
		for (IInteractionEventListener listener : interactionListeners) {
			listener.interactionObserved(interactionEvent);
		}
	}

	public List<IInteractionEventListener> getInteractionListeners() {
		return interactionListeners;
	}

	class MonitorUiExtensionPointReader {

		public static final String EXTENSION_ID_USER = "org.eclipse.mylyn.monitor.ui.user"; //$NON-NLS-1$

		public static final String ELEMENT_ACTIVITY_TIMER = "osActivityTimer"; //$NON-NLS-1$

		public static final String ELEMENT_CLASS = "class"; //$NON-NLS-1$

		private boolean extensionsRead = false;

		public void initExtensions() {
			try {
				if (!extensionsRead) {
					IExtensionRegistry registry = Platform.getExtensionRegistry();
					IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_USER);
					if (extensionPoint != null) {
						IExtension[] extensions = extensionPoint.getExtensions();
						for (IExtension extension : extensions) {
							IConfigurationElement[] elements = extension.getConfigurationElements();
							for (IConfigurationElement element : elements) {
								if (element.getName().compareTo(ELEMENT_ACTIVITY_TIMER) == 0) {
									readActivityMonitor(element);
								}
							}
						}
						extensionsRead = true;
					}
				}
			} catch (Throwable t) {
				StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN,
						"Could not read monitor extension", t)); //$NON-NLS-1$
			}
		}

		private void readActivityMonitor(IConfigurationElement element) throws CoreException {
			try {
				if (element.getAttribute(ELEMENT_CLASS) != null) {
					Object activityTimer = element.createExecutableExtension(ELEMENT_CLASS);
					if (activityTimer instanceof AbstractUserActivityMonitor) {
						monitors.add(0, (AbstractUserActivityMonitor) activityTimer);
					}
				}
			} catch (Throwable e) {
				StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN, "Could not load activity timer", //$NON-NLS-1$
						e));
			}
		}
	}

	public IActivityContextManager getActivityContextManager() {
		return activityContextManager;
	}

	public boolean suppressConfigurationWizards() {
		List<String> commandLineArgs = Arrays.asList(Platform.getCommandLineArgs());
		if (commandLineArgs.contains("-showMylynWizards")) { //$NON-NLS-1$
			return false;
		} else {
			return commandLineArgs.contains("-pdelaunch"); //$NON-NLS-1$
		}
	}

	private void removeListenersFromWindow(IWorkbenchWindow window) {
		for (IPageListener listener : pageListeners) {
			window.removePageListener(listener);
		}
		for (IPartListener listener : partListeners) {
			window.getPartService().removePartListener(listener);
		}
		for (IPerspectiveListener listener : perspectiveListeners) {
			window.removePerspectiveListener(listener);
		}
		for (ISelectionListener listener : postSelectionListeners) {
			window.getSelectionService().removePostSelectionListener(listener);
		}
		monitoredWindows.remove(window);
	}

	// TODO: consider making API
	private void addListenersToWindow(IWorkbenchWindow window) {
		for (IPageListener listener : pageListeners) {
			window.addPageListener(listener);
		}
		for (IPartListener listener : partListeners) {
			window.getPartService().addPartListener(listener);
		}
		for (IPerspectiveListener listener : perspectiveListeners) {
			window.addPerspectiveListener(listener);
		}
		for (ISelectionListener listener : postSelectionListeners) {
			window.getSelectionService().addPostSelectionListener(listener);
		}

		monitoredWindows.add(window);
	}

	/**
	 * @since 2.2
	 */
	public Set<IWorkbenchWindow> getMonitoredWindows() {
		return monitoredWindows;
	}

	/**
	 * @since 2.2
	 */
	public IWorkbenchWindow getLaunchingWorkbenchWindow() {
		return launchingWorkbenchWindow;
	}

	private void init() {
		try {
			getWorkbench().addWindowListener(WINDOW_LISTENER);
			launchingWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();

			for (IWorkbenchWindow window : getWorkbench().getWorkbenchWindows()) {
				addListenersToWindow(window);
			}

			shellLifecycleListener = new ShellLifecycleListener(ContextCorePlugin.getContextManager());
			getWorkbench().getActiveWorkbenchWindow().getShell().addShellListener(shellLifecycleListener);

			monitors.add(new WorkbenchUserActivityMonitor());
			new MonitorUiExtensionPointReader().initExtensions();

			activityContextManager = new ActivityContextManager(monitors);

			updateActivityTimout();

			if (getPreferenceStore().getBoolean(PREF_USER_ACTIVITY_ENABLED)) {
				activityContextManager.start();
			}

			getPreferenceStore().addPropertyChangeListener(PROPERTY_LISTENER);

		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN, "Monitor UI start failed", e)); //$NON-NLS-1$
		}
	}

	private void updateActivityTimout() {
		if (getPreferenceStore().getBoolean(ActivityContextManager.ACTIVITY_TIMEOUT_ENABLED)) {
			activityContextManager.setInactivityTimeout(getPreferenceStore().getInt(
					ActivityContextManager.ACTIVITY_TIMEOUT));
		} else {
			activityContextManager.setInactivityTimeout(0);
		}
	}
}
