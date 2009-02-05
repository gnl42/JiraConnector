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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooClientManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Steffen Pingel
 */
public class BambooView extends ViewPart {

	private class RefreshBuildsJob extends Job {

		private final ArrayList<BambooBuild> builds;

		public RefreshBuildsJob(String name) {
			super(name);
			this.builds = new ArrayList<BambooBuild>();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			Set<TaskRepository> repositories = TasksUi.getRepositoryManager().getRepositories(
					BambooCorePlugin.CONNECTOR_KIND);
			MultiStatus result = new MultiStatus(BambooUiPlugin.PLUGIN_ID, 0, "Retrieval of Bamboo builds failed", null);
			for (TaskRepository repository : repositories) {
				BambooClient client = clientManager.getClient(repository);
				try {
					this.builds.addAll(client.getBuilds(monitor));
				} catch (CoreException e) {
					result.add(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, NLS.bind(
							"Update of builds from {0} failed", repository.getRepositoryLabel()), e));
				}
			}
			return result;
		}

		public List<BambooBuild> getBuilds() {
			return builds;
		}

	}

	private class BuildContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return ((Collection<?>) inputElement).toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private TreeViewer buildViewer;

	private BambooViewDataProvider bambooDataprovider;

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
						return CommonImages.getImage(BambooImages.STATUS_FAILED);
					case BUILD_SUCCEED:
						return CommonImages.getImage(BambooImages.STATUS_PASSED);
					default:
						return CommonImages.getImage(BambooImages.STATUS_DISABLED);
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
		column.getColumn().setText("Build Reason");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return build.getBuildRelativeBuildDate();
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

	private void refresh(List<BambooBuild> builds) {
		buildViewer.setInput(builds);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
	}

	public void buildsChanged() {
		if (bambooDataprovider.getBuilds() == null) {
			refreshBuilds();
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh(bambooDataprovider.getBuilds());
				}
			});
		}
	}

	private void refreshBuilds() {
		RefreshBuildsJob job = new RefreshBuildsJob("Refreshing builds");
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (buildViewer.getControl() != null && !buildViewer.getControl().isDisposed()) {
								refresh(((RefreshBuildsJob) event.getJob()).getBuilds());
							}
						}
					});
				}
			}
		});
		job.schedule();
	}
}
