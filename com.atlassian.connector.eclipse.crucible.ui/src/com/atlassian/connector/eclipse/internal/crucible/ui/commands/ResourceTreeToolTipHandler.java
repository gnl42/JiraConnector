/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListToolTip;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Steffen Pingel
 */
public class ResourceTreeToolTipHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);
		if (site instanceof IViewSite) {
			IViewSite viewSite = (IViewSite) site;
			IWorkbenchPart part = viewSite.getPart();
			if (part instanceof TaskListView) {
				TaskListView taskListView = (TaskListView) part;
				execute(event, taskListView);
			}
		}
		return null;
	}

	protected void execute(ExecutionEvent event, TaskListView taskListView) throws ExecutionException {
		TaskListToolTip toolTip = taskListView.getToolTip();
		if (toolTip.isVisible()) {
			toolTip.hide();
		} else {
			taskListView.getViewer().getControl().getBounds();
			Tree tree = taskListView.getViewer().getTree();
			TreeItem[] selection = tree.getSelection();
			if (selection.length > 0) {
				toolTip.show(new Point(selection[0].getBounds().x + 1, selection[0].getBounds().y + 1));
			}
		}
	}

}
