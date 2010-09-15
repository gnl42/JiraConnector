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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleConstants;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.ui.commons.TreeContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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

	private CrucibleCustomFilterPage customPage;

	public CrucibleNamedFilterPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);
		setNeedsRepositoryConfiguration(false);
		setDescription("Select a pre-defined filter to create a query");
	}

	public CrucibleNamedFilterPage(TaskRepository repository) {
		this(repository, null);
	}

	@Override
	protected void createPageContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		if (getQuery() == null) {
			customButton = new Button(composite, SWT.RADIO);
			customButton.setText("Custom");
			customButton.setSelection(false);
			customButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateQueryPage();
				}
			});

			GridDataFactory.fillDefaults().grab(true, false).applyTo(customButton);

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
		} else {
			Label label = new Label(composite, SWT.NONE);
			label.setText("Predefined Filter:");
		}

		filterList = new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		filterList.setContentProvider(new TreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof PredefinedFilter[]) {
					return (Object[]) inputElement;
				}
				return new Object[0];
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
		filterList.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) filterList.getSelection();
				if (!selection.isEmpty() && selection.getFirstElement() instanceof PredefinedFilter) {
					setQueryTitle(((PredefinedFilter) selection.getFirstElement()).getFilterName());
				}
				updateQueryPage();
			}

		});

		GridDataFactory.fillDefaults().grab(true, true).applyTo(filterList.getControl());
	}

	private void updateQueryPage() {
		// TODO disable the title??
		if (isCustomSelected()) {
			filterList.getControl().setEnabled(false);
		} else {
			filterList.getControl().setEnabled(true);
		}
		getContainer().updateButtons();
	}

	@Override
	protected void doRefresh() {
		// we dont need to do anything since this data isnt from the repository
	}

	@Override
	protected boolean hasRepositoryConfiguration() {
		return getClient().hasRepositoryData();
	}

	private CrucibleClient getClient() {
		return ((CrucibleRepositoryConnector) getConnector()).getClientManager().getClient(getTaskRepository());
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {
		doRefresh();

		if (query != null) {
			String filterId = query.getAttribute(CrucibleConstants.KEY_FILTER_ID);
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
		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, filterId);

		query.setUrl(CrucibleUtil.getPredefinedFilterWebUrl(getTaskRepository().getUrl(), filterId));
	}

	private String getFilterId() {
		if (!isCustomSelected()) {
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
		return isCustomSelected();
	}

	@Override
	public boolean isPageComplete() {
		return isCustomSelected() ? false : !filterList.getSelection().isEmpty() && super.isPageComplete();
	}

	private boolean isCustomSelected() {
		return customButton != null && customButton.getSelection();
	}

	@Override
	public IWizardPage getNextPage() {
		if (!isCustomSelected()) {
			return null;
		}
		if (customPage == null) {
			customPage = new CrucibleCustomFilterPage(getTaskRepository(), getQuery(), getQueryTitle());
			if (getWizard() instanceof Wizard) {
				((Wizard) getWizard()).addPage(customPage);
			}
		}
		return customPage;
	}

}
