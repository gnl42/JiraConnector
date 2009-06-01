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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.AddCommentToBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.AddLabelToBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.NewTaskFromFailedBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.RunBuildAction;
import com.atlassian.connector.eclipse.ui.commons.AtlassianUiUtil;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.editor.EditorBusyIndicator;
import org.eclipse.mylyn.internal.provisional.commons.ui.editor.IBusyEditor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.WorkbenchPart;

import java.util.ArrayList;

/**
 * Bamboo Build Rich Editor
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooEditor extends SharedHeaderFormEditor {

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.editors.build"; //$NON-NLS-1$

	private Composite editorParent;

	private IHyperlinkListener messageHyperLinkListener;

	private EditorBusyIndicator editorBusyIndicator;

	private MenuManager menuManager;

	private BambooEditorInput editorInput;

	private BambooBuild bambooBuild;

	private TaskRepository taskRepository;

	private IFormPage bambooBuildEditorPage;

	@Override
	protected Composite createPageContainer(Composite parent) {
		this.editorParent = parent;
		return super.createPageContainer(parent);
	}

	Composite getEditorParent() {
		return editorParent;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof BambooEditorInput)) {
			throw new PartInitException("Invalid editor input \"" + input.getClass() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		super.init(site, input);

		this.editorInput = (BambooEditorInput) input;
		this.bambooBuild = editorInput.getBambooBuild();
		this.taskRepository = editorInput.getRepository();

		setPartName(input.getName());

//		// activate context - not needed for now
//		IContextService contextSupport = (IContextService) site.getService(IContextService.class);
//		if (contextSupport != null) {
//			contextSupport.activateContext(ID);
//		}
	}

	private void initialize() {
		editorBusyIndicator = new EditorBusyIndicator(new IBusyEditor() {
			public Image getTitleImage() {
				return BambooEditor.this.getTitleImage();
			}

			public void setTitleImage(Image image) {
				BambooEditor.this.setTitleImage(image);
			}
		});

		menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(getContainer());
		getContainer().setMenu(menu);
		getEditorSite().registerContextMenu(menuManager, getEditorSite().getSelectionProvider(), false);

		// install context menu on form heading and title
		getHeaderForm().getForm().setMenu(menu);
		Composite head = getHeaderForm().getForm().getForm().getHead();
		if (head != null) {
			CommonUiUtil.setMenu(head, menu);
		}
	}

	@Override
	public void setFocus() {
		if (bambooBuildEditorPage != null) {
			bambooBuildEditorPage.setFocus();
		}
	}

	@Override
	protected void addPages() {
		initialize();

		try {
			bambooBuildEditorPage = new BambooBuildEditorPage(this, "Bamboo");
			int index = addPage(bambooBuildEditorPage);
			setPageImage(index, CommonImages.getImage(BambooImages.BAMBOO));
			setPageText(index, "Bamboo");
		} catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
					"Could not create Bamboo Build editor.", e));
		}

		//for header toolbar: see TaskEditor.updateHeaderToolBar()
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public Menu getMenu() {
		return getContainer().getMenu();
	}

	public BambooEditorInput getEditorInput() {
		return editorInput;
	}

	@Override
	protected void createHeaderContents(IManagedForm headerForm) {
		getToolkit().decorateFormHeading(headerForm.getForm().getForm());
		updateHeader();
	}

	private void updateHeader() {
		BambooEditorInput input = getEditorInput();
		switch (bambooBuild.getStatus()) {
		case FAILURE:
			getHeaderForm().getForm().setImage(CommonImages.getImage(BambooImages.STATUS_FAILED));
			break;
		case SUCCESS:
			getHeaderForm().getForm().setImage(CommonImages.getImage(BambooImages.STATUS_PASSED));
			break;
		default:
			getHeaderForm().getForm().setImage(CommonImages.getImage(BambooImages.STATUS_DISABLED));
			break;
		}
		getHeaderForm().getForm().setText("Build " + input.getToolTipText());
		setTitleToolTip(input.getToolTipText());
		setPartName(input.getToolTipText());
	}

	public void updateHeaderToolBar() {
		final Form form = getHeaderForm().getForm().getForm();
		IToolBarManager toolBarManager = form.getToolBarManager();

		toolBarManager.removeAll();
		toolBarManager.update(true);

		ControlContribution repositoryLabelControl = new ControlContribution("Title") {
			@Override
			protected Control createControl(Composite parent) {
				FormToolkit toolkit = getHeaderForm().getToolkit();
				Composite composite = toolkit.createComposite(parent);
				composite.setLayout(new RowLayout());
				composite.setBackground(null);
				String label = taskRepository.getRepositoryLabel();
				if (label.indexOf("//") != -1) { //$NON-NLS-1$
					label = label.substring((taskRepository.getRepositoryUrl().indexOf("//") + 2)); //$NON-NLS-1$
				}

				Hyperlink link = new Hyperlink(composite, SWT.NONE);
				link.setText(label);
				link.setFont(JFaceResources.getBannerFont());
				link.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
				link.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						TasksUiUtil.openEditRepositoryWizard(taskRepository);
					}
				});

				return composite;
			}
		};
		toolBarManager.add(repositoryLabelControl);

		for (IFormPage page : getPages()) {
			if (page instanceof BambooFormPage) {
				BambooFormPage taskEditorPage = (BambooFormPage) page;
				taskEditorPage.fillToolBar(toolBarManager);
			}
		}

		BaseSelectionListenerAction runBuildAction = new RunBuildAction();
		runBuildAction.selectionChanged(new StructuredSelection(bambooBuild));
		toolBarManager.add(runBuildAction);

		toolBarManager.add(new Separator());

		BaseSelectionListenerAction addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.selectionChanged(new StructuredSelection(bambooBuild));
		toolBarManager.add(addLabelToBuildAction);

		BaseSelectionListenerAction addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.selectionChanged(new StructuredSelection(bambooBuild));
		toolBarManager.add(addCommentToBuildAction);

		BaseSelectionListenerAction newTaskFromFailedBuildAction = new NewTaskFromFailedBuildAction();
		newTaskFromFailedBuildAction.selectionChanged(new StructuredSelection(bambooBuild));
		toolBarManager.add(newTaskFromFailedBuildAction);

		toolBarManager.add(new Separator());

		final String buildUrl = bambooBuild.getBuildUrl();
		if (buildUrl != null && buildUrl.length() > 0) {
			Action openWithBrowserAction = new Action() {
				@Override
				public void run() {
					TasksUiUtil.openUrl(BambooUtil.getUrlFromBuild(bambooBuild));
				}
			};
			openWithBrowserAction.setImageDescriptor(CommonImages.BROWSER_OPEN_TASK);
			openWithBrowserAction.setToolTipText("Open with Web Browser");
			toolBarManager.add(openWithBrowserAction);
		}

		toolBarManager.update(true);

		updateHeader();
	}

	@SuppressWarnings("unchecked")
	IFormPage[] getPages() {
		ArrayList formPages = new ArrayList();
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++) {
				Object page = pages.get(i);
				if (page instanceof IFormPage) {
					formPages.add(page);
				}
			}
		}
		return (IFormPage[]) formPages.toArray(new IFormPage[formPages.size()]);
	}

	public void setMessage(String message, int type, IHyperlinkListener listener) {
		if (getHeaderForm() != null && getHeaderForm().getForm() != null) {
			if (!getHeaderForm().getForm().isDisposed()) {
				Form form = getHeaderForm().getForm().getForm();
				form.setMessage(message, type);
				if (messageHyperLinkListener != null) {
					form.removeMessageHyperlinkListener(messageHyperLinkListener);
				}
				if (listener != null) {
					form.addMessageHyperlinkListener(listener);
				}
				messageHyperLinkListener = listener;
			}
		}
	}

	@Override
	public void showBusy(boolean busy) {
		if (editorBusyIndicator != null) {
			if (busy) {
				if (AtlassianUiUtil.isAnimationsEnabled()) {
					editorBusyIndicator.start();
				}
			} else {
				editorBusyIndicator.stop();
			}
		}

		if (getHeaderForm() != null && getHeaderForm().getForm() != null && !getHeaderForm().getForm().isDisposed()) {
			Form form = getHeaderForm().getForm().getForm();
			if (form != null && !form.isDisposed()) {
				IToolBarManager toolBarManager = form.getToolBarManager();
				if (toolBarManager instanceof ToolBarManager) {
					ToolBar control = ((ToolBarManager) toolBarManager).getControl();
					if (control != null) {
						control.setEnabled(!busy);
					}
				}

				CommonUiUtil.setEnabled(form.getBody(), !busy);
				for (IFormPage page : getPages()) {
					if (page instanceof WorkbenchPart) {
						WorkbenchPart part = (WorkbenchPart) page;
						part.showBusy(busy);
					}
				}
			}
		}
	}
}
