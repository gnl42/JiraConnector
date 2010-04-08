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

package com.atlassian.connector.eclipse.internal.crucible.ui.dialogs;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonTextSupport;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener;
import org.eclipse.mylyn.internal.wikitext.tasks.ui.editor.ConfluenceMarkupTaskEditorExtension;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractCrucibleCommentDialog extends ProgressDialog {

	private final String taskKey;

	private final String taskId;

	private Review review;

	protected final HashMap<CustomFieldDef, ComboViewer> customCombos;

	protected final HashMap<String, CustomField> customFieldSelections;

	protected Button defectButton;

	protected CommonTextSupport textSupport;
	protected IContextService contextService;
	protected IContextActivation commentContext;
	private Action toggleEditAction;
	private Action toggleBrowserAction;
	protected boolean ignoreToggleEvents;
	protected RichTextEditor commentText;
	protected final TaskRepository taskRepository;

	public AbstractCrucibleCommentDialog(Shell parentShell, TaskRepository taskRepository, Review review,
			String taskKey, String taskId) {
		super(parentShell);
		this.taskRepository = taskRepository;
		this.review = review;
		this.taskKey = taskKey;
		this.taskId = taskId;
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	@Nullable
	public Review getReview() {
		return review;
	}

	protected void setReview(Review review) {
		this.review = review;
	}

	protected void createWikiTextControl(Composite composite, FormToolkit toolkit) {
		final AbstractTaskEditorExtension extension = new ConfluenceMarkupTaskEditorExtension();
		final String contextId = extension.getEditorContextId();

		contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		commentContext = contextService.activateContext(contextId, new ActiveShellExpression(getShell()));

		commentText = new RichTextEditor(taskRepository, SWT.MULTI | SWT.BORDER, contextService, extension);
		final MarkupParser markupParser = new MarkupParser();
		final MarkupLanguage markupLanguage = ServiceLocator.getInstance().getMarkupLanguage("Confluence");
		if (markupLanguage != null) {
			commentText.setRenderingEngine(new AbstractRenderingEngine() {

				@Override
				public String renderAsHtml(TaskRepository repository, String text, IProgressMonitor monitor)
						throws CoreException {

					markupParser.setMarkupLanguage(markupLanguage);
					String htmlContent = markupParser.parseToHtml(text);
					return htmlContent;
				}
			});
		} else {
			StatusHandler.log(new Status(IStatus.INFO, CrucibleUiPlugin.PLUGIN_ID,
							"Unable to locate Confluence MarkupLanguage. Browser preview will not be possible"));
		}

		commentText.setReadOnly(false);
		commentText.setSpellCheckingEnabled(true);

		// creating toolbar - must be created before actually commentText widget is created, to stay on top
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		fillToolBar(toolBarManager, commentText);

		if (toolBarManager.getSize() > 0) {
			Composite toolbarComposite = toolkit.createComposite(composite);
			GridDataFactory.fillDefaults().grab(false, false).hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.END, SWT.FILL)
					.applyTo(toolbarComposite);
			toolbarComposite.setBackground(null);
			RowLayout rowLayout = new RowLayout();
			rowLayout.marginLeft = 0;
			rowLayout.marginRight = 0;
			rowLayout.marginTop = 0;
			rowLayout.marginBottom = 0;
			rowLayout.center = true;
			toolbarComposite.setLayout(rowLayout);

			toolBarManager.createControl(toolbarComposite);
		}
		// end of toolbar creation stuff

		// auto-completion support
		commentText.createControl(composite, toolkit);
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		if (handlerService != null) {
			textSupport = new CommonTextSupport(handlerService);
			textSupport.install(commentText.getViewer(), true);
		}

		commentText.showEditor();
		commentText.getViewer().getTextWidget().setBackground(null);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(commentText.getControl());

		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 100;
		textGridData.widthHint = 500;
		commentText.getControl().setLayoutData(textGridData);
	}

	protected void fillToolBar(ToolBarManager manager, final RichTextEditor editor) {
		if (editor.hasPreview()) {
			toggleEditAction = new Action("", SWT.TOGGLE) { //$NON-NLS-1$
				@Override
				public void run() {
					if (isChecked()) {
						editor.showEditor();
					} else {
						editor.showPreview();
					}

					if (toggleBrowserAction != null) {
						toggleBrowserAction.setChecked(false);
					}
				}
			};
			toggleEditAction.setImageDescriptor(CommonImages.EDIT_SMALL);
			toggleEditAction.setToolTipText(Messages.TaskEditorRichTextPart_Edit_Tooltip);
			toggleEditAction.setChecked(true);
			editor.addStateChangedListener(new StateChangedListener() {
				public void stateChanged(StateChangedEvent event) {
					try {
						ignoreToggleEvents = true;
						toggleEditAction.setChecked(event.state == State.EDITOR || event.state == State.DEFAULT);
					} finally {
						ignoreToggleEvents = false;
					}
				}
			});
			manager.add(toggleEditAction);
		}
		if (/* toggleEditAction == null && */editor.hasBrowser()) {
			toggleBrowserAction = new Action("", SWT.TOGGLE) { //$NON-NLS-1$
				@Override
				public void run() {
					if (ignoreToggleEvents) {
						return;
					}
					if (isChecked()) {
						editor.showBrowser();
					} else {
						editor.showEditor();
					}

					if (toggleEditAction != null) {
						toggleEditAction.setChecked(false);
					}
				}
			};
			toggleBrowserAction.setImageDescriptor(CommonImages.PREVIEW_WEB);
			toggleBrowserAction.setToolTipText(Messages.TaskEditorRichTextPart_Browser_Preview);
			toggleBrowserAction.setChecked(false);
			editor.addStateChangedListener(new StateChangedListener() {
				public void stateChanged(StateChangedEvent event) {
					try {
						ignoreToggleEvents = true;
						toggleBrowserAction.setChecked(event.state == State.BROWSER);
					} finally {
						ignoreToggleEvents = false;
					}
				}
			});
			manager.add(toggleBrowserAction);
		}
	}

	@Override
	protected Collection<? extends Control> getDisableableControls() {
		Set<Control> controls = new HashSet<Control>(super.getDisableableControls());
		if (customCombos.size() > 0) {
			for (ComboViewer viewer : customCombos.values()) {
				controls.add(viewer.getControl());
			}
		}

		if (defectButton != null) {
			controls.add(defectButton);
		}

		return controls;
	}

	@Override
	public boolean close() {
		if (contextService != null && commentContext != null) {
			contextService.deactivateContext(commentContext);
			commentContext = null;
		}

		if (textSupport != null) {
			textSupport.dispose();
		}
		return super.close();
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public void cancelUpdateComment() {
		setReturnCode(Window.CANCEL);
		close();
	}

	public String getTaskKey() {
		return taskKey;
	}

	public String getTaskId() {
		return taskId;
	}

}