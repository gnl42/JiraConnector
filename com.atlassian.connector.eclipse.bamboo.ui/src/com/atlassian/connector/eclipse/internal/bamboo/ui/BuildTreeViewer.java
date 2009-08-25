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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BuildTreeViewer extends TreeViewer {

	private volatile Thread animation;

	private volatile boolean animationResumed = false;

	private volatile boolean disposed = false;

	public BuildTreeViewer(Composite parent, int i) {
		super(parent, i);

		// Start animation thread
		animation = new Thread() {
			private static final int ANIMATION_FRAME_BREAK = 100;

			@Override
			public void run() {
				Display display = getTree().getDisplay();

				while (!disposed) { // thread stop condition

					synchronized (animation) {
						while (!animationResumed) { // thread suspend/resume condition
							try {
								wait();
							} catch (InterruptedException e) {
								// we can do nothing here
								LoggerImpl.getInstance().warn(
										"Exception occured when calling wait() for the 'build in progress' animation",
										e);
//								StatusHandler.fail(new Status(severity, pluginId, message))
							}
						}
					}

					if (display != null) {
						display.asyncExec(new Runnable() {
							public void run() {
								refresh();
							}
						});
						try {
							Thread.sleep(ANIMATION_FRAME_BREAK);
						} catch (InterruptedException e) {
							// we can do nothing here
							LoggerImpl.getInstance().warn(
									"Exception occured when calling sleep() for the 'build in progress' animation", e);
						}
					}
				}
			}
		};

		animation.start();

		// start general tree refresh ('XXX minutes ago' text is refreshed)
		new Thread() {
			private static final long TREE_REFRESH_INTERVAL = 60000;

			@Override
			public void run() {
				Display display = getTree().getDisplay();

				while (!disposed) { // thread stop condition

					if (display != null) {
						display.asyncExec(new Runnable() {
							public void run() {
								refresh();
							}
						});
						try {
							Thread.sleep(TREE_REFRESH_INTERVAL);
						} catch (InterruptedException e) {
							// we can do nothing here
							LoggerImpl.getInstance().warn(
									"Exception occured when calling sleep() for the build window refresh", e);
						}
					}
				}

			};
		}.start();

	}

	@Override
	public void refresh() {
		TreeItem[] selection = getTree().getSelection();
		super.refresh();
		restoreSelection(selection);
	}

	@Override
	public void refresh(Object element, boolean updateLabels) {
		TreeItem[] selection = getTree().getSelection();
		super.refresh(element, updateLabels);
		restoreSelection(selection);
	}

	@Override
	public void refresh(Object element) {
		TreeItem[] selection = getTree().getSelection();
		super.refresh(element);
		restoreSelection(selection);
	}

	@Override
	public void refresh(boolean updateLabels) {
		TreeItem[] selection = getTree().getSelection();
		super.refresh(updateLabels);
		restoreSelection(selection);
	}

	public void setBuilds(Map<TaskRepository, Collection<BambooBuild>> builds) {
		Collection<BambooBuild> selectedBuilds = getSelectedBuilds();

		super.setInput(builds);

		restoreSelectionByBuild(selectedBuilds);

		// pause animation thread
		animationResumed = false;
		// find if there is a build 'in progress'
		for (Collection<BambooBuild> onlyBuilds : builds.values()) {
			for (BambooBuild build : onlyBuilds) {
				if (build.getStatus() == BuildStatus.BUILDING) {
					// resume animation thread
					animationResumed = true;
					synchronized (animation) {
						animation.notify();
					}
					break;
				}
			}
		}
	}

	private Collection<BambooBuild> getSelectedBuilds() {
		Collection<BambooBuild> selectedBuilds = new ArrayList<BambooBuild>();

		TreeItem[] selection = getTree().getSelection();
		for (TreeItem selectedItem : selection) {
			if (selectedItem.getData() instanceof BambooBuildAdapter) {
				selectedBuilds.add(((BambooBuildAdapter) selectedItem.getData()).getBuild());
			}
		}

		return selectedBuilds;
	}

	private void restoreSelectionByBuild(Collection<BambooBuild> selectedBuilds) {
		Collection<TreeItem> rewritenSelection = new ArrayList<TreeItem>();

		for (TreeItem item : getTree().getItems()) {
			if (item.getData() instanceof BambooBuildAdapter) {
				BambooBuild build = ((BambooBuildAdapter) item.getData()).getBuild();
				for (BambooBuild selectedBuild : selectedBuilds) {
					if (selectedBuild.getServer().equals(build.getServer())
							&& selectedBuild.getProjectName().equals(build.getProjectName())
							&& selectedBuild.getPlanKey().equals(build.getPlanKey())) {
						rewritenSelection.add(item);
					}
				}
			}
		}

		restoreSelection(rewritenSelection.toArray(new TreeItem[0]));
	}

	private void restoreSelection(TreeItem[] selection) {
		getTree().setSelection(selection);
		fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	public void dispose() {
		// stop inner threads
		disposed = true;
	}
}
