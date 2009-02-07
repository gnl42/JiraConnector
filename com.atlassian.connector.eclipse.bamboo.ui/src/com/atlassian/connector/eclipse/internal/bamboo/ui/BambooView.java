/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.RefreshBuildsForAllRepositoriesJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.DateUtil;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Steffen Pingel
 */
public class BambooView extends ViewPart {

	private class BuildContentProvider implements ITreeContentProvider {

		private List<BambooBuild> allBuilds;

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return allBuilds.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			allBuilds = new ArrayList<BambooBuild>();
			if (newInput != null) {
				boolean hasFailed = false;
				for (Collection<BambooBuild> collection : ((Map<TaskRepository, Collection<BambooBuild>>) newInput).values()) {
					allBuilds.addAll(collection);
					for (BambooBuild build : collection) {
						if (build.getStatus() == BuildStatus.BUILD_FAILED) {
							hasFailed = true;
						}
					}
				}
				updateViewIcon(hasFailed);
			}
		}
	}

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.plans";

	private TreeViewer buildViewer;

	private BambooViewDataProvider bambooDataprovider;

	private final Image buildFailedImage = CommonImages.getImage(BambooImages.STATUS_FAILED);

	private final Image buildPassedImage = CommonImages.getImage(BambooImages.STATUS_PASSED);

	private final Image buildDisabledImage = CommonImages.getImage(BambooImages.STATUS_DISABLED);

	private final Image bambooImage = CommonImages.getImage(BambooImages.BAMBOO);

	private Image currentTitleImage = bambooImage;

	@Override
	public void createPartControl(Composite parent) {
		buildViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		buildViewer.setContentProvider(new BuildContentProvider());
		buildViewer.setUseHashlookup(true);

		TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					return ((BambooBuild) element).getBuildName() + " - " + ((BambooBuild) element).getBuildKey();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof BambooBuild) {
					switch (((BambooBuild) element).getStatus()) {
					case BUILD_FAILED:
						return buildFailedImage;
					case BUILD_SUCCEED:
						return buildPassedImage;
					default:
						return buildDisabledImage;
					}
				}
				return super.getImage(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Status");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					int totalTests = build.getTestsFailed() + build.getTestsPassed();
					if (totalTests == 0) {
						return "Tests: Testless build";
					} else {
						return NLS.bind("Tests: {0} out of {1} failed", new Object[] { build.getTestsFailed(),
								totalTests });
					}
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build Reason");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return build.getBuildReason();
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Last Built");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return DateUtil.getRelativeBuildTime(build.getBuildCompletedDate());
				}
				return super.getText(element);
			}
		});

		//GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(planViewer.getControl());

		contributeToActionBars();

		bambooDataprovider = BambooViewDataProvider.getInstance();
		bambooDataprovider.setView(this);
	}

	@Override
	public void setFocus() {
		// ignore

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillPopupMenu(bars.getMenuManager());
		fillToolBar(bars.getToolBarManager());
	}

	private void fillToolBar(IToolBarManager toolBarManager) {
		Action refreshAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		toolBarManager.add(refreshAction);
	}

	private void refresh(Map<TaskRepository, Collection<BambooBuild>> map) {
		buildViewer.setInput(map);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
	}

	public void buildsChanged() {
		if (bambooDataprovider.getBuilds() != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh(bambooDataprovider.getBuilds());
				}
			});
		}
	}

	private void updateViewIcon(boolean buildsFailed) {
		if (buildsFailed) {
			currentTitleImage = buildFailedImage;
		} else {
			currentTitleImage = bambooImage;
		}
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	/*
	 * @see IWorkbenchPart#getTitleImage()
	 */
	@Override
	public Image getTitleImage() {
		if (currentTitleImage == null) {
			return super.getTitleImage();
		}
		return currentTitleImage;
	}

	private void refreshBuilds() {
		RefreshBuildsForAllRepositoriesJob job = new RefreshBuildsForAllRepositoriesJob("Refreshing builds",
				TasksUi.getRepositoryManager());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					BuildPlanManager.getInstance().handleFinishedRefreshAllBuildsJob(event);
				}
			}
		});
		job.schedule();
	}
}
