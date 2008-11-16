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

/**
 * 
 */
package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.theplugin.commons.bamboo.BambooStatusTooltipListener;
import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.eclipse.actions.bamboo.CommentBuildAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.LabelBuildAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.RefreshBuildsListAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.RunBuildAction;
import com.atlassian.theplugin.eclipse.actions.bamboo.ShowBuildLogAction;
import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * @author Jacek
 *
 */
public class BambooToolWindow extends ViewPart {

	private IAction runBuildAction;
	private IAction labelBuildAction;
	private IAction commentBuildAction;
	private ShowBuildLogAction showBuilLogAction;
	private RefreshBuildsListAction refreshBuildListAction;
	
	private BambooToolWindowContent bambooToolWindowContent;
	private BambooStatusTooltipListener popupListener;
	private SubStatusLineManager statusLineManager;
	private BambooStatusBar bambooStatusLine;

	/**
	 * 
	 */
	public BambooToolWindow() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		// add toolbar buttons to the view
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		this.runBuildAction = new RunBuildAction(this); 
		this.labelBuildAction = new LabelBuildAction(this);
		this.commentBuildAction = new CommentBuildAction(this);
		this.showBuilLogAction = new ShowBuildLogAction(this);
		this.refreshBuildListAction = new RefreshBuildsListAction();
		
		toolBarManager.add(runBuildAction);
		toolBarManager.add(labelBuildAction);
		toolBarManager.add(commentBuildAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(showBuilLogAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(refreshBuildListAction);
		toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		bambooToolWindowContent = new BambooToolWindowContent(parent, this);
		
		// register view as a bamboo checker listener
		Activator.getDefault().getBambooChecker().registerListener(bambooToolWindowContent);
		
		// create and register popup as a bamboo checker listener
		BambooStatusTooltip popup = new BambooStatusTooltip();
		popupListener = new BambooStatusTooltipListener(popup, Activator.getDefault().getPluginConfiguration());
		Activator.getDefault().getBambooChecker().registerListener(popupListener);
		Activator.getDefault().registerConfigurationListener(popupListener);
		
		//getViewSite().registerContextMenu(menuManager, selectionProvider)
		
		// add status line
		statusLineManager = (SubStatusLineManager) getViewSite().getActionBars().getStatusLineManager();
		bambooStatusLine = new BambooStatusBar();
		statusLineManager.getParent().add(bambooStatusLine);
		Activator.getDefault().getBambooChecker().registerListener(
				new StausIconBambooListener(bambooStatusLine, Activator.getDefault().getPluginConfiguration()));
		
		Activator.getDefault().rescheduleStatusCheckers();
		
		//getViewSite().getActionBars().updateActionBars();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}
	
	public void setHeaderText(String text) {
		setContentDescription(text);
	}
	
	public void setStatusBarText(String text) {
		getViewSite().getActionBars().getStatusLineManager().setMessage(text);
	}

	public BambooToolWindowContent getBambooToolWindowContent() {
		return bambooToolWindowContent;
	}

	public void enableBambooBuildActions() {
		runBuildAction.setEnabled(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
	}
	
	public void enableBamboo2BuildActions() {
		labelBuildAction.setEnabled(true);
		commentBuildAction.setEnabled(true);
		showBuilLogAction.setEnabled(true);
		getViewSite().getActionBars().getToolBarManager().update(true);
	}
	
	public void disableBambooBuildActions() {
		runBuildAction.setEnabled(false);
		labelBuildAction.setEnabled(false);
		commentBuildAction.setEnabled(false);
		showBuilLogAction.setEnabled(false);
		
		getViewSite().getActionBars().getToolBarManager().update(true);
	}

	@Override
	public void dispose() {
		
		super.dispose();
		
		if (bambooToolWindowContent != null) {
			Activator.getDefault().getBambooChecker().unregisterListener(bambooToolWindowContent);
		}
		
		statusLineManager.getParent().remove(bambooStatusLine);
		statusLineManager.getParent().update(true);
		
	}

	public IAction getRunBuildAction() {
		return runBuildAction;
	}

	public IAction getLabelBuildAction() {
		return labelBuildAction;
	}

	public IAction getCommentBuildAction() {
		return commentBuildAction;
	}

	public ShowBuildLogAction getShowBuilLogAction() {
		return showBuilLogAction;
	}

	public RefreshBuildsListAction getRefreshBuildListAction() {
		return refreshBuildListAction;
	}

	
}

