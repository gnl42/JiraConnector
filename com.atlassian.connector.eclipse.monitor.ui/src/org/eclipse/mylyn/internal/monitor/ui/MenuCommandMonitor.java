/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Leah Findlater - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Mik Kersten
 * @author Leah Findlater
 * 
 */
public class MenuCommandMonitor implements Listener {

	public static final String MENU_ITEM_ID = "item.label."; //$NON-NLS-1$

	public static final String MENU_ITEM_SELECTED = "menu"; //$NON-NLS-1$

	public static final String TOOLBAR_ITEM_SELECTED = "toolbar"; //$NON-NLS-1$

	public static final String MENU_PATH_DELIM = "/"; //$NON-NLS-1$

	public void handleEvent(Event event) {
		try {
			if (!(event.widget instanceof Item)) {
				return;
			}
			Item item = (Item) event.widget;
			if (item.getData() == null) {
				return;
			}
			Object target = event.widget.getData();
			String id = null;
			String delta = null;
			if (target instanceof IContributionItem) {
				id = ((IContributionItem) target).getId();
			}
			if (id == null && target instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) target).getAction();
				if (action.getId() != null) {
					id = action.getId();
				} else {
					id = action.getClass().getName();
				}
			} else if (id == null) {
				id = target.getClass().getName();
			}

			if (item instanceof MenuItem) {
				MenuItem menu = (MenuItem) item;
				Menu parentMenu = menu.getParent();
				String location = ""; //$NON-NLS-1$
				if (parentMenu != null) {
					while (parentMenu.getParentItem() != null) {
						location = parentMenu.getParentItem().getText() + MENU_PATH_DELIM + location;
						parentMenu = parentMenu.getParentMenu();
					}
				}
				if (id == null) {
					return;
					// TODO: would be good to put back this info in some form
					// but it can contain private data, bug 178604

//					if (id == null) 
//						id = "null";
//					String itemText = obfuscateItemText(item.getText());
//					id = id + "$" + MENU_ITEM_ID + location + itemText;
				}

				delta = MENU_ITEM_SELECTED;
			} else if (item instanceof ToolItem) {
				// TODO: would be good to put back this info in some form
				// but it can contain private data, bug 178604
				// ToolItem tool = (ToolItem) item;
				// if (id == null)
				// 	 id = "null";
				// id = id + "$" + MENU_ITEM_ID + '.' + tool.getToolTipText();
				delta = TOOLBAR_ITEM_SELECTED;
			}
			InteractionEvent interactionEvent = InteractionEvent.makeCommand(id, delta);
			MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);

		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, MonitorUiPlugin.ID_PLUGIN, "Could not log selection", t)); //$NON-NLS-1$
		}
	}
}
