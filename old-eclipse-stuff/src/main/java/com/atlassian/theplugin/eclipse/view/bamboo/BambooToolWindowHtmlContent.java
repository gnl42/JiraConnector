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

package com.atlassian.theplugin.eclipse.view.bamboo;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;

public class BambooToolWindowHtmlContent implements BambooStatusDisplay {

	//private Composite parent;

	private Browser htmlBrowser;

	private static boolean linkClicked = false;

	private static String html;

	public BambooToolWindowHtmlContent(Composite parent) {
		//this.parent = parent;
		htmlBrowser = new Browser(parent, SWT.NONE);

		htmlBrowser.addMouseListener(new MouseClickListener());
		htmlBrowser
				.addLocationListener(new BrowserLocationListener(htmlBrowser));
	}

	public void updateBambooStatus(BuildStatus arg0, BambooPopupInfo info) {
		BambooToolWindowHtmlContent.html = info.toHtml();
		linkClicked = false;
		htmlBrowser.setText(html);
	}

	private class MouseClickListener extends MouseAdapter {

		public MouseClickListener() {
			super();
			System.out.print("");
		}

		@Override
		public void mouseUp(MouseEvent e) {
			super.mouseUp(e);
			// mouse clicked
			BambooToolWindowHtmlContent.linkClicked = true;
			System.out.println("click");
		}
	}

	private class BrowserLocationListener extends LocationAdapter {

		private Browser browser;

		public BrowserLocationListener(Browser htmlBrowser) {
			super();
			this.browser = htmlBrowser;
		}

		@Override
		public void changing(LocationEvent event) {
			super.changing(event);
			if (BambooToolWindowHtmlContent.linkClicked) {
				try {
					Activator.getDefault().getWorkbench().getBrowserSupport()
							.createBrowser(
									IWorkbenchBrowserSupport.AS_EXTERNAL,
									"aCustomId", "url", "url").openURL(
									new URL(event.location));
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				browser.stop();
				browser.setText(html);
				BambooToolWindowHtmlContent.linkClicked = false;
				System.out.println("changing");
			}
		}

	}

}
