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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
	private Tree tree;

	private int mode;

	private List<IResource> resources;

	private List<IContainer> compressedFolders = null;

	private IContainer[] folders;

	private IContainer[] rootFolders;

	private CheckboxTreeViewer treeViewer;

	private LabelProvider labelProvider;

	private final String label;

//	private Button selectAllButton;
//	private Button deselectAllButton;
	private Action treeAction;

	private Action flatAction;

	private Action compressedAction;

	private final ResourceComparator comparator = new ResourceComparator();

	private final IToolbarControlCreator toolbarControlCreator;

//	private final ResourceSelectionTreeDecorator resourceSelectionTreeDecorator = new ResourceSelectionTreeDecorator();

	private final ResourceSelectionContentProvider resourceSelectionContentProvider = new ResourceSelectionContentProvider();

	public final static String MODE_SETTING = "ResourceSelectionTree.mode"; //$NON-NLS-1$

	public final static int MODE_COMPRESSED_FOLDERS = 0;

	public final static int MODE_FLAT = 1;

	public final static int MODE_TREE = 2;

	private final static int SPACEBAR = 32;

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
	 */
	public ResourceSelectionTree(Composite parent, String label,
			@NotNull Map<IResource, ResourceStatus> resourcesToShow, IToolbarControlCreator toolbarControlCreator) {
		super(parent, SWT.NONE);
		this.label = label;
		this.resources = new ArrayList<IResource>(resourcesToShow.keySet());
		this.toolbarControlCreator = toolbarControlCreator;
		// TODO jj restore tree type setting
//		this.settings = SVNUIPlugin.getPlugin().getDialogSettings();
		if (resourcesToShow != null) {
			Collections.sort(resources, comparator);
		}
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
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));

		ViewForm viewerPane = new ViewForm(this, SWT.BORDER | SWT.FLAT);
		viewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

//		Composite treeGroup = new Composite(viewerPane, SWT.NONE);
//		
//		GridLayout treeLayout = new GridLayout();
//		treeLayout.marginWidth = 0;
//		treeLayout.verticalSpacing = 1;
//		treeLayout.horizontalSpacing = 0;
//		treeLayout.numColumns = 1;
//		treeLayout.marginHeight = 0;
//		treeGroup.setLayout(treeLayout);
//		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
//		treeGroup.setLayoutData(gridData);	

//		Composite toolbarGroup = new Composite(treeGroup, SWT.NONE);
//		GridLayout toolbarGroupLayout = new GridLayout();
//		toolbarGroupLayout.numColumns = 2;
//		toolbarGroupLayout.marginWidth = 0;
//		toolbarGroupLayout.marginHeight = 0;
//		toolbarGroup.setLayout(toolbarGroupLayout);
//		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
//		toolbarGroup.setLayoutData(gridData);	

		CLabel toolbarLabel = new CLabel(viewerPane, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return super.computeSize(wHint, Math.max(24, hHint), changed);
			}
		};

//		Label toolbarLabel = new Label(viewerPane, SWT.NONE);
//		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
//		gridData.horizontalIndent = 3;
//		gridData.horizontalAlignment = SWT.BEGINNING;
//		gridData.verticalAlignment = SWT.CENTER;
//		toolbarLabel.setLayoutData(gridData);
		if (label != null) {
			toolbarLabel.setText(label);
		}
		viewerPane.setTopLeft(toolbarLabel);

		int buttonGroupColumns = 1;
		if (toolbarControlCreator != null) {
			buttonGroupColumns = buttonGroupColumns + toolbarControlCreator.getControlCount();
		}

//		Composite buttonGroup = new Composite(toolbarGroup, SWT.NONE);
//		GridLayout buttonLayout = new GridLayout();
//		buttonLayout.numColumns = buttonGroupColumns;
//		buttonLayout.marginHeight = 0;
//		buttonLayout.marginWidth = 0;
//		buttonGroup.setLayout(buttonLayout);
//		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
//		buttonGroup.setLayoutData(gridData);

		ToolBar toolbar = new ToolBar(viewerPane, SWT.FLAT);
