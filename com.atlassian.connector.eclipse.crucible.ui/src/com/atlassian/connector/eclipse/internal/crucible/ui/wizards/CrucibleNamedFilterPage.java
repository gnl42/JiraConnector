/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

/**
 * Query page for predefined filters
 * 
 * @author Shawn Minto
 */
public class CrucibleNamedFilterPage extends AbstractRepositoryQueryPage2 implements IWizardPage {

	private static final String TITLE = "New Crucible Query";

	private Button customButton;

	private Button predefinedButton;

	private ListViewer filterList;

	public CrucibleNamedFilterPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);
		setNeedsRepositoryConfiguration(false);
	}

	public CrucibleNamedFilterPage(TaskRepository repository) {
		super(TITLE, repository, null);
		setNeedsRepositoryConfiguration(false);
	}

	@Override
	protected void createPageContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		predefinedButton = new Button(composite, SWT.RADIO);
		predefinedButton.setText("Predefined Filter");
		predefinedButton.setEnabled(true);
		predefinedButton.setSelection(true);
		predefinedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateQueryPage();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(predefinedButton);

		customButton = new Button(composite, SWT.RADIO);
		customButton.setText("Custom");
		customButton.setEnabled(false);
		customButton.setSelection(false);
		customButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateQueryPage();
			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(customButton);

		filterList = new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		filterList.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				return new Object[0];
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof PredefinedFilter[]) {
					return (Object[]) inputElement;
				}
				return new Object[0];
			}

			public void dispose() {
				// ignore

			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// ignore
			}

		});
		filterList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PredefinedFilter) {
					return ((PredefinedFilter) element).getFilterName();
				}
				return "";
			};
		});
		filterList.setInput(PredefinedFilter.values());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(filterList.getControl());
	}

	private void updateQueryPage() {
		if (customButton.getSelection()) {
			filterList.getControl().setEnabled(false);
			// TODO add the next page for custom queries
		} else if (predefinedButton.getSelection()) {
			filterList.getControl().setEnabled(true);
		}
	}

	@Override
	protected void doRefresh() {
		// we dont need to do anything since this data isnt from the repository
	}

	@Override
	protected boolean hasRepositoryConfiguration() {
		return getClient().hasData();
	}

	private CrucibleClient getClient() {
		return ((CrucibleRepositoryConnector) getConnector()).getClientManager().getClient(getTaskRepository());
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {
		doRefresh();

		if (query != null) {
			String filterId = query.getAttribute(CrucibleUtil.KEY_FILTER_ID);
			PredefinedFilter filter = CrucibleUtil.getPredefinedFilter(filterId);
			if (filter != null) {
				filterList.setSelection(new StructuredSelection(filter));
			}
		}
		return false;
	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		String filterId = getFilterId();
		query.setSummary(getQueryTitle());
		query.setAttribute(CrucibleUtil.KEY_FILTER_ID, filterId);

		query.setUrl(CrucibleUtil.getPredefinedFilterWebUrl(getTaskRepository(), filterId));
	}

	private String getFilterId() {
		if (predefinedButton.getSelection()) {
			StructuredSelection selection = (StructuredSelection) filterList.getSelection();
			Object element = selection.getFirstElement();
			if (element instanceof PredefinedFilter) {
				return ((PredefinedFilter) element).getFilterUrl();
			}
		}
		return "";
	}

	@Override
	public boolean canFlipToNextPage() {
		return predefinedButton.getSelection() && getFilterId() != null && getFilterId().length() > 0;
	}

}
