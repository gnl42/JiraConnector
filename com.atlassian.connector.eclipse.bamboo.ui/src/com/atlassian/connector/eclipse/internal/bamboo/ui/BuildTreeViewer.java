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
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.UIJob;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author Jacek Jaroczynski
 */
public class BuildTreeViewer extends TreeViewer {

	private volatile boolean disposed = false;

	public BuildTreeViewer(Composite parent, int i) {
		super(parent, i);
		Job lastBuildRefreshJob = new UIJob(parent.getDisplay(), "Bamboo View Last Build Refresh Job") {

			private static final long TREE_REFRESH_INTERVAL = 60 * 1000;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!disposed) {
					update();
					schedule(TREE_REFRESH_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		lastBuildRefreshJob.setUser(false);
		lastBuildRefreshJob.setSystem(true);
		lastBuildRefreshJob.schedule();
	}

	private void update() {
		// we need to check this here as synchronization mechanism used by threads does not guarantee that
		// that SWT widget is not disposed at this moment (see PLE-690)
		if (!getTree().isDisposed()) {
			update(getInput(), new String[] { IBasicPropertyConstants.P_TEXT });
		}
	}

	@Override
	public void refresh() {
		refresh(getRoot());
	}

	@Override
	public void refresh(Object element, boolean updateLabels) {
		if (disposed || getTree().isDisposed()) {
			return;
		}
		TreeItem[] selection = getTree().getSelection();
		super.refresh(element, updateLabels);
		restoreSelection(selection);
	}

	@Override
	public void refresh(Object element) {
		if (disposed || getTree().isDisposed()) {
			return;
		}
		TreeItem[] selection = getTree().getSelection();
		super.refresh(element);
		restoreSelection(selection);
	}

	@Override
	public void refresh(boolean updateLabels) {
		if (disposed || getTree().isDisposed()) {
			return;
		}
		TreeItem[] selection = getTree().getSelection();
		super.refresh(updateLabels);
		restoreSelection(selection);
	}

	public void setBuilds(Collection<EclipseBambooBuild> builds) {
//		Collection<BambooBuild> selectedBuilds = getSelectedBuilds();

		Object[] expandedElements = super.getExpandedElements();
		TreePath[] expandedTreePaths = super.getExpandedTreePaths();

		super.setInput(builds);

		super.setExpandedElements(expandedElements);
		super.setExpandedTreePaths(expandedTreePaths);

//		restoreSelectionByBuild(selectedBuilds);
	}

	private Collection<BambooBuild> getSelectedBuilds() {
		Collection<BambooBuild> selectedBuilds = new ArrayList<BambooBuild>();

		TreeItem[] selection = getTree().getSelection();
		for (TreeItem selectedItem : selection) {
			if (selectedItem.getData() instanceof EclipseBambooBuild) {
				selectedBuilds.add(((EclipseBambooBuild) selectedItem.getData()).getBuild());
			}
		}

		return selectedBuilds;
	}

	private void restoreSelectionByBuild(Collection<BambooBuild> selectedBuilds) {
		Collection<TreeItem> rewritenSelection = new ArrayList<TreeItem>();

		for (TreeItem item : getTree().getItems()) {
			if (item.getData() instanceof EclipseBambooBuild) {
				BambooBuild build = ((EclipseBambooBuild) item.getData()).getBuild();
				for (BambooBuild selectedBuild : selectedBuilds) {
					if (selectedBuild.getServer().equals(build.getServer())
							&& MiscUtil.isEqual(selectedBuild.getProjectName(), build.getProjectName())
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

	@SuppressWarnings("unchecked")
	public Collection<EclipseBambooBuild> getInput() {
		return (Collection<EclipseBambooBuild>) super.getInput();
	}

}