//		GridLayout toolbarLayout = new GridLayout();
//		toolbarLayout.numColumns = 3;
//		toolbar.setLayout(toolbarLayout);
//		toolbar.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewerPane.setTopCenter(toolbar);

		ToolBarManager toolbarManager = new ToolBarManager(toolbar);

		if (toolbarControlCreator != null) {
			toolbarControlCreator.createToolbarControls(toolbarManager);
			toolbarManager.add(new Separator());
		}

		flatAction = new Action("ResourceSelectionTree.flat", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = MODE_FLAT;
//				settings.put(MODE_SETTING, MODE_FLAT);
				treeAction.setChecked(false);
				compressedAction.setChecked(false);
				refresh();
			}
		};
		flatAction.setImageDescriptor(AtlassianImages.IMG_FLAT_MODE);
		toolbarManager.add(flatAction);
		treeAction = new Action("ResourceSelectionTree.tree", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = MODE_TREE;
//				settings.put(MODE_SETTING, MODE_TREE);
				flatAction.setChecked(false);
				compressedAction.setChecked(false);
				refresh();
			}
		};
		treeAction.setImageDescriptor(AtlassianImages.IMG_TREE_MODE);
		toolbarManager.add(treeAction);

		compressedAction = new Action("ResourceSelectionTree.compressedFolders", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				mode = MODE_COMPRESSED_FOLDERS;
//				settings.put(MODE_SETTING, MODE_COMPRESSED_FOLDERS);
				treeAction.setChecked(false);
				flatAction.setChecked(false);
				refresh();
			}
		};
		compressedAction.setImageDescriptor(AtlassianImages.IMG_COMPRESSED_FOLDER_MODE);
		toolbarManager.add(compressedAction);

		toolbarManager.update(true);

		mode = MODE_COMPRESSED_FOLDERS;
		try {
//			mode = settings.getInt(MODE_SETTING);
		} catch (Exception e) {
		}
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
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerPane.setContent(tree);

		labelProvider = new ResourceSelectionLabelProvider();

		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setContentProvider(resourceSelectionContentProvider);
		treeViewer.setUseHashlookup(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 125;
		treeViewer.getControl().setLayoutData(gd);
		treeViewer.setInput(this);

//		  SelectionListener selectionListener = new SelectionAdapter() {
//		    public void widgetSelected(SelectionEvent e) {
//		      setAllChecked(e.getSource() == selectAllButton);
//		    }			
//		  };
//
//		  deselectAllButton = new Button(this, SWT.PUSH);
//  	  deselectAllButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
//  	  deselectAllButton.setText(Policy.bind("ResourceSelectionTree.DeselectAll")); //$NON-NLS-1$
//  	  deselectAllButton.addSelectionListener(selectionListener);
//  	
//  	  selectAllButton = new Button(this, SWT.PUSH);
//  	  selectAllButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
//  	  selectAllButton.setText(Policy.bind("ResourceSelectionTree.SelectAll")); //$NON-NLS-1$
//  	  selectAllButton.addSelectionListener(selectionListener);

		treeViewer.expandAll();

		setAllChecked(true);
		if (mode == MODE_TREE) {
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
		treeViewer.setAllChecked(state);
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
		if (mode != MODE_FLAT) {
			Action expandAllAction = new Action("Expand All") { //$NON-NLS-1$
				public void run() {
					treeViewer.expandAll();
				}
			};
			menuMgr.add(expandAllAction);
		}
	}

//	private void remove(IResource resource) {
//		ArrayList removedResources = new ArrayList();
//		Iterator iter = resourceList.iterator();
//		while (iter.hasNext()) {
//			IResource checkResource = (IResource) iter.next();
//			if (checkResource.getFullPath().toString().equals(resource.getFullPath().toString())
//					|| (mode != MODE_FLAT && isChild(checkResource, resource))) {
//				removedResources.add(checkResource);
//			}
//		}
//		iter = removedResources.iterator();
//		while (iter.hasNext()) {
//			resourceList.remove(iter.next());
//		}
//	}

//	public void removeUnversioned() {
//		try {
//			Iterator iter = unversionedResourceList.iterator();
//			while (iter.hasNext()) {
//				resourceList.remove(iter.next());
//			}
//
//			resources = new IResource[resourceList.size()];
//			resourceList.toArray(resources);
//			compressedFolders = null;
//			rootFolders = null;
//			folders = null;
//			refresh();
//			includeUnversioned = false;
//		} catch (Exception e) {
//			SVNUIPlugin.openError(getShell(), null, null, e);
//		}
//	}

//	public void addUnversioned() {
//		try {
//			Iterator iter = unversionedResourceList.iterator();
//			while (iter.hasNext()) {
//				resourceList.add(iter.next());
//			}
//
//			resources = new IResource[resourceList.size()];
//			resourceList.toArray(resources);
//			Arrays.sort(resources, comparator);
//			compressedFolders = null;
//			rootFolders = null;
//			folders = null;
//			refresh();
//			checkUnversioned(tree.getItems(), true);
//			includeUnversioned = true;
//		} catch (Exception e) {
//			SVNUIPlugin.openError(getShell(), null, null, e);
//		}
//	}

//	private boolean isChild(IResource resource, IResource parent) {
//		IContainer container = resource.getParent();
//		while (container != null) {
//			if (container.getFullPath().toString().equals(parent.getFullPath().toString())) {
//				return true;
//			}
//			container = container.getParent();
//		}
//		return false;
//	}

	private void handleCheckStateChange(CheckStateChangedEvent event) {
		treeViewer.setGrayed(event.getElement(), false);
		treeViewer.setSubtreeChecked(event.getElement(), event.getChecked());
		IResource resource = (IResource) event.getElement();
		updateParentState(resource, event.getChecked());
	}

	private void updateParentState(IResource child, boolean baseChildState) {
		if (mode == MODE_FLAT || child == null || child.getParent() == null || resources.contains(child.getParent())) {
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
		if (mode == MODE_TREE) {
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
			if (mode != MODE_FLAT && element instanceof IContainer) {
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
				if (mode == MODE_FLAT) {
					return resources.toArray(new IResource[resources.size()]);
				} else if (mode == MODE_COMPRESSED_FOLDERS) {
					return getCompressedFolders();
				} else {
					return getRootFolders();
				}
			}
			if (parentElement instanceof IContainer) {
				if (mode == MODE_COMPRESSED_FOLDERS) {
					return getChildResources((IContainer) parentElement);
				}
				if (mode == MODE_TREE) {
					return getFolderChildren((IContainer) parentElement);
				}
			}
			return new Object[0];
		}
	}

	private class ResourceSelectionLabelProvider extends LabelProvider {
		private final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

//		private final AbstractSynchronizeLabelProvider syncLabelProvider = new AbstractSynchronizeLabelProvider() {
//
//			protected ILabelProvider getDelegateLabelProvider() {
//				return workbenchLabelProvider;
//			}
//
//			protected boolean isDecorationEnabled() {
//				return true;
//			}
//		};

//		private final ResourceSelectionTreeDecorator resourceSelectionTreeDecorator = new ResourceSelectionTreeDecorator();

		public Image getImage(Object element) {
//			Image image = null;
//			if (resources.contains(element)) {
//				image = syncLabelProvider.getImage(element);
//				image = resourceSelectionTreeDecorator.getImage(image, ResourceSelectionTreeDecorator.FILE_CHANGED);
//			} else {
//				image = workbenchLabelProvider.getImage(element);
//			}
//
//			return image;

			return workbenchLabelProvider.getImage(element);
		}

		public String getText(Object element) {
			String text = null;
			IResource resource = (IResource) element;
			if (mode == MODE_FLAT) {
				text = resource.getFullPath().toString();
			} else if (mode == MODE_COMPRESSED_FOLDERS) {
				if (element instanceof IContainer) {
					IContainer container = (IContainer) element;
					text = container.getFullPath().makeRelative().toString();
				} else {
					text = resource.getName();
				}
			} else {
				text = resource.getName();
			}
			return text;
		}
	}

	private class ResourceComparator implements Comparator<IResource> {
		public int compare(IResource obj0, IResource obj1) {
			return obj0.getFullPath().toOSString().compareTo(obj1.getFullPath().toOSString());
		}
	}

	public static interface IToolbarControlCreator {
		public void createToolbarControls(ToolBarManager toolbarManager);

		public int getControlCount();
	}

	public void setResources(@NotNull Map<IResource, ResourceStatus> resourcesToShow) {
		resources = new ArrayList<IResource>(resourcesToShow.keySet());

		compressedFolders = null;
		folders = null;
		rootFolders = null;

		if (resources != null) {
			Collections.sort(resources, comparator);
		}
	}
}
