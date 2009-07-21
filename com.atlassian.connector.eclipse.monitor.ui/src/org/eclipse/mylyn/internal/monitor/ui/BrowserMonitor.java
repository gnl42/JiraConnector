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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditor;

/**
 * @author Mik Kersten
 */
// TODO e3.3 move to web.ui or tests
public class BrowserMonitor extends AbstractUserInteractionMonitor implements IPartListener, IWindowListener,
		IPageListener {

	public static final String URL_LIST_DELIM = ","; //$NON-NLS-1$

	private final UrlTrackingListener urlTrackingListener = new UrlTrackingListener();

	private List<String> acceptedUrls = new ArrayList<String>();

	class UrlTrackingListener implements LocationListener {

		public void changing(LocationEvent event) {
			// ignore
		}

		public void changed(LocationEvent locationEvent) {
			String url = locationEvent.location;
			boolean accept = false;
			for (String urlMatch : acceptedUrls) {
				if (url.indexOf(urlMatch) != -1) {
					accept = true;
				}
			}
			if (accept) {
				InteractionEvent interactionEvent = new InteractionEvent(InteractionEvent.Kind.SELECTION, "url", url, //$NON-NLS-1$
						WebBrowserEditor.WEB_BROWSER_EDITOR_ID, "null", "", 0); //$NON-NLS-1$ //$NON-NLS-2$
				MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent); // TODO:
				// move
			}
		}
	}

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection, boolean contributeToContext) {
		// ignore, this is a special case
	}

	// ---- Part Listener

	public void partOpened(IWorkbenchPart part) {
		if (part instanceof WebBrowserEditor) {
			Browser browser = getBrowser((WebBrowserEditor) part);
			if (browser != null) {
				browser.addLocationListener(urlTrackingListener);
			}
		}
	}

	public void partClosed(IWorkbenchPart part) {
		if (part instanceof WebBrowserEditor) {
			Browser browser = getBrowser((WebBrowserEditor) part);
			if (browser != null && !browser.isDisposed()) {
				browser.removeLocationListener(urlTrackingListener);
			}
		}
	}

	public void partActivated(IWorkbenchPart part) {
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	private Browser getBrowser(final WebBrowserEditor browserEditor) {
		try { // HACK: using reflection to gain accessibility
			Class<?> browserClass = browserEditor.getClass();
			Field browserField = browserClass.getDeclaredField("webBrowser"); //$NON-NLS-1$
			browserField.setAccessible(true);
			Object browserObject = browserField.get(browserEditor);
			if (browserObject != null && browserObject instanceof BrowserViewer) {
				return ((BrowserViewer) browserObject).getBrowser();
			}
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorUiPlugin.ID_PLUGIN, "Could not add browser listener", //$NON-NLS-1$
					e));
		}
		return null;
	}

	// --- Window listener

	public void windowActivated(IWorkbenchWindow window) {
	}

	public void windowDeactivated(IWorkbenchWindow window) {
	}

	public void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
	}

	public void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
	}

	// ---- IPageListener

	public void pageActivated(IWorkbenchPage page) {
	}

	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
	}

	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	public List<String> getAcceptedUrls() {
		return acceptedUrls;
	}

	public void setAcceptedUrls(String urlBuffer) {
		acceptedUrls = new ArrayList<String>();

		if (urlBuffer != null) {
			StringTokenizer token = new StringTokenizer(urlBuffer, URL_LIST_DELIM);
			while (token.hasMoreTokens()) {
				acceptedUrls.add(token.nextToken());
			}
		}
	}

}
