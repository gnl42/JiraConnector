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

package com.atlassian.theplugin.eclipse.view.bamboo;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooStatusListener;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;

public class BambooToolWindowContent implements BambooStatusListener {

	
	private Collection<BambooBuildAdapterEclipse> buildStatuses = new ArrayList<BambooBuildAdapterEclipse>();
	private Table table;
	private TableViewer tableViewer;
	private final BambooToolWindow viewPart;

	public BambooToolWindowContent(Composite parent, final BambooToolWindow viewPart) {
		
		this.viewPart = viewPart;
		
		createTable(parent);	
		
	}

	private void createTable(Composite parent) {
		
		createTableViewer(parent);
		createTableColumns();
		createContextMenu();
		
		tableViewer.setInput(buildStatuses);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
	}

	private void createContextMenu() {
		
		final MenuManager menuManager = new MenuManager();
		
		menuManager.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				
				menuManager.add(viewPart.getRunBuildAction());
				menuManager.add(viewPart.getLabelBuildAction());
				menuManager.add(viewPart.getCommentBuildAction());
				menuManager.add(new Separator());
				menuManager.add(viewPart.getShowBuilLogAction());
				menuManager.add(new Separator());
				menuManager.add(viewPart.getRefreshBuildListAction());
				menuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
			}
			
		});
		
		menuManager.setRemoveAllWhenShown(true);		
		viewPart.getViewSite().registerContextMenu(menuManager, tableViewer);
		table.setMenu(menuManager.createContextMenu(table));
	}

	private void createTableColumns() {
		// create columns
		TableColumn tableColumn; 
		
		ControlListener columnListener = new BambooTableColumnListener();
		
		List<Integer> configColumnsWidth = 
			Activator.getDefault().getPluginConfiguration().getBambooTabConfiguration().getColumnsWidth();
		
		for (int i = 0; i < Column.values().length; ++i) {
			Column column = Column.values()[i];
			tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setText(column.columnName());
			
			tableColumn.setWidth(column.columnWidth());

			tableColumn.setMoveable(true);
			tableColumn.setResizable(true);
		}
		
		// set columns width according to the config values
		if (configColumnsWidth.size() == Column.values().length) {
			for (int i = 0; i < configColumnsWidth.size(); ++i) {
				table.getColumn(i).setWidth(configColumnsWidth.get(i));
			}
		}

		// set columns order
		int[] order = 
			Activator.getDefault().getPluginConfiguration().getBambooTabConfiguration().getColumnsOrder();
		
		if (order.length == Column.values().length) {
			table.setColumnOrder(order);
		}
		
		// add width and order listener
		for (TableColumn column : table.getColumns()) {
			column.addControlListener(columnListener);
		}
	}

	private void createTableViewer(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL 
					| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		
		tableViewer = new TableViewer(parent, style);
		tableViewer.setContentProvider(new BambooContentProvider());
		tableViewer.setLabelProvider(new BambooLabelProvider());
		
		tableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
					/**
					 * Disable/enable toolbar buttons/actions 
					 */
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection().isEmpty()) {
							viewPart.disableBambooBuildActions();
						} else if (getBuild(event).isBamboo2()) {
							viewPart.enableBamboo2BuildActions();
							viewPart.enableBambooBuildActions();
						} else {
							viewPart.enableBambooBuildActions();
						}
					}
				});
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			/**
			 * opens browser with build details
			 */
			public void doubleClick(DoubleClickEvent event) {
				BambooBuildAdapterEclipse build = getBuild(event);
				
				try {
					Activator.getDefault().getWorkbench().getBrowserSupport().createBrowser(
									IWorkbenchBrowserSupport.AS_EXTERNAL,
									"browserId", "name", "tooltip").openURL(
									new URL(build.getBuildResultUrl()));
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		
		table = tableViewer.getTable();
	}
	
	public void updateBuildStatuses(Collection<BambooBuild> aBuildStatuses) {
		
		Date pollingTime = new Date();
		
		this.buildStatuses.clear();
		
		//PluginUtil.getLogger().debug(buildStatuses.toString());
		
		for (BambooBuild build : aBuildStatuses) {
			this.buildStatuses.add(new BambooBuildAdapterEclipse(build));
			pollingTime = build.getPollingTime();
			
//			if (build.getTestsFailed() < build.getTestsPassed()) {
//				BuildDetails buildDetails;
//				try {
//					buildDetails = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()).
//						getBuildDetails(build.getServer(), build.getBuildKey(), build.getBuildNumber());
//					
//					
//					List<TestDetails> testDetails = buildDetails.getFailedTestDetails();
//					
//					System.out.println(build.getBuildKey());
//					
//					for (TestDetails test : testDetails) {
//						System.out.println(test.getTestClassName() + " : " + test.getTestMethodName());
//					}
//					
//					for (BambooChangeSet changeSet : buildDetails.getCommitInfo()) {
//						System.out.println(changeSet.getAuthor() + " : " + changeSet.getComment());
//					}
//					
//				} catch (ServerPasswordNotProvidedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (RemoteApiException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
		 
		tableViewer.setInput(aBuildStatuses);
		viewPart.setHeaderText("Last polling time: " + pollingTime.toString());
		
	}

	public void resetState() {
	}

	private class BambooTableColumnListener implements ControlListener {
		
		public void controlMoved(ControlEvent e) {
			Activator.getDefault().getPluginConfiguration().getBambooTabConfiguration().
				setColumnsOrder(table.getColumnOrder());
		}
		
		public void controlResized(ControlEvent e) {
			List<Integer> columnsWidth = new ArrayList<Integer>(table.getColumns().length);
			for (TableColumn column : table.getColumns()) {
				columnsWidth.add(column.getWidth());
			}
			Activator.getDefault().getPluginConfiguration().getBambooTabConfiguration().setColumnsWidth(columnsWidth);
		}
	}

	private class BambooContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return buildStatuses.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class BambooLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			
			BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) element;
			
			Column column = Column.valueOfAlias(table.getColumn(columnIndex).getText());
			
			if (column == Column.BUILD_STATUS) {
				switch (build.getStatus()) {
					case BUILD_SUCCEED:
						return PluginIcons.getImageRegistry().get(BuildStatus.BUILD_SUCCEED.toString());
					case BUILD_FAILED:
						return PluginIcons.getImageRegistry().get(BuildStatus.BUILD_FAILED.toString());
					case UNKNOWN:
						return PluginIcons.getImageRegistry().get(BuildStatus.UNKNOWN.toString());
					default:
						return null;
				}
			}
			
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) element;
			
			Column column = Column.valueOfAlias(table.getColumn(columnIndex).getText());
			
			switch (column) {
				
				case BUILD_DATE:
					return build.getBuildTimeFormated();
				case BUILD_NUMBER:
					return build.getBuildNumber();
				case BUILD_KEY:
					return build.getBuildKey();
				case BUILD_STATUS:
					return ""; //build.getStatus().toString();
				case PROJECT_KEY:
					return build.getProjectName();
				case BUILD_REASON:
					return build.getBuildReason();
				case MESSAGE:
					return build.getMessage();
				case FAILED_TESTS:
					return build.getTestsPassedSummary();
				case SERVER:
					return build.getServerName();
				default:
					return "";
			}
		}
	}
	
	/**
	 * That class provides column names and width for bamboo tab table
	 */
	private enum Column {
		BUILD_STATUS ("ST", 30),
		BUILD_KEY ("Build Plan", 80),
		BUILD_NUMBER ("Build Number", 100),
		PROJECT_KEY ("Project", 100),
		BUILD_DATE ("Build Date", 100),
		FAILED_TESTS ("Tests", 100),
		BUILD_REASON ("Reason", 100),
		SERVER ("Server", 100),
		MESSAGE ("Message", 300);
		
		private String columnName;
		private int columnWidth;

		Column(String columnName, int columnWidth) {
			this.columnName = columnName;
			this.columnWidth = columnWidth;
		}
		
		public static Column valueOfAlias(String text) {
			for (Column column : Column.values()) {
				if (column.columnName().equals(text)) {
					return column;
				}
			}
			return null;
		}

		public String columnName() {
			return columnName;
		}

		public int columnWidth() {
			return columnWidth;
		}
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private BambooBuildAdapterEclipse getBuild(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) selection.getFirstElement();
		return build;
	}
	
	private BambooBuildAdapterEclipse getBuild(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) selection.getFirstElement();
		return build;
	}	
}
