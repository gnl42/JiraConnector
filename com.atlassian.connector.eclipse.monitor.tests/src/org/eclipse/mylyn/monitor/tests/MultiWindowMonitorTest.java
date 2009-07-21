/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.mylyn.internal.monitor.ui.IMonitoredWindow;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @author Brian de Alwis
 * @author Mik Kersten
 * @author Shawn Minto
 */
public class MultiWindowMonitorTest extends TestCase {

	private class ContextAwareWorkbenchWindow extends WorkbenchWindow implements IMonitoredWindow {

		private boolean monitored = true;

		public ContextAwareWorkbenchWindow(int number, boolean monitored) {
			super(number);
			this.monitored = monitored;
		}

		public boolean isMonitored() {
			return monitored;
		}

		public boolean isPerspectiveManaged() {
			return false;
		}
	}

	private class MockWorkbench {
		private final Workbench wBench;

		private WindowManager parentManager = null;

		private MockWorkbench() {
			wBench = Workbench.getInstance();

			Field wManagerField;
			try {
				wManagerField = Workbench.class.getDeclaredField("windowManager");
				wManagerField.setAccessible(true);
				parentManager = (WindowManager) wManagerField.get(wBench);

			} catch (Exception e) {
			}
		}

		private int getNewWindowNumber() {
			Window[] windows = parentManager.getWindows();
			int count = windows.length;

			boolean checkArray[] = new boolean[count];
			for (int nX = 0; nX < count; nX++) {
				if (windows[nX] instanceof WorkbenchWindow) {
					WorkbenchWindow ww = (WorkbenchWindow) windows[nX];
					int index = ww.getNumber() - 1;
					if (index >= 0 && index < count) {
						checkArray[index] = true;
					}
				}
			}

			for (int index = 0; index < count; index++) {
				if (!checkArray[index]) {
					return index + 1;
				}
			}
			return count + 1;
		}

		private ContextAwareWorkbenchWindow newWorkbenchWindow(boolean isMonitored) {
			return new ContextAwareWorkbenchWindow(getNewWindowNumber(), isMonitored);
		}

		public ContextAwareWorkbenchWindow restoreState(IMemento memento, boolean isMonitored) {

			ContextAwareWorkbenchWindow newWindow = newWorkbenchWindow(isMonitored);
			newWindow.create();

			parentManager.add(newWindow);

			boolean opened = false;

			try {
				newWindow.restoreState(memento, null);
				newWindow.open();
				opened = true;
			} finally {
				if (!opened) {
					newWindow.close();
				}
			}

			return newWindow;
		}
	}

	private final InteractionEventLogger logger = UiUsageMonitorPlugin.getDefault().getInteractionLogger();

	private final MockSelectionMonitor selectionMonitor = new MockSelectionMonitor();

	private IWorkbenchWindow window1;

	private IWorkbenchWindow window2;

	private IWorkbenchWindow window3;

	private IWorkbenchWindow window4;

	private boolean monitoringWasEnabled;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		monitoringWasEnabled = UiUsageMonitorPlugin.getDefault().isMonitoringEnabled();
		UiUsageMonitorPlugin.getDefault().stopMonitoring();

		// make sure the MonitorUiPlugin is fully initialized
		while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
		}

		window1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		assertTrue(MonitorUiPlugin.getDefault().getMonitoredWindows().contains(window1));
		window2 = duplicateWindow(window1);
		assertNotNull(window2);
		assertTrue(MonitorUiPlugin.getDefault().getMonitoredWindows().contains(window2));
		window3 = createContextAwareWindow(true, window1);
		assertNotNull(window3);
		assertTrue(MonitorUiPlugin.getDefault().getMonitoredWindows().contains(window3));
		window4 = createContextAwareWindow(false, window1);
		assertNotNull(window4);
		assertFalse(MonitorUiPlugin.getDefault().getMonitoredWindows().contains(window4));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		window2.close();
		window3.close();
		window4.close();
		if (monitoringWasEnabled) {
			UiUsageMonitorPlugin.getDefault().startMonitoring();
		}
	}

	protected void generateSelection(IWorkbenchWindow w) {
		selectionMonitor.selectionChanged(w.getActivePage().getActivePart(), new StructuredSelection("yo"));
	}

	public void testMultipleWindows() throws IOException {
		File monitorFile = UiUsageMonitorPlugin.getDefault().getMonitorLogFile();
		logger.clearInteractionHistory();
		assertEquals(0, logger.getHistoryFromFile(monitorFile).size());

		generateSelection(window1);
		assertEquals(0, logger.getHistoryFromFile(monitorFile).size());

		UiUsageMonitorPlugin.getDefault().startMonitoring();
		generateSelection(window1);
		generateSelection(window2);
		generateSelection(window3);
		generateSelection(window4);
		assertEquals(3, logger.getHistoryFromFile(monitorFile).size());
	}

	protected IWorkbenchWindow duplicateWindow(IWorkbenchWindow window) {
		WorkbenchWindow w = (WorkbenchWindow) window;
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus status = w.saveState(memento);
		if (!status.isOK()) {
			fail("failed to duplicate window: " + status);
		}
		return restoreWorkbenchWindow((Workbench) w.getWorkbench(), memento);
	}

	private IWorkbenchWindow createContextAwareWindow(boolean monitored, IWorkbenchWindow window) {
		WorkbenchWindow w = (WorkbenchWindow) window;
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus status = w.saveState(memento);
		if (!status.isOK()) {
			fail("failed to duplicate window: " + status);
		}
		return new MockWorkbench().restoreState(memento, monitored);
	}

	protected IWorkbenchWindow restoreWorkbenchWindow(Workbench workbench, IMemento memento) {
		return (IWorkbenchWindow) invokeMethod(workbench, "restoreWorkbenchWindow", new Class[] { IMemento.class },
				new Object[] { memento });
	}

	protected Object invokeMethod(Object instance, String methodName, Class<?> argTypes[], Object arguments[]) {
		Class<?> clas = instance.getClass();
		try {
			Method method = clas.getDeclaredMethod(methodName, argTypes);
			method.setAccessible(true);
			return method.invoke(instance, arguments);
		} catch (Exception ex) {
			fail("exception during reflective invocation of " + clas.getName() + "." + methodName + ": " + ex);
			return null;
		}
	}

}
