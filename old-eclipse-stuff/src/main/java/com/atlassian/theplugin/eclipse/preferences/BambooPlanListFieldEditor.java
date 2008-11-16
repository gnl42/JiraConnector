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

package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooPlanData;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

/**
 * A field editor for a string type preference.
 * <p>
 * This class may be used as is, or subclassed as required.
 * </p>
 */
public class BambooPlanListFieldEditor extends FieldEditor implements
		IPropertyChangeListener {

	private static final String WAITING_FOR_PLANS_MESSAGE = "Waiting for plans list...";

	private Table table;

	private Set<SubscribedPlan> subscribedPlans;

	private PreferencePageServers parentPreferencePage;

	private Button refreshButton;

	private Text messageArea;

	private boolean useFavourites = false;

	private Composite parent;

	/**
	 * Creates a new string field editor
	 */
	protected BambooPlanListFieldEditor() {
	}

	/**
	 * Creates a string field editor of unlimited width. Use the method
	 * <code>setTextLimit</code> to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 * @param preferencePageServers
	 */
	public BambooPlanListFieldEditor(String name, String labelText,
			Composite parent, PreferencePageServers preferencePageServers) {
		init(name, labelText);
		createControl(parent);
		this.parent = parent;
		this.parentPreferencePage = preferencePageServers;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) table.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code>
	 * framework method contributes the text field. Subclasses may override but
	 * must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	protected void doFillIntoGrid(final Composite aParent, int numColumns) {

		Label empty1 = new Label(aParent, SWT.NONE);

		refreshButton = new Button(aParent, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton
				.setToolTipText("Refresh list using form values instead of saved ones");
		refreshButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				super.mouseUp(e);

				ServerBean bambooServer = new ServerBean();
				bambooServer.setUrlString(parentPreferencePage.getBambooUrl());
				bambooServer.setUserName(parentPreferencePage.getUserName());
				bambooServer.transientSetPasswordString(parentPreferencePage
						.getPassword(), true);

				loadServerPlans(bambooServer);
				refreshButton.setEnabled(false);
				setEnabled(false, aParent);
				messageArea.setText(WAITING_FOR_PLANS_MESSAGE);
			}

		});

		Label label = getLabelControl(aParent);
		GridData gdLabel = new GridData();
		gdLabel.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gdLabel);

		table = getTableControl(aParent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.heightHint = convertVerticalDLUsToPixels(table, 70);
		gd.verticalAlignment = GridData.FILL;
		table.setLayoutData(gd);

		Label empty2 = new Label(aParent, SWT.NONE);

		messageArea = new Text(aParent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
				| SWT.READ_ONLY);
		gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.heightHint = 35;
		// gd.grabExcessVerticalSpace = true;
		messageArea.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoad() {

		this.subscribedPlans = subscribedPlansString2Collection(getPreferenceStore()
				.getString(getPreferenceName()));

		loadServerPlans();
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoadDefault() {
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), getStringValue());
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns the field editor's value.
	 * 
	 * @return the current value
	 */
	private String getStringValue() {
		if (table != null) {

			StringBuffer ret = new StringBuffer();

			for (TableItem item : table.getItems()) {
				if (item.getChecked()) {
					ret.append(item.getText(2));
					ret.append(" ");
				}
			}
			return ret.toString();
		}

		return getPreferenceStore().getString(getPreferenceName());
	}

	/**
	 * Returns this field editor's text control.
	 * 
	 * @return the text control, or <code>null</code> if no text field is
	 *         created yet
	 */
	protected Table getTableControl() {
		return table;
	}

	private Table getTableControl(Composite aParent) {
		if (table == null) {

			int style = SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

			table = new Table(aParent, style);

			TableColumn tableColumn;

			for (int i = 0; i < Column.values().length; ++i) {
				Column column = Column.values()[i];

				tableColumn = new TableColumn(table, SWT.LEFT);
				tableColumn.setText(column.columnName());
				tableColumn.setWidth(column.columnWidth());
				tableColumn.setMoveable(false);
				tableColumn.setResizable(false);

			}

			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					table = null;
				}
			});

		} else {
			checkParent(table, aParent);
		}

		return table;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public void setFocus() {
		if (table != null) {
			table.setFocus();
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	public void setEnabled(boolean enabled) {
		if ((enabled == true && useFavourites == false) || enabled == false) {
			super.setEnabled(enabled, parent);
			getTableControl(parent).setEnabled(enabled);
			// refreshButton.setEnabled(enabled);
		}
	}

	/**
	 * Sets plans (fills the whole list).
	 * 
	 * @param allPlans
	 */
	private void setPlans(Collection<BambooPlan> allPlans) {
		if (table != null && !table.isDisposed()) {
			fillTable(allPlans);
		}
	}

	private void fillTable(Collection<BambooPlan> serverPlans) {
		table.clearAll();
		table.setItemCount(0); // this is necessary to clear the table

		TableItem item;

		// add server plans to the list
		for (BambooPlan plan : serverPlans) {
			item = new TableItem(table, SWT.NONE);

			// check the plan if was subscribed
			if (subscribedPlans.contains(new SubscribedPlanBean(plan
					.getPlanKey()))) {
				item.setChecked(true);
			}

			if (plan.isFavourite()) {
				item.setImage(Column.FAVOURITE.ordinal(), 
						PluginIcons.getImageRegistry().get(PluginIcons.ICON_FAVOURITE_ON));
			} else {
				item.setImage(Column.FAVOURITE.ordinal(), 
						PluginIcons.getImageRegistry().get(PluginIcons.ICON_FAVOURITE_OFF));
			}
			item.setText(Column.PLAN_KEY.ordinal(), plan.getPlanKey());
		}

		// add to the list subscribed plans which do not exist on the server
		// (with grey icon)
		for (SubscribedPlan plan : subscribedPlans) {
			if (!serverPlans.contains(new BambooPlanData(plan.getPlanId()))) {
				item = new TableItem(table, SWT.NONE);
				item.setChecked(true);
				item.setImage(Column.FAVOURITE.ordinal(), PluginIcons
						.getImageRegistry()
						.get(PluginIcons.ICON_BAMBOO_UNKNOWN));
				item.setText(Column.PLAN_KEY.ordinal(), plan.getPlanId());
			}
		}

		setEnabled(true);
		refreshButton.setEnabled(true);

	}

	private Set<SubscribedPlan> subscribedPlansString2Collection(
			String subscribedPlans) {
		Set<SubscribedPlan> plansList = new HashSet<SubscribedPlan>();

		String[] plansArray = subscribedPlans.split(" ");

		for (String plan : plansArray) {
			if (plan != null && plan.length() > 0) {
				SubscribedPlanBean subscribedPlan = new SubscribedPlanBean(plan);
				plansList.add(subscribedPlan);
			}
		}

		return plansList;
	}

	private enum Column {
		WATCHED("Watched", 25), 
		FAVOURITE("Favourite", 20), 
		PLAN_KEY("Plan Key", 150);

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

		public static String[] getNames() {
			ArrayList<String> list = new ArrayList<String>(
					Column.values().length);
			for (Column column : Column.values()) {
				list.add(column.name());
			}
			return list.toArray(new String[0]);
		}
	}

	private void loadServerPlans() {

		Collection<ServerBean> servers = Activator.getDefault()
				.getPluginConfiguration().getBambooConfigurationData()
				.getServersData();
		final Iterator<ServerBean> iterator = servers.iterator();

		// we take only first server right now
		if (iterator.hasNext()) {
			ServerBean serverBean = iterator.next();
			loadServerPlans(serverBean);
		}
	}

	private void loadServerPlans(final ServerBean bambooServer) {

		setEnabled(false);
		refreshButton.setEnabled(false);
		messageArea.setText(WAITING_FOR_PLANS_MESSAGE);

		Job planListJob = new Job("Atlassian Bamboo plans") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				Collection<BambooPlan> serverPlans = new ArrayList<BambooPlan>(
						0);
				BambooServerFacade bambooFacade = BambooServerFacadeImpl
						.getInstance(PluginUtil.getLogger());
				String message = "";

				try {
					serverPlans = bambooFacade.getPlanList(bambooServer);
				} catch (ServerPasswordNotProvidedException ex) {
					message = ex.getMessage();
				} catch (RemoteApiException ex) {
					message = ex.getMessage();
				}

				final Collection<BambooPlan> plans = serverPlans;
				final String finalMessage = message;

				EclipseActionScheduler.getInstance().invokeLater(
						new Runnable() {
							public void run() {
								if (messageArea != null
										&& !messageArea.isDisposed()) {
									messageArea.setText(finalMessage);
								}
								BambooPlanListFieldEditor.this.setPlans(plans);
							}
						});

				return Status.OK_STATUS;
			}
		};

		planListJob.schedule();

	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.BAMBOO_URL)
				|| event.getProperty().equals(
						PreferenceConstants.BAMBOO_USER_NAME)
				|| event.getProperty().equals(
						PreferenceConstants.BAMBOO_USER_PASSWORD)) {

			doLoad();

		}
	}

	public void setFavourites(boolean useFavourites) {
		this.useFavourites = useFavourites;
		this.setEnabled(!useFavourites);
	}
}
