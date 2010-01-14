package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ResourceSelectionPage.ResourceStatus;
import com.atlassian.connector.eclipse.ui.AtlassianImages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResourceSelectionTree extends Composite {
	private static final int TREE_WIDTH = 500;

	private Tree tree;

	private TreeViewMode mode;

	private List<IResource> resources;

	private List<IContainer> compressedFolders = null;

	private IContainer[] folders;

	private IContainer[] rootFolders;

	private CheckboxTreeViewer treeViewer;

	private final String label;

	private Action treeAction;

	private Action flatAction;

	private Action compressedAction;

	private final ResourceComparator comparator = new ResourceComparator();

	private final IToolbarControlCreator toolbarControlCreator;

	private final ResourceSelectionContentProvider resourceSelectionContentProvider = new ResourceSelectionContentProvider();

	public static enum TreeViewMode {
		MODE_COMPRESSED_FOLDERS, MODE_FLAT, MODE_TREE;
	}

	private final static int SPACEBAR = 32;

	private final ITreeViewModeSettingProvider settingsProvider;

	private Map<IResource, ResourceStatus> resourcesToShow;

	/**
	 * 
	 * @param parent
	 *            parent composite for this tree
	 * @param label
	 *            label in the toolbar
	 * @param resourcesToShow
	 *            list of resources to show
	 * @param toolbarControlCreator
	 *            toolbar actions creator if want to add additional actions (can be null)
	 * @param settingsProvider
	 *            settings provider to store/restore resources tree mode (can be null)
	 */
	public ResourceSelectionTree(Composite parent, String label,
			@NotNull Map<IResource, ResourceStatus> resourcesToShow, IToolbarControlCreator toolbarControlCreator,
			ITreeViewModeSettingProvider settingsProvider) {
		super(parent, SWT.NONE);
		this.label = label;
		this.resourcesToShow = resourcesToShow;
		this.settingsProvider = settingsProvider;
		this.resources = new ArrayList<IResource>(resourcesToShow.keySet());
		this.toolbarControlCreator = toolbarControlCreator;

		if (resources != null) {
			Collections.sort(resources, comparator);
		}

		if (settingsProvider == null) {
			settingsProvider = new ITreeViewModeSettingProvider() {

				public void setTreeViewMode(TreeViewMode mode) {
				}

				public TreeViewMode getTreeViewMode() {
					return TreeViewMode.MODE_COMPRESSED_FOLDERS;
				}
			};
		}

		mode = settingsProvider.getTreeViewMode();

		createControls();
	}

	public CheckboxTreeViewer getTreeViewer() {
		return treeViewer;
	}

	public IResource[] getSelectedResources() {
		ArrayList<IResource> selected = new ArrayList<IResource>();
		Object[] checkedResources = treeViewer.getCheckedElements();
		for (Object checkedResource : checkedResources) {
			if (resources.contains(checkedResource)) {
				selected.add((IResource) checkedResource);
			}
		}
		IResource[] selectedResources = new IResource[selected.size()];
		selected.toArray(selectedResources);
		return selectedResources;
	}

	private void createControls() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = 500;
		setLayoutData(layoutData);

		ViewForm viewerPane = new ViewForm(this, SWT.BORDER | SWT.FLAT);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.widthHint = 500;
		viewerPane.setLayoutData(layoutData);

		CLabel toolbarLabel = new CLabel(viewerPane, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return super.computeSize(wHint, Math.max(24, hHint), changed);
			}
		};

		if (label != null) {
			toolbarLabel.setText(label);
		}
		viewerPane.setTopLeft(toolbarLabel);

		int buttonGroupColumns = 1;
		if (toolbarControlCreator != null) {
			buttonGroupColumns = buttonGroupColumns + toolbarControlCreator.getControlCount();
		}

		ToolBar toolbar = new ToolBar(viewerPane, SWT.FLAT);
		viewerPane.setTopCenter(toolbar);

		ToolBarManager toolbarManager = new ToolBarManager(toolbar);

		if (toolbarControlCreator != null) {
			toolbarControlCreator.createToolbarControls(toolbarManager);
			toolbarManager.add(new Separator());
		}

		flatAction = new Action("Flat Mode", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = TreeViewMode.MODE_FLAT;
				settingsProvider.setTreeViewMode(mode);
				treeAction.setChecked(false);
				compressedAction.setChecked(false);
				refresh();
			}
		};
		flatAction.setImageDescriptor(AtlassianImages.IMG_FLAT_MODE);
		toolbarManager.add(flatAction);

		treeAction = new Action("Tree Mode", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = TreeViewMode.MODE_TREE;
				settingsProvider.setTreeViewMode(mode);
				flatAction.setChecked(false);
				compressedAction.setChecked(false);
				refresh();
			}
		};
		treeAction.setImageDescriptor(AtlassianImages.IMG_TREE_MODE);
		toolbarManager.add(treeAction);

		compressedAction = new Action("Compressed Folders Mode", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = TreeViewMode.MODE_COMPRESSED_FOLDERS;
				settingsProvider.setTreeViewMode(mode);
				treeAction.setChecked(false);
				flatAction.setChecked(false);
				refresh();
			}
		};
		compressedAction.setImageDescriptor(AtlassianImages.IMG_COMPRESSED_FOLDER_MODE);
		toolbarManager.add(compressedAction);

		toolbarManager.update(true);

		switch (mode) {
		case MODE_COMPRESSED_FOLDERS:
			compressedAction.setChecked(true);
			break;
		case MODE_FLAT:
			flatAction.setChecked(true);
			break;
		case MODE_TREE:
			treeAction.setChecked(true);
			break;
		default:
			compressedAction.setChecked(true);
			mode = TreeViewMode.MODE_COMPRESSED_FOLDERS;
			settingsProvider.setTreeViewMode(mode);
			break;
		}

		treeViewer = new CheckboxTreeViewer(viewerPane, SWT.MULTI);

		// Override the spacebar behavior to toggle checked state for all selected items.
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SPACEBAR) {
					Tree tree = (Tree) treeViewer.getControl();
					TreeItem[] items = tree.getSelection();
					for (int i = 0; i < items.length; i++) {
						if (i > 0) {
							items[i].setChecked(!items[i].getChecked());
						}
					}
				}
			}
		});
		tree = treeViewer.getTree();
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 500;
		tree.setLayoutData(layoutData);
		viewerPane.setContent(tree);

		final DelegatingStyledCellLabelProvider labelProvider = new DelegatingStyledCellLabelProvider(
				new ResourceSelectionLabelProvider());

		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setContentProvider(resourceSelectionContentProvider);
		treeViewer.setUseHashlookup(true);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 125;
		layoutData.widthHint = TREE_WIDTH;
		treeViewer.getControl().setLayoutData(layoutData);
		treeViewer.setInput(this);

		treeViewer.expandAll();

		setAllChecked(true);
		if (mode == TreeViewMode.MODE_TREE) {
			treeViewer.collapseAll();
		}
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTreeMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		treeViewer.getTree().setMenu(menu);
	}

	void setAllChecked(boolean state) {
		for (IResource resource : resourcesToShow.keySet()) {
			treeViewer.setChecked(resource, state);
			handleCheckStateChange(resource, state);
		}
	}

	protected void fillTreeMenu(IMenuManager menuMgr) {
		Action selectAllAction = new Action("Select All") { //$NON-NLS-1$
			public void run() {
				setAllChecked(true);
			}
		};
		menuMgr.add(selectAllAction);

		Action deselectAllAction = new Action("Deselect All") { //$NON-NLS-1$
			public void run() {
				setAllChecked(false);
			}
		};
		menuMgr.add(deselectAllAction);

		menuMgr.add(new Separator());

		Action deselectPreCommit = new Action("Deselect Pre-Commit") { //$NON-NLS-1$
			public void run() {
				for (Object obj : getSelectedResources()) {
					if (!resourcesToShow.get(obj).isUpToDate()) {
						treeViewer.setChecked(obj, false);
						handleCheckStateChange(obj, false);
					}
				}

			}
		};
		menuMgr.add(deselectPreCommit);

		Action deselectPostCommit = new Action("Deselect Post-Commit") { //$NON-NLS-1$
			public void run() {
				for (Object obj : getSelectedResources()) {
					if (resourcesToShow.get(obj).isUpToDate()) {
						treeViewer.setChecked(obj, false);
						handleCheckStateChange(obj, false);
					}
				}
			}
		};
		menuMgr.add(deselectPostCommit);

		menuMgr.add(new Separator());

		if (mode != TreeViewMode.MODE_FLAT) {
			Action expandAllAction = new Action("Expand All") { //$NON-NLS-1$
				public void run() {
					treeViewer.expandAll();
				}
			};
			menuMgr.add(expandAllAction);
		}
	}

	private void handleCheckStateChange(Object element, boolean checked) {
		treeViewer.setGrayed(element, false);
		treeViewer.setSubtreeChecked(element, checked);
		IResource resource = (IResource) element;
		updateParentState(resource, checked);
	}

	private void handleCheckStateChange(CheckStateChangedEvent event) {
		handleCheckStateChange(event.getElement(), event.getChecked());
	}

	private void updateParentState(IResource child, boolean baseChildState) {
		if (mode == TreeViewMode.MODE_FLAT || child == null || child.getParent() == null
				|| resources.contains(child.getParent())) {
			return;
		}
		if (child == null) {
			return;
		}
		Object parent = resourceSelectionContentProvider.getParent(child);
		if (parent == null) {
			return;
		}
		boolean allSameState = true;
		Object[] children = null;
		children = resourceSelectionContentProvider.getChildren(parent);
		for (int i = children.length - 1; i >= 0; i--) {
			if (treeViewer.getChecked(children[i]) != baseChildState || treeViewer.getGrayed(children[i])) {
				allSameState = false;
				break;
			}
		}
		treeViewer.setGrayed(parent, !allSameState);
		treeViewer.setChecked(parent, !allSameState || baseChildState);
		updateParentState((IResource) parent, baseChildState);
	}

	public void refresh() {
		Object[] checkedElements = null;
		checkedElements = treeViewer.getCheckedElements();
		treeViewer.refresh();
		treeViewer.expandAll();
		treeViewer.setCheckedElements(checkedElements);
		for (Object checkedElement : checkedElements) {
			updateParentState((IResource) checkedElement, true);
		}
		if (mode == TreeViewMode.MODE_TREE) {
			treeViewer.collapseAll();
		}
	}

	private IContainer[] getRootFolders() {
		if (rootFolders == null) {
			getFolders();
		}
		return rootFolders;
	}

	private IContainer[] getCompressedFolders() {
		if (compressedFolders == null) {
			compressedFolders = new ArrayList<IContainer>();
			for (IResource res : resources) {
				if (res instanceof IContainer && !compressedFolders.contains(res)) {
					compressedFolders.add((IContainer) res);
				}
				if (!(res instanceof IContainer)) {
					IContainer parent = res.getParent();
					if (parent != null && !(parent instanceof IWorkspaceRoot) && !compressedFolders.contains(parent)) {
						compressedFolders.add(parent);
					}
				}
			}
			Collections.sort(compressedFolders, comparator);
		}
		return compressedFolders.toArray(new IContainer[compressedFolders.size()]);
	}

	private IResource[] getChildResources(IContainer parent) {
		ArrayList<IResource> children = new ArrayList<IResource>();
		for (IResource res : resources) {
			if (!(res instanceof IContainer)) {
				IContainer parentFolder = res.getParent();
				if (parentFolder != null && parentFolder.equals(parent) && !children.contains(parentFolder)) {
					children.add(res);
				}
			}
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}

	private IResource[] getFolderChildren(IContainer parent) {
		ArrayList<IResource> children = new ArrayList<IResource>();
		folders = getFolders();
		for (IContainer folder : folders) {
			if (folder.getParent() != null && folder.getParent().equals(parent)) {
				children.add(folder);
			}
		}
		for (IResource res : resources) {
			if (!(res instanceof IContainer) && res.getParent() != null && res.getParent().equals(parent)) {
				children.add(res);
			}
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}

	private IContainer[] getFolders() {
		List<IContainer> rootList = new ArrayList<IContainer>();

		if (folders == null) {
			ArrayList<IContainer> folderList = new ArrayList<IContainer>();
			for (IResource resource : resources) {
				if (resource instanceof IContainer) {
					folderList.add((IContainer) resource);
				}
				IResource parent = resource;
				while (parent != null && !(parent instanceof IWorkspaceRoot)) {
					if (!(parent.getParent() instanceof IWorkspaceRoot) && folderList.contains(parent.getParent())) {
						break;
					}
					if (parent.getParent() == null || parent.getParent() instanceof IWorkspaceRoot) {
						rootList.add((IContainer) parent);
					}
					parent = parent.getParent();
					folderList.add((IContainer) parent);
				}
			}
			folders = new IContainer[folderList.size()];
			folderList.toArray(folders);
			Arrays.sort(folders, comparator);
			rootFolders = new IContainer[rootList.size()];
			rootList.toArray(rootFolders);
			Arrays.sort(rootFolders, comparator);
		}
		return folders;
	}

	private class ResourceSelectionContentProvider extends WorkbenchContentProvider {
		public Object getParent(Object element) {
			return ((IResource) element).getParent();
		}

		public boolean hasChildren(Object element) {
			if (mode != TreeViewMode.MODE_FLAT && element instanceof IContainer) {
				return true;
			} else {
				return false;
			}
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ResourceSelectionTree) {
				if (mode == TreeViewMode.MODE_FLAT) {
					return resources.toArray(new IResource[resources.size()]);
				} else if (mode == TreeViewMode.MODE_COMPRESSED_FOLDERS) {
					return getCompressedFolders();
				} else {
					return getRootFolders();
				}
			}
			if (parentElement instanceof IContainer) {
				if (mode == TreeViewMode.MODE_COMPRESSED_FOLDERS) {
					return getChildResources((IContainer) parentElement);
				}
				if (mode == TreeViewMode.MODE_TREE) {
					return getFolderChildren((IContainer) parentElement);
				}
			}
			return new Object[0];
		}
	}

	private class ResourceSelectionLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
		private final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

		private final static String colorRed = "com.atlassian.connector.eclipse.internal.red";

		public ResourceSelectionLabelProvider() {
			super();
			JFaceResources.getColorRegistry().put(colorRed, new RGB(150, 20, 20));
		}

		public Image getImage(Object element) {
			return workbenchLabelProvider.getImage(element);
		}

		@NotNull
		private String getTextForResource(IResource resource) {
			String text = null;
			if (mode == TreeViewMode.MODE_FLAT) {
				text = resource.getFullPath().toString();
			} else if (mode == TreeViewMode.MODE_COMPRESSED_FOLDERS) {
				if (resource instanceof IContainer) {
					IContainer container = (IContainer) resource;
					text = container.getFullPath().makeRelative().toString();
				} else {
					text = resource.getName();
				}
			} else {
				text = resource.getName();
			}
			return text == null ? "" : text;
		}

		public StyledString getStyledText(Object element) {
			StyledString styledString = new StyledString();

			IResource resource = (IResource) element;

			styledString.append(getTextForResource(resource));

			if (resourcesToShow.containsKey(resource)) {
				if (!resourcesToShow.get(resource).isUpToDate()) {
					styledString.append(" ");
					styledString.append(resourcesToShow.get(resource).getState(),
							StyledString.createColorRegistryStyler(colorRed, null));
				}
			}

			return styledString;
		}
	}

	private class ResourceComparator implements Comparator<IResource> {
		public int compare(IResource obj0, IResource obj1) {
			return obj0.getFullPath().toOSString().compareTo(obj1.getFullPath().toOSString());
		}
	}

	public static interface IToolbarControlCreator {
		void createToolbarControls(ToolBarManager toolbarManager);

		public int getControlCount();
	}

	public static interface ITreeViewModeSettingProvider {
		TreeViewMode getTreeViewMode();

		void setTreeViewMode(TreeViewMode mode);
	}

	public void setResources(@NotNull Map<IResource, ResourceStatus> resourcesToShow) {
		resources = new ArrayList<IResource>(resourcesToShow.keySet());
		this.resourcesToShow = resourcesToShow;

		compressedFolders = null;
		folders = null;
		rootFolders = null;

		if (resources != null) {
			Collections.sort(resources, comparator);
		}
	}
}
