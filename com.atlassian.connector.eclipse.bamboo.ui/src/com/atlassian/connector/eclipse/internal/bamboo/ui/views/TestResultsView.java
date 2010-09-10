/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Julien Ruaux: jruaux@octo.com see bug 25324 Ability to know when tests are finished [junit]
 *     Vincent Massol: vmassol@octo.com 25324 Ability to know when tests are finished [junit]
 *     Sebastian Davids: sdavids@gmx.de 35762 JUnit View wasting a lot of screen space [JUnit]
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

import com.atlassian.connector.eclipse.internal.bamboo.ui.model.TestElement;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.TestDetails;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.internal.junit.ui.CounterPanel;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.ui.ProgressImages;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class TestResultsView extends ViewPart {
	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.views.testResultView"; //$NON-NLS-1$

	static final int REFRESH_INTERVAL = 200;

	static final int LAYOUT_FLAT = 0;

	static final int LAYOUT_HIERARCHICAL = 1;

	/**
	 * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code> <code>VIEW_ORIENTATION_VERTICAL</code>,
	 * or <code>VIEW_ORIENTATION_AUTOMATIC</code>.
	 */
	private int fOrientation = VIEW_ORIENTATION_AUTOMATIC;

	/**
	 * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code> <code>VIEW_ORIENTATION_VERTICAL</code>.
	 */
	private int fCurrentOrientation;

	private BuildDetails fBuildDetails;

	/**
	 * The current layout mode (LAYOUT_FLAT or LAYOUT_HIERARCHICAL).
	 */
	private int fLayout = LAYOUT_HIERARCHICAL;

//	private boolean fTestIsRunning= false;

	protected ProgressImages fProgressImages;

	protected Image fViewImage;

	protected CounterPanel fCounterPanel;

	protected boolean fShowOnErrorOnly = false;

	protected Clipboard fClipboard;

	protected volatile String fInfoMessage;

	private FailureTrace fFailureTrace;

	private TestViewer fTestViewer;

	/**
	 * Is the UI disposed?
	 */
	private boolean fIsDisposed = false;

	/**
	 * Actions
	 */
	private Action fNextAction;

	private Action fPreviousAction;

	private JUnitCopyAction fCopyAction;

	private Action fFailuresOnlyFilterAction;

	private ToggleOrientationAction[] fToggleOrientationActions;

	private ShowTestHierarchyAction fShowTestHierarchyAction;

	final Image fStackViewIcon;

	final Image fTestRunOKIcon;

	final Image fTestRunFailIcon;

	final Image fTestRunOKDirtyIcon;

	final Image fTestRunFailDirtyIcon;

	final Image fTestIcon;

	final Image fTestOkIcon;

	final Image fTestErrorIcon;

	final Image fTestFailIcon;

	final Image fTestRunningIcon;

	final Image fTestIgnoredIcon;

	final ImageDescriptor fSuiteIconDescriptor = JUnitPlugin.getImageDescriptor("obj16/tsuite.gif"); //$NON-NLS-1$

	final ImageDescriptor fSuiteOkIconDescriptor = JUnitPlugin.getImageDescriptor("obj16/tsuiteok.gif"); //$NON-NLS-1$

	final ImageDescriptor fSuiteErrorIconDescriptor = JUnitPlugin.getImageDescriptor("obj16/tsuiteerror.gif"); //$NON-NLS-1$

	final ImageDescriptor fSuiteFailIconDescriptor = JUnitPlugin.getImageDescriptor("obj16/tsuitefail.gif"); //$NON-NLS-1$

	final ImageDescriptor fSuiteRunningIconDescriptor = JUnitPlugin.getImageDescriptor("obj16/tsuiterun.gif"); //$NON-NLS-1$

	final Image fSuiteIcon;

	final Image fSuiteOkIcon;

	final Image fSuiteErrorIcon;

	final Image fSuiteFailIcon;

	final Image fSuiteRunningIcon;

	final List fImagesToDispose;

	// Persistence tags.
	static final String TAG_PAGE = "page"; //$NON-NLS-1$

	static final String TAG_RATIO = "ratio"; //$NON-NLS-1$

	static final String TAG_TRACEFILTER = "tracefilter"; //$NON-NLS-1$

	static final String TAG_ORIENTATION = "orientation"; //$NON-NLS-1$

	static final String TAG_SCROLL = "scroll"; //$NON-NLS-1$

	/**
	 * @since 3.2
	 */
	static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$

	/**
	 * @since 3.2
	 */
	static final String TAG_FAILURES_ONLY = "failuresOnly"; //$NON-NLS-1$

	/**
	 * @since 3.5
	 */
	static final String PREF_LAST_PATH = "lastImportExportPath"; //$NON-NLS-1$

	//orientations
	static final int VIEW_ORIENTATION_VERTICAL = 0;

	static final int VIEW_ORIENTATION_HORIZONTAL = 1;

	static final int VIEW_ORIENTATION_AUTOMATIC = 2;

	private IMemento fMemento;

	Image fOriginalViewImage;

//	private CTabFolder fTabFolder;
	private SashForm fSashForm;

	private Composite fCounterComposite;

	private Composite fParent;

	public static final Object FAMILY_JUNIT_RUN = new Object();

	private final IPartListener2 fPartListener = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
		}

		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		public void partInputChanged(IWorkbenchPartReference ref) {
		}

		public void partClosed(IWorkbenchPartReference ref) {
		}

		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref) {
		}

		public void partVisible(IWorkbenchPartReference ref) {
			if (getSite().getId().equals(ref.getId())) {
				fPartIsVisible = true;
			}
		}

		public void partHidden(IWorkbenchPartReference ref) {
			if (getSite().getId().equals(ref.getId())) {
				fPartIsVisible = false;
			}
		}
	};

	protected boolean fPartIsVisible = false;

	private class ToggleOrientationAction extends Action {
		private final int fActionOrientation;

		public ToggleOrientationAction(int orientation) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			if (orientation == VIEW_ORIENTATION_HORIZONTAL) {
				setText(JUnitMessages.TestRunnerViewPart_toggle_horizontal_label);
				setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/th_horizontal.gif")); //$NON-NLS-1$
			} else if (orientation == VIEW_ORIENTATION_VERTICAL) {
				setText(JUnitMessages.TestRunnerViewPart_toggle_vertical_label);
				setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/th_vertical.gif")); //$NON-NLS-1$
			} else if (orientation == VIEW_ORIENTATION_AUTOMATIC) {
				setText(JUnitMessages.TestRunnerViewPart_toggle_automatic_label);
				setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/th_automatic.gif")); //$NON-NLS-1$
			}
			fActionOrientation = orientation;
		}

		public int getOrientation() {
			return fActionOrientation;
		}

		public void run() {
			if (isChecked()) {
				fOrientation = fActionOrientation;
				computeOrientation();
			}
		}
	}

	/**
	 * Listen for for modifications to Java elements
	 */
	private class DirtyListener implements IElementChangedListener {
		public void elementChanged(ElementChangedEvent event) {
			processDelta(event.getDelta());
		}

		private boolean processDelta(IJavaElementDelta delta) {
			int kind = delta.getKind();
			int details = delta.getFlags();
			int type = delta.getElement().getElementType();

			switch (type) {
			// Consider containers for class files.
			case IJavaElement.JAVA_MODEL:
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
				// If we did something different than changing a child we flush the undo / redo stack.
				if (kind != IJavaElementDelta.CHANGED || details != IJavaElementDelta.F_CHILDREN) {
					codeHasChanged();
					return false;
				}
				break;
			case IJavaElement.COMPILATION_UNIT:
				// if we have changed a primary working copy (e.g created, removed, ...)
				// then we do nothing.
				if ((details & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {
					return true;
				}
				codeHasChanged();
				return false;

			case IJavaElement.CLASS_FILE:
				// Don't examine children of a class file but keep on examining siblings.
				return true;
			default:
				codeHasChanged();
				return false;
			}

			IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
			if (affectedChildren == null) {
				return true;
			}

			for (int i = 0; i < affectedChildren.length; i++) {
				if (!processDelta(affectedChildren[i])) {
					return false;
				}
			}
			return true;
		}
	}

	private class FailuresOnlyFilterAction extends Action {
		public FailuresOnlyFilterAction() {
			super(JUnitMessages.TestRunnerViewPart_show_failures_only, AS_CHECK_BOX);
			setToolTipText(JUnitMessages.TestRunnerViewPart_show_failures_only);
			setImageDescriptor(JUnitPlugin.getImageDescriptor("obj16/failures.gif")); //$NON-NLS-1$
		}

		public void run() {
			setShowFailuresOnly(isChecked());
		}
	}

	private class ShowTestHierarchyAction extends Action {

		public ShowTestHierarchyAction() {
			super(JUnitMessages.TestRunnerViewPart_hierarchical_layout, IAction.AS_CHECK_BOX);
			setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/hierarchicalLayout.gif")); //$NON-NLS-1$
		}

		public void run() {
			int mode = isChecked() ? LAYOUT_HIERARCHICAL : LAYOUT_FLAT;
			setLayoutMode(mode);
		}
	}

	public TestResultsView() {
		fImagesToDispose = new ArrayList();

		fStackViewIcon = createManagedImage("eview16/stackframe.gif");//$NON-NLS-1$
		fTestRunOKIcon = createManagedImage("eview16/junitsucc.gif"); //$NON-NLS-1$
		fTestRunFailIcon = createManagedImage("eview16/juniterr.gif"); //$NON-NLS-1$
		fTestRunOKDirtyIcon = createManagedImage("eview16/junitsuccq.gif"); //$NON-NLS-1$
		fTestRunFailDirtyIcon = createManagedImage("eview16/juniterrq.gif"); //$NON-NLS-1$

		fTestIcon = createManagedImage("obj16/test.gif"); //$NON-NLS-1$
		fTestOkIcon = createManagedImage("obj16/testok.gif"); //$NON-NLS-1$
		fTestErrorIcon = createManagedImage("obj16/testerr.gif"); //$NON-NLS-1$
		fTestFailIcon = createManagedImage("obj16/testfail.gif"); //$NON-NLS-1$
		fTestRunningIcon = createManagedImage("obj16/testrun.gif"); //$NON-NLS-1$
		fTestIgnoredIcon = createManagedImage("obj16/testignored.gif"); //$NON-NLS-1$

		fSuiteIcon = createManagedImage(fSuiteIconDescriptor);
		fSuiteOkIcon = createManagedImage(fSuiteOkIconDescriptor);
		fSuiteErrorIcon = createManagedImage(fSuiteErrorIconDescriptor);
		fSuiteFailIcon = createManagedImage(fSuiteFailIconDescriptor);
		fSuiteRunningIcon = createManagedImage(fSuiteRunningIconDescriptor);
	}

	private Image createManagedImage(String path) {
		return createManagedImage(JUnitPlugin.getImageDescriptor(path));
	}

	private Image createManagedImage(ImageDescriptor descriptor) {
		Image image = descriptor.createImage();
		if (image == null) {
			image = ImageDescriptor.getMissingImageDescriptor().createImage();
		}
		fImagesToDispose.add(image);
		return image;
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento = memento;
		IWorkbenchSiteProgressService progressService = getProgressService();
		if (progressService != null) {
			progressService.showBusyForFamily(TestRunnerViewPart.FAMILY_JUNIT_RUN);
		}
	}

	private IWorkbenchSiteProgressService getProgressService() {
		Object siteService = getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if (siteService != null) {
			return (IWorkbenchSiteProgressService) siteService;
		}
		return null;
	}

	public void saveState(IMemento memento) {
		if (fSashForm == null) {
			// part has not been created
			if (fMemento != null) {
				memento.putMemento(fMemento);
			}
			return;
		}

//		int activePage= fTabFolder.getSelectionIndex();
//		memento.putInteger(TAG_PAGE, activePage);
		int weigths[] = fSashForm.getWeights();
		int ratio = (weigths[0] * 1000) / (weigths[0] + weigths[1]);
		memento.putInteger(TAG_RATIO, ratio);
		memento.putInteger(TAG_ORIENTATION, fOrientation);

		memento.putString(TAG_FAILURES_ONLY, fFailuresOnlyFilterAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putInteger(TAG_LAYOUT, fLayout);
	}

	private void restoreLayoutState(IMemento memento) {
//		Integer page= memento.getInteger(TAG_PAGE);
//		if (page != null) {
//			int p= page.intValue();
//			if (p < fTestRunTabs.size()) { // tab count can decrease if a contributing plug-in is removed
//				fTabFolder.setSelection(p);
//				fActiveRunTab= (TestRunTab)fTestRunTabs.get(p);
//			}
//		}
		Integer ratio = memento.getInteger(TAG_RATIO);
		if (ratio != null) {
			fSashForm.setWeights(new int[] { ratio.intValue(), 1000 - ratio.intValue() });
		}
		Integer orientation = memento.getInteger(TAG_ORIENTATION);
		if (orientation != null) {
			fOrientation = orientation.intValue();
		}
		computeOrientation();

		Integer layout = memento.getInteger(TAG_LAYOUT);
		int layoutValue = LAYOUT_HIERARCHICAL;
		if (layout != null) {
			layoutValue = layout.intValue();
		}

		String failuresOnly = memento.getString(TAG_FAILURES_ONLY);
		boolean showFailuresOnly = false;
		if (failuresOnly != null) {
			showFailuresOnly = failuresOnly.equals("true"); //$NON-NLS-1$
		}

		setFilterAndLayout(showFailuresOnly, layoutValue);
	}

	private void processChangesInUI() {
		if (fSashForm.isDisposed()) {
			return;
		}

		doShowInfoMessage();
		refreshCounters();

		if (!fPartIsVisible) {
			updateViewTitleProgress();
		} else {
			updateViewIcon();
		}
		boolean hasErrorsOrFailures = hasErrorsOrFailures();
		fNextAction.setEnabled(hasErrorsOrFailures);
		fPreviousAction.setEnabled(hasErrorsOrFailures);

		fTestViewer.processChangesInUI();
	}

	public void selectNextFailure() {
		fTestViewer.selectFailure(true);
	}

	public void selectPreviousFailure() {
		fTestViewer.selectFailure(false);
	}

	protected void selectFirstFailure() {
		fTestViewer.selectFirstFailure();
	}

	private boolean hasErrorsOrFailures() {
		return getErrorsPlusFailures() > 0;
	}

	private int getErrorsPlusFailures() {
		if (fBuildDetails == null) {
			return 0;
		} else {
			return fBuildDetails.getFailedTestDetails().size();
		}
	}

	private String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000);
	}

	private void handleStopped() {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) {
					return;
				}
				resetViewIcon();
			}
		});
	}

	private void resetViewIcon() {
		fViewImage = fOriginalViewImage;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	private void updateViewIcon() {
		if (fBuildDetails == null) {
			fViewImage = fOriginalViewImage;
		} else if (hasErrorsOrFailures()) {
			fViewImage = fTestRunFailIcon;
		} else {
			fViewImage = fTestRunOKIcon;
		}
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	private void updateViewTitleProgress() {
		if (fBuildDetails != null) {
			updateViewIcon();
		} else {
			resetViewIcon();
		}
	}

	public synchronized void dispose() {
		fIsDisposed = true;

		if (fProgressImages != null) {
			fProgressImages.dispose();
		}
		getViewSite().getPage().removePartListener(fPartListener);

		disposeImages();
		if (fClipboard != null) {
			fClipboard.dispose();
		}
	}

	private void disposeImages() {
		for (int i = 0; i < fImagesToDispose.size(); i++) {
			((Image) fImagesToDispose.get(i)).dispose();
		}
	}

	private void postSyncRunnable(Runnable r) {
		if (!isDisposed()) {
			getDisplay().syncExec(r);
		}
	}

	private void refreshCounters() {
		// TODO: Inefficient. Either
		// - keep a boolean fHasTestRun and update only on changes, or
		// - improve components to only redraw on changes (once!).

		int totalCount;
		int errorCount;

		if (fBuildDetails != null) {
			totalCount = fBuildDetails.getSuccessfulTestDetails().size() + fBuildDetails.getFailedTestDetails().size();
			errorCount = fBuildDetails.getFailedTestDetails().size();
		} else {
			totalCount = 0;
			errorCount = 0;
		}

		fCounterPanel.setTotal(totalCount);
		fCounterPanel.setErrorValue(errorCount);
	}

	protected void postShowTestResultsView() {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) {
					return;
				}
				showTestResultsView();
			}
		});
	}

	public void showTestResultsView() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		TestResultsView testRunner = null;

		if (page != null) {
			try { // show the result view
				testRunner = (TestResultsView) page.findView(TestResultsView.ID);
				if (testRunner == null) {
					IWorkbenchPart activePart = page.getActivePart();
					testRunner = (TestResultsView) page.showView(TestResultsView.ID);
					//restore focus
					page.activate(activePart);
				} else {
					page.bringToTop(testRunner);
				}
			} catch (PartInitException pie) {
				JUnitPlugin.log(pie);
			}
		}
	}

	protected void doShowInfoMessage() {
		if (fInfoMessage != null) {
			setContentDescription(fInfoMessage);
			fInfoMessage = null;
		}
	}

	protected void registerInfoMessage(String message) {
		fInfoMessage = message;
	}

	private SashForm createSashForm(Composite parent) {
		fSashForm = new SashForm(parent, SWT.VERTICAL);

		ViewForm top = new ViewForm(fSashForm, SWT.NONE);

		Composite empty = new Composite(top, SWT.NONE);
		empty.setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(1, 1); // (0, 0) does not work with super-intelligent ViewForm
			}

			protected void layout(Composite composite, boolean flushCache) {
			}
		});
		top.setTopLeft(empty); // makes ViewForm draw the horizontal separator line ...
		fTestViewer = new TestViewer(top, fClipboard, this);
		top.setContent(fTestViewer.getTestViewerControl());

		ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);

		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText(JUnitMessages.TestRunnerViewPart_label_failure);
		label.setImage(fStackViewIcon);
		bottom.setTopLeft(label);
		ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		bottom.setTopCenter(failureToolBar);
		fFailureTrace = new FailureTrace(bottom, fClipboard, this, failureToolBar);
		bottom.setContent(fFailureTrace.getComposite());

		fSashForm.setWeights(new int[] { 50, 50 });
		return fSashForm;
	}

	private void clearStatus() {
		getStatusLine().setMessage(null);
		getStatusLine().setErrorMessage(null);
	}

	public void setFocus() {
		if (fTestViewer != null) {
			fTestViewer.getTestViewerControl().setFocus();
		}
	}

	public void createPartControl(Composite parent) {
		fParent = parent;
		addResizeListener(parent);
		fClipboard = new Clipboard(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);

		configureToolBar();

		fCounterComposite = createProgressCountPanel(parent);
		fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		IActionBars actionBars = getViewSite().getActionBars();
		fCopyAction = new JUnitCopyAction(fFailureTrace, fClipboard);
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);

		fOriginalViewImage = getTitleImage();
		fProgressImages = new ProgressImages();

		getViewSite().getPage().addPartListener(fPartListener);

		setFilterAndLayout(false, LAYOUT_HIERARCHICAL);
		fTestViewer.setShowTime(true);
		if (fMemento != null) {
			restoreLayoutState(fMemento);
		}
		fMemento = null;
	}

	private void addResizeListener(Composite parent) {
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});
	}

	void computeOrientation() {
		if (fOrientation != VIEW_ORIENTATION_AUTOMATIC) {
			fCurrentOrientation = fOrientation;
			setOrientation(fCurrentOrientation);
		} else {
			Point size = fParent.getSize();
			if (size.x != 0 && size.y != 0) {
				if (size.x > size.y) {
					setOrientation(VIEW_ORIENTATION_HORIZONTAL);
				} else {
					setOrientation(VIEW_ORIENTATION_VERTICAL);
				}
			}
		}
	}

	private void configureToolBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		IMenuManager viewMenu = actionBars.getMenuManager();

		fNextAction = new ShowNextFailureAction(this);
		fNextAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAction);

		fPreviousAction = new ShowPreviousFailureAction(this);
		fPreviousAction.setEnabled(false);
		actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAction);

		fFailuresOnlyFilterAction = new FailuresOnlyFilterAction();

		fToggleOrientationActions = new ToggleOrientationAction[] {
				new ToggleOrientationAction(VIEW_ORIENTATION_VERTICAL),
				new ToggleOrientationAction(VIEW_ORIENTATION_HORIZONTAL),
				new ToggleOrientationAction(VIEW_ORIENTATION_AUTOMATIC) };

		fShowTestHierarchyAction = new ShowTestHierarchyAction();

		toolBar.add(fNextAction);
		toolBar.add(fPreviousAction);
		toolBar.add(fFailuresOnlyFilterAction);
		toolBar.add(new Separator());

		viewMenu.add(fShowTestHierarchyAction);
		viewMenu.add(new Separator());

		MenuManager layoutSubMenu = new MenuManager(JUnitMessages.TestRunnerViewPart_layout_menu);
		for (int i = 0; i < fToggleOrientationActions.length; ++i) {
			layoutSubMenu.add(fToggleOrientationActions[i]);
		}
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());

		viewMenu.add(fFailuresOnlyFilterAction);

		actionBars.updateActionBars();
	}

	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go through the active part
		IViewSite site = getViewSite();
		IWorkbenchPage page = site.getPage();
		IWorkbenchPart activePart = page.getActivePart();

		if (activePart instanceof IViewPart) {
			IViewPart activeViewPart = (IViewPart) activePart;
			IViewSite activeViewSite = activeViewPart.getViewSite();
			return activeViewSite.getActionBars().getStatusLineManager();
		}

		if (activePart instanceof IEditorPart) {
			IEditorPart activeEditorPart = (IEditorPart) activePart;
			IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
			if (contributor instanceof EditorActionBarContributor) {
				return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
			}
		}
		// no active part
		return getViewSite().getActionBars().getStatusLineManager();
	}

	protected Composite createProgressCountPanel(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		setCounterColumns(layout);

		fCounterPanel = new CounterPanel(composite);
		fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	public void handleTestSelected(TestElement test) {
		showFailure(test);
		fCopyAction.handleTestSelected(test);
	}

	private void showFailure(final TestElement test) {
		postSyncRunnable(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					fFailureTrace.showFailure(test);
				}
			}
		});
	}

	private boolean isDisposed() {
		return fIsDisposed || fCounterPanel.isDisposed();
	}

	private Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}

	/*
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		if (fOriginalViewImage == null) {
			fOriginalViewImage = super.getTitleImage();
		}

		if (fViewImage == null) {
			return super.getTitleImage();
		}
		return fViewImage;
	}

	void codeHasChanged() {
		if (fViewImage == fTestRunOKIcon) {
			fViewImage = fTestRunOKDirtyIcon;
		} else if (fViewImage == fTestRunFailIcon) {
			fViewImage = fTestRunFailDirtyIcon;
		}

		Runnable r = new Runnable() {
			public void run() {
				if (isDisposed()) {
					return;
				}
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
			}
		};
		if (!isDisposed()) {
			getDisplay().asyncExec(r);
		}
	}

	public boolean isCreated() {
		return fCounterPanel != null;
	}

	private void setOrientation(int orientation) {
		if ((fSashForm == null) || fSashForm.isDisposed()) {
			return;
		}
		boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
		fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
		for (int i = 0; i < fToggleOrientationActions.length; ++i) {
			fToggleOrientationActions[i].setChecked(fOrientation == fToggleOrientationActions[i].getOrientation());
		}
		fCurrentOrientation = orientation;
		GridLayout layout = (GridLayout) fCounterComposite.getLayout();
		setCounterColumns(layout);
		fParent.layout();
	}

	private void setCounterColumns(GridLayout layout) {
		if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL) {
			layout.numColumns = 2;
		} else {
			layout.numColumns = 1;
		}
	}

	public FailureTrace getFailureTrace() {
		return fFailureTrace;
	}

	void setShowFailuresOnly(boolean failuresOnly) {
		setFilterAndLayout(failuresOnly, fLayout);
	}

	private void setLayoutMode(int mode) {
		setFilterAndLayout(fFailuresOnlyFilterAction.isChecked(), mode);
	}

	private void setFilterAndLayout(boolean failuresOnly, int layoutMode) {
		fShowTestHierarchyAction.setChecked(layoutMode == LAYOUT_HIERARCHICAL);
		fLayout = layoutMode;
		fFailuresOnlyFilterAction.setChecked(failuresOnly);
		fTestViewer.setShowFailuresOnly(failuresOnly, layoutMode);
	}

	List<TestDetails> getAllFailures() {
		return fBuildDetails.getFailedTestDetails();
	}

	public void setTestsResult(String buildKey, BuildDetails testResults) {
		if (testResults != null) {
			setContentDescription(NLS.bind("Bamboo build {0}", buildKey));

			clearStatus();
			fFailureTrace.clear();

			fTestViewer.setBuildDetails(buildKey, testResults);
			fTestViewer.expandFirstLevel();
		} else {
			setContentDescription("");
			setTitleToolTip(null);
			resetViewIcon();
			clearStatus();
			fFailureTrace.clear();

			registerInfoMessage(" "); //$NON-NLS-1$
		}

		//String[] keys = { elapsedTimeAsString(elapsedTime) };
		//String msg = Messages.format(JUnitMessages.TestRunnerViewPart_message_finish, keys);
		//registerInfoMessage(msg);

		postSyncRunnable(new Runnable() {
			public void run() {
				if (isDisposed()) {
					return;
				}
				processChangesInUI();
				if (hasErrorsOrFailures()) {
					selectFirstFailure();
				}
			}
		});
	}
}
