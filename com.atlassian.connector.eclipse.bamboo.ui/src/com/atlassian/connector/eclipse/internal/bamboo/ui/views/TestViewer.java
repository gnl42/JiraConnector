/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *******************************************************************************/
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

package com.atlassian.connector.eclipse.internal.bamboo.ui.views;

import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestCaseElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestElement.Status;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestJobElement;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestRoot;
import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestSuiteElement;
import com.atlassian.theplugin.commons.bamboo.BambooJob;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.SelectionProviderMediator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.PageBook;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestViewer {
	private final class TestSelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			handleSelected();
		}
	}

	private final class TestOpenListener extends SelectionAdapter {
		public void widgetDefaultSelected(SelectionEvent e) {
			handleDefaultSelected();
		}
	}

	private final class FailuresOnlyFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return select(((TestElement) element));
		}

		public boolean select(TestElement testElement) {
			Status status = testElement.getStatus();
			return status.isError();
		}
	}

	private static class ReverseList extends AbstractList {
		private final List fList;

		public ReverseList(List list) {
			fList = list;
		}

		public Object get(int index) {
			return fList.get(fList.size() - index - 1);
		}

		public int size() {
			return fList.size();
		}
	}

	private class ExpandAllAction extends Action {
		public ExpandAllAction() {
			setText("Expand All");
			setToolTipText("Expand All Nodes");
		}

		public void run() {
			fTreeViewer.expandAll();
		}
	}

	private final FailuresOnlyFilter fFailuresOnlyFilter = new FailuresOnlyFilter();

	private final TestResultsView fTestRunnerPart;

	private final Clipboard fClipboard;

	private PageBook fViewerbook;

	private TreeViewer fTreeViewer;

	private TestSessionTreeContentProvider fTreeContentProvider;

	private TestSessionLabelProvider fTreeLabelProvider;

	private TableViewer fTableViewer;

	private TestSessionTableContentProvider fTableContentProvider;

	private TestSessionLabelProvider fTableLabelProvider;

	private SelectionProviderMediator fSelectionProvider;

	private int fLayoutMode;

	private boolean fTreeHasFilter;

	private boolean fTableHasFilter;

	private boolean fTreeNeedsRefresh;

	private boolean fTableNeedsRefresh;

	private HashSet/*<TestElement>*/fNeedUpdate;

	private LinkedList/*<TestSuiteElement>*/fAutoClose;

	private HashSet/*<TestSuite>*/fAutoExpand;

	private BuildDetails fBuildDetails;

	private TestRoot fTestRoot;

	public TestViewer(Composite parent, Clipboard clipboard, TestResultsView runner) {
		fTestRunnerPart = runner;
		fClipboard = clipboard;

		fLayoutMode = TestResultsView.LAYOUT_HIERARCHICAL;

		createTestViewers(parent);

		registerViewersRefresh();

		initContextMenu();
	}

	private void createTestViewers(Composite parent) {
		fViewerbook = new PageBook(parent, SWT.NULL);

		fTreeViewer = new TreeViewer(fViewerbook, SWT.V_SCROLL | SWT.SINGLE);
		fTreeViewer.setUseHashlookup(true);
		fTreeContentProvider = new TestSessionTreeContentProvider();
		fTreeViewer.setContentProvider(fTreeContentProvider);
		fTreeLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestResultsView.LAYOUT_HIERARCHICAL);
		fTreeViewer.setLabelProvider(new ColoringLabelProvider(fTreeLabelProvider));

		fTableViewer = new TableViewer(fViewerbook, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		fTableViewer.setUseHashlookup(true);
		fTableContentProvider = new TestSessionTableContentProvider();
		fTableViewer.setContentProvider(fTableContentProvider);
		fTableLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestResultsView.LAYOUT_FLAT);
		fTableViewer.setLabelProvider(new ColoringLabelProvider(fTableLabelProvider));

		fSelectionProvider = new SelectionProviderMediator(new StructuredViewer[] { fTreeViewer, fTableViewer },
				fTreeViewer);
		fSelectionProvider.addSelectionChangedListener(new TestSelectionListener());
		TestOpenListener testOpenListener = new TestOpenListener();
		fTreeViewer.getTree().addSelectionListener(testOpenListener);
		fTableViewer.getTable().addSelectionListener(testOpenListener);

		fTestRunnerPart.getSite().setSelectionProvider(fSelectionProvider);

		fViewerbook.showPage(fTreeViewer.getTree());
	}

	private void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		fTestRunnerPart.getSite().registerContextMenu(menuMgr, fSelectionProvider);
		Menu menu = menuMgr.createContextMenu(fViewerbook);
		fTreeViewer.getTree().setMenu(menu);
		fTableViewer.getTable().setMenu(menu);
	}

	void handleMenuAboutToShow(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
		if (!selection.isEmpty()) {
			TestElement testElement = (TestElement) selection.getFirstElement();

			String testLabel = testElement.getTestName();
			if (testElement instanceof TestSuiteElement) {
				manager.add(new OpenTestAction(fTestRunnerPart, testLabel));
				manager.add(new Separator());
			} else {
				TestCaseElement testCaseElement = (TestCaseElement) testElement;
				manager.add(new OpenTestAction(fTestRunnerPart, testCaseElement));
				manager.add(new Separator());
			}
			if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
				manager.add(new Separator());
				manager.add(new ExpandAllAction());
			}

		}
		if (fBuildDetails != null && fBuildDetails.getFailedTestDetails().size() > 0) {
			if (fLayoutMode != TestResultsView.LAYOUT_HIERARCHICAL) {
				manager.add(new Separator());
			}
			manager.add(new CopyFailureListAction(fTestRunnerPart, fClipboard));
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
	}

	public Control getTestViewerControl() {
		return fViewerbook;
	}

	public synchronized void setBuildDetails(String buildKey, BuildDetails buildDetails) {
		fBuildDetails = buildDetails;
		fTestRoot = new TestRoot(buildKey, fBuildDetails);

		if (fBuildDetails.getJobs().size() > 0) {
			for (BambooJob job : buildDetails.getJobs()) {
				String jobString = (job.getShortName() != null && job.getShortName().length() > 0) ? job.getShortName()
						: job.getName() + " [" + job.getShortKey() + "]";
				TestJobElement jobElement = new TestJobElement(fTestRoot, jobString, 1);

				Map<String, TestSuiteElement> testSuites = MiscUtil.buildHashMap();

				for (TestDetails test : job.getFailedTests()) {
					if (!testSuites.containsKey(test.getTestClassName())) {
						testSuites.put(test.getTestClassName(),
								new TestSuiteElement(jobElement, test.getTestClassName(), 1));
					}

					TestCaseElement tce = new TestCaseElement(testSuites.get(test.getTestClassName()), String.format(
							"%s(%s)", test.getTestMethodName(), test.getTestClassName()));
					tce.setElapsedTimeInSeconds(test.getTestDuration());
					tce.setStatus(Status.ERROR, test.getErrors(), null, null);
				}

				for (TestDetails test : job.getSuccessfulTests()) {
					if (!testSuites.containsKey(test.getTestClassName())) {
						testSuites.put(test.getTestClassName(),
								new TestSuiteElement(jobElement, test.getTestClassName(), 1));
					}

					TestCaseElement tce = new TestCaseElement(testSuites.get(test.getTestClassName()), String.format(
							"%s(%s)", test.getTestMethodName(), test.getTestClassName()));
					tce.setElapsedTimeInSeconds(test.getTestDuration());
					tce.setStatus(Status.OK, test.getErrors(), null, null);
				}

			}
		} else {

			Map<String, TestSuiteElement> testSuites = MiscUtil.buildHashMap();

			for (TestDetails test : buildDetails.getFailedTestDetails()) {
				if (!testSuites.containsKey(test.getTestClassName())) {
					testSuites.put(test.getTestClassName(), new TestSuiteElement(fTestRoot, test.getTestClassName(), 1));
				}

				TestCaseElement tce = new TestCaseElement(testSuites.get(test.getTestClassName()), String.format(
						"%s(%s)", test.getTestMethodName(), test.getTestClassName()));
				tce.setElapsedTimeInSeconds(test.getTestDuration());
				tce.setStatus(Status.ERROR, test.getErrors(), null, null);
			}

			for (TestDetails test : buildDetails.getSuccessfulTestDetails()) {
				if (!testSuites.containsKey(test.getTestClassName())) {
					testSuites.put(test.getTestClassName(), new TestSuiteElement(fTestRoot, test.getTestClassName(), 1));
				}

				TestCaseElement tce = new TestCaseElement(testSuites.get(test.getTestClassName()), String.format(
						"%s(%s)", test.getTestMethodName(), test.getTestClassName()));
				tce.setElapsedTimeInSeconds(test.getTestDuration());
				tce.setStatus(Status.OK, test.getErrors(), null, null);
			}
		}

		registerViewersRefresh();
	}

	void handleDefaultSelected() {
		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
		if (selection.size() != 1) {
			return;
		}

		TestElement testElement = (TestElement) selection.getFirstElement();

		OpenTestAction action;
		if (testElement instanceof TestJobElement) {
			// do nothing
			return;
		} else if (testElement instanceof TestSuiteElement) {
			action = new OpenTestAction(fTestRunnerPart, testElement.getTestName());
		} else if (testElement instanceof TestCaseElement) {
			TestCaseElement testCase = (TestCaseElement) testElement;
			action = new OpenTestAction(fTestRunnerPart, testCase);
		} else {
			throw new IllegalStateException(String.valueOf(testElement));
		}

		if (action.isEnabled()) {
			action.run();
		}
	}

	private void handleSelected() {
		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
		TestElement testElement = null;
		if (selection.size() == 1) {
			testElement = (TestElement) selection.getFirstElement();
		}
		fTestRunnerPart.handleTestSelected(testElement);
	}

	public synchronized void setShowTime(boolean showTime) {
		try {
			fViewerbook.setRedraw(false);
			fTreeLabelProvider.setShowTime(showTime);
			fTableLabelProvider.setShowTime(showTime);
		} finally {
			fViewerbook.setRedraw(true);
		}
	}

	public synchronized void setShowFailuresOnly(boolean failuresOnly, int layoutMode) {
		/*
		 * Management of fTreeViewer and fTableViewer
		 * ******************************************
		 * - invisible viewer is updated on registerViewerUpdate unless its f*NeedsRefresh is true
		 * - invisible viewer is not refreshed upfront
		 * - on layout change, new viewer is refreshed if necessary
		 * - filter only applies to "current" layout mode / viewer
		 */
		try {
			fViewerbook.setRedraw(false);

			IStructuredSelection selection = null;
			boolean switchLayout = layoutMode != fLayoutMode;
			if (switchLayout) {
				selection = (IStructuredSelection) fSelectionProvider.getSelection();
				if (layoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
					if (fTreeNeedsRefresh) {
						clearUpdateAndExpansion();
					}
				} else {
					if (fTableNeedsRefresh) {
						clearUpdateAndExpansion();
					}
				}
				fLayoutMode = layoutMode;
				fViewerbook.showPage(getActiveViewer().getControl());
			}

			//avoid realizing all TableItems, especially in flat mode!
			StructuredViewer viewer = getActiveViewer();
			if (failuresOnly) {
				if (!getActiveViewerHasFilter()) {
					setActiveViewerNeedsRefresh(true);
					setActiveViewerHasFilter(true);
					viewer.setInput(null);
					viewer.addFilter(fFailuresOnlyFilter);
				}

			} else {
				if (getActiveViewerHasFilter()) {
					setActiveViewerNeedsRefresh(true);
					setActiveViewerHasFilter(false);
					viewer.setInput(null);
					viewer.removeFilter(fFailuresOnlyFilter);
				}
			}
			processChangesInUI();

			if (selection != null) {
				// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=125708
				// (ITreeSelection not adapted if TreePaths changed):
				StructuredSelection flatSelection = new StructuredSelection(selection.toList());
				fSelectionProvider.setSelection(flatSelection, true);
			}

		} finally {
			fViewerbook.setRedraw(true);
		}
	}

	private boolean getActiveViewerHasFilter() {
		if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
			return fTreeHasFilter;
		} else {
			return fTableHasFilter;
		}
	}

	private void setActiveViewerHasFilter(boolean filter) {
		if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
			fTreeHasFilter = filter;
		} else {
			fTableHasFilter = filter;
		}
	}

	private StructuredViewer getActiveViewer() {
		if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
			return fTreeViewer;
		} else {
			return fTableViewer;
		}
	}

	private boolean getActiveViewerNeedsRefresh() {
		if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
			return fTreeNeedsRefresh;
		} else {
			return fTableNeedsRefresh;
		}
	}

	private void setActiveViewerNeedsRefresh(boolean needsRefresh) {
		if (fLayoutMode == TestResultsView.LAYOUT_HIERARCHICAL) {
			fTreeNeedsRefresh = needsRefresh;
		} else {
			fTableNeedsRefresh = needsRefresh;
		}
	}

	/**
	 * To be called periodically by the TestRunnerViewPart (in the UI thread).
	 */
	public void processChangesInUI() {
		if (fBuildDetails == null) {
			registerViewersRefresh();
			fTreeNeedsRefresh = false;
			fTableNeedsRefresh = false;
			fTreeViewer.setInput(null);
			fTableViewer.setInput(null);
			return;
		}

		StructuredViewer viewer = getActiveViewer();
		if (getActiveViewerNeedsRefresh()) {
			clearUpdateAndExpansion();
			setActiveViewerNeedsRefresh(false);
			viewer.setInput(fTestRoot);
		} else {
			Object[] toUpdate;
			synchronized (this) {
				toUpdate = fNeedUpdate.toArray();
				fNeedUpdate.clear();
			}
			if (!fTreeNeedsRefresh && toUpdate.length > 0) {
				if (fTreeHasFilter) {
					for (Object element : toUpdate) {
						updateElementInTree((TestElement) element);
					}
				} else {
					HashSet toUpdateWithParents = new HashSet();
					toUpdateWithParents.addAll(Arrays.asList(toUpdate));
					for (Object element : toUpdate) {
						TestElement parent = ((TestElement) element).getParent();
						while (parent != null) {
							toUpdateWithParents.add(parent);
							parent = parent.getParent();
						}
					}
					fTreeViewer.update(toUpdateWithParents.toArray(), null);
				}
			}
			if (!fTableNeedsRefresh && toUpdate.length > 0) {
				if (fTableHasFilter) {
					for (Object element : toUpdate) {
						updateElementInTable((TestElement) element);
					}
				} else {
					fTableViewer.update(toUpdate, null);
				}
			}
		}
	}

	private void updateElementInTree(final TestElement testElement) {
		if (isShown(testElement)) {
			updateShownElementInTree(testElement);
		} else {
			TestElement current = testElement;
			do {
				if (fTreeViewer.testFindItem(current) != null) {
					fTreeViewer.remove(current);
				}
				current = current.getParent();
			} while (!(current instanceof TestRoot) && !isShown(current));

			while (current != null && !(current instanceof TestRoot)) {
				fTreeViewer.update(current, null);
				current = current.getParent();
			}
		}
	}

	private void updateShownElementInTree(TestElement testElement) {
		if (testElement == null || testElement instanceof TestRoot) {
			return;
		}

		TestSuiteElement parent = testElement.getParent();
		updateShownElementInTree(parent); // make sure parent is shown and up-to-date

		if (fTreeViewer.testFindItem(testElement) == null) {
			fTreeViewer.add(parent, testElement); // if not yet in tree: add
		} else {
			fTreeViewer.update(testElement, null); // if in tree: update
		}
	}

	private void updateElementInTable(TestElement element) {
		if (isShown(element)) {
			if (fTableViewer.testFindItem(element) == null) {
				TestElement previous = getNextFailure(element, false);
				int insertionIndex = -1;
				if (previous != null) {
					TableItem item = (TableItem) fTableViewer.testFindItem(previous);
					if (item != null) {
						insertionIndex = fTableViewer.getTable().indexOf(item);
					}
				}
				fTableViewer.insert(element, insertionIndex);
			} else {
				fTableViewer.update(element, null);
			}
		} else {
			fTableViewer.remove(element);
		}
	}

	private boolean isShown(TestElement current) {
		return fFailuresOnlyFilter.select(current);
	}

	public void selectFirstFailure() {
		TestCaseElement firstFailure = getNextChildFailure(fTestRoot, true);
		if (firstFailure != null) {
			getActiveViewer().setSelection(new StructuredSelection(firstFailure), true);
		}
	}

	public void selectFailure(boolean showNext) {
		IStructuredSelection selection = (IStructuredSelection) getActiveViewer().getSelection();
		TestElement selected = (TestElement) selection.getFirstElement();
		TestElement next;

		if (selected == null) {
			next = getNextChildFailure(fTestRoot, showNext);
		} else {
			next = getNextFailure(selected, showNext);
		}

		if (next != null) {
			getActiveViewer().setSelection(new StructuredSelection(next), true);
		}
	}

	private TestElement getNextFailure(TestElement selected, boolean showNext) {
		if (selected instanceof TestSuiteElement) {
			TestElement nextChild = getNextChildFailure((TestSuiteElement) selected, showNext);
			if (nextChild != null) {
				return nextChild;
			}
		}
		return getNextFailureSibling(selected, showNext);
	}

	private TestCaseElement getNextFailureSibling(TestElement current, boolean showNext) {
		TestSuiteElement parent = current.getParent();
		if (parent == null) {
			return null;
		}

		List siblings = Arrays.asList(parent.getChildren());
		if (!showNext) {
			siblings = new ReverseList(siblings);
		}

		int nextIndex = siblings.indexOf(current) + 1;
		for (int i = nextIndex; i < siblings.size(); i++) {
			TestElement sibling = (TestElement) siblings.get(i);
			if (sibling.getStatus().isError()) {
				if (sibling instanceof TestCaseElement) {
					return (TestCaseElement) sibling;
				} else {
					return getNextChildFailure((TestSuiteElement) sibling, showNext);
				}
			}
		}
		return getNextFailureSibling(parent, showNext);
	}

	private TestCaseElement getNextChildFailure(TestSuiteElement root, boolean showNext) {
		List children = Arrays.asList(root.getChildren());
		if (!showNext) {
			children = new ReverseList(children);
		}
		for (int i = 0; i < children.size(); i++) {
			TestElement child = (TestElement) children.get(i);
			if (child.getStatus().isError()) {
				if (child instanceof TestCaseElement) {
					return (TestCaseElement) child;
				} else {
					return getNextChildFailure((TestSuiteElement) child, showNext);
				}
			}
		}
		return null;
	}

	public synchronized void registerViewersRefresh() {
		fTreeNeedsRefresh = true;
		fTableNeedsRefresh = true;
		clearUpdateAndExpansion();
	}

	private void clearUpdateAndExpansion() {
		fNeedUpdate = new LinkedHashSet();
		fAutoClose = new LinkedList();
		fAutoExpand = new HashSet();
	}

	public void expandFirstLevel() {
		fTreeViewer.expandToLevel(2);
	}

}
