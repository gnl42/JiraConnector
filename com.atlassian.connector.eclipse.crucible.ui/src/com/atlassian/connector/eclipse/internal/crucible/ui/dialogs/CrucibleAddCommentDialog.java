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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.operations.AddCommentRemoteOperation;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog shown to the user when they add a comment to a review
 * 
 * @author Thomas Ehrnhoefer
 * @author Shawn Minto
 */
public class CrucibleAddCommentDialog extends ProgressDialog {

	public class AddCommentRunnable implements IRunnableWithProgress {

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				monitor.beginTask("Adding comment", IProgressMonitor.UNKNOWN);
				if (newComment.length() > 0) {

					AddCommentRemoteOperation operation = new AddCommentRemoteOperation(taskRepository, review, client,
							crucibleFile, newComment, monitor);
					operation.setDefect(defect);
					operation.setDraft(draft);
					operation.setCustomFields(customFieldSelections);
					operation.setCommentLines(commentLines);
					operation.setParentComment(parentComment);

					try {
						client.execute(operation);
					} catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Unable to post Comment", e));
						throw e; // rethrow exception so dialog stays open and displays error message
					}
					client.getReview(getTaskRepository(), getTaskId(), true, monitor);
				}
			} catch (CoreException e) {
				throw new InvocationTargetException(e);

			}

		}
	}

	private Review review;

	private final String shellTitle;

	private final TaskRepository taskRepository;

	private final String taskKey;

	private final String taskId;

	private final CrucibleClient client;

	private LineRange commentLines;

	private Comment parentComment;

	private CrucibleFile crucibleFile;

	private static final String SAVE_LABEL = "&Post";

	private static final String DRAFT_LABEL = "Post as &Draft";

	private static final String DEFECT_LABEL = "Defect";

	private final boolean edit = false;

	private final HashMap<CustomFieldDef, ComboViewer> customCombos;

	private final HashMap<String, CustomField> customFieldSelections;

	private FormToolkit toolkit;

	private boolean draft = false;

	private boolean defect = false;

	private RichTextEditor commentText;

	private String newComment;

	private Button defectButton;

	private Button saveButton;

	private Button saveDraftButton;

	private CommonTextSupport textSupport;

	private IContextService contextService;

	private IContextActivation commentContext;

	private Action toggleEditAction;

	private Action toggleBrowserAction;

	protected boolean ignoreToggleEvents;

	public CrucibleAddCommentDialog(Shell parentShell, String shellTitle, Review review, String taskKey, String taskId,
			TaskRepository taskRepository, CrucibleClient client) {
		super(parentShell);
		this.shellTitle = shellTitle;
		this.review = review;
		this.taskKey = taskKey;
		this.taskId = taskId;
		this.taskRepository = taskRepository;
		this.client = client;
		customCombos = new HashMap<CustomFieldDef, ComboViewer>();
		customFieldSelections = new HashMap<String, CustomField>();
	}

	@Override
	protected Control createPageControls(Composite parent) {
		// CHECKSTYLE:MAGIC:OFF
		getShell().setText(shellTitle);
		setTitle(shellTitle);

		if (parentComment == null) {
			setMessage("Create a new comment");
		} else {
			setMessage("Reply to a comment from: " + parentComment.getAuthor().getDisplayName());
		}

		// CHECKSTYLE:MAGIC:OFF
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		if (toolkit == null) {
			toolkit = new FormToolkit(getShell().getDisplay());
		}
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (toolkit != null) {
					toolkit.dispose();
				}
			}
		});

		createAdditionalControl(composite);

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
		commentText.getViewer().addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {
				boolean enabled = false;
				if (commentText != null && commentText.getText().trim().length() > 0) {
					enabled = true;
				}

				if (saveButton != null && !saveButton.isDisposed()
						&& (parentComment == null || !parentComment.isDraft())) {
					saveButton.setEnabled(enabled);
				}

				if (saveDraftButton != null && !saveDraftButton.isDisposed()) {
					saveDraftButton.setEnabled(enabled);
				}
			}
		});

		GridDataFactory.fillDefaults().grab(true, true).applyTo(commentText.getControl());

		GridData textGridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		textGridData.heightHint = 100;
		textGridData.widthHint = 500;
		commentText.getControl().setLayoutData(textGridData);
		commentText.getControl().forceFocus();

		((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
		// create buttons according to (implicit) reply type
		int nrOfCustomFields = 0;
		if (parentComment == null) { // "defect button" needed if new comment
			Composite compositeCustomFields = new Composite(composite, SWT.NONE);
			compositeCustomFields.setLayout(new GridLayout(1, false));
			createDefectButton(compositeCustomFields);
			nrOfCustomFields = addCustomFields(compositeCustomFields);
			GridDataFactory.fillDefaults().grab(true, false).span(nrOfCustomFields + 1, 1).applyTo(
					compositeCustomFields);
		}

		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(composite);

		applyDialogFont(composite);
		return composite;
	}

	private void fillToolBar(ToolBarManager manager, final RichTextEditor editor) {
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
		// if (!getEditor().isReadOnly()) {
		// manager.add(getMaximizePartAction());
		// }
		// super.fillToolBar(manager);
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

	protected void createAdditionalControl(Composite composite) {
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

	protected void processFields() {
		newComment = commentText.getText();
		if (defect) { // process custom field selection only when defect is selected
			for (CustomFieldDef field : customCombos.keySet()) {
				CustomFieldValue customValue = (CustomFieldValue) customCombos.get(field).getElementAt(
						customCombos.get(field).getCombo().getSelectionIndex());
				if (customValue != null) {
					CustomFieldBean bean = new CustomFieldBean();
					bean.setConfigVersion(field.getConfigVersion());
					bean.setValue(customValue.getName());
					customFieldSelections.put(field.getName(), bean);
				}
			}
		}
	}

	private int addCustomFields(Composite parent) {
		if (review == null) {
			return 0;
		}
		List<CustomFieldDef> customFields = CrucibleCorePlugin.getDefault().getReviewCache().getMetrics(
				review.getMetricsVersion());
		if (customFields == null) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleCorePlugin.PLUGIN_ID,
					"Metrics are for review version are not cached: " + review.getMetricsVersion() + " "
							+ review.getName(), null));
			return 0;
		} else {
			for (CustomFieldDef customField : customFields) {
				createCombo(parent, customField, 0);
			}
			return customFields.size();
		}
	}

	protected Button createDefectButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		defectButton = new Button(parent, SWT.CHECK);
		defectButton.setText(DEFECT_LABEL);
		defectButton.setFont(JFaceResources.getDialogFont());
		defectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				defect = !defect;
				// toggle combos
				for (CustomFieldDef field : customCombos.keySet()) {
					customCombos.get(field).getCombo().setEnabled(defect);
				}
			}
		});
		return defectButton;
	}

	protected void createCombo(Composite parent, final CustomFieldDef customField, int selection) {
		((GridLayout) parent.getLayout()).numColumns++;
		Label label = new Label(parent, SWT.NONE);
		label.setText("Select " + customField.getName());
		((GridLayout) parent.getLayout()).numColumns++;
		ComboViewer comboViewer = new ComboViewer(parent);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				CustomFieldValue fieldValue = (CustomFieldValue) element;
				return fieldValue.getName();
			}
		});
		comboViewer.setInput(customField.getValues());
		comboViewer.getCombo().setEnabled(false);
		customCombos.put(customField, comboViewer);
	}

	public boolean addComment() {
		try {
			newComment = commentText.getText();
			processFields();
			setMessage("");
			run(true, false, new AddCommentRunnable());
		} catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to add the comment to the review");
			return false;
		} catch (InterruptedException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
			setErrorMessage("Unable to add the comment to the review");
			return false;
		}

		setReturnCode(Window.OK);
		close();
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		saveButton = createButton(parent, IDialogConstants.CLIENT_ID + 2, SAVE_LABEL, false);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addComment();
			}
		});
		saveButton.setEnabled(false);
		if (!edit) { // if it is a new reply, saving as draft is possible
			saveDraftButton = createButton(parent, IDialogConstants.CLIENT_ID + 2, DRAFT_LABEL, false);
			saveDraftButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					draft = true;
					addComment();
				}
			});
			saveDraftButton.setEnabled(false);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						cancelPressed();
					}
				});
	}

	public void cancelAddComment() {
		setReturnCode(Window.CANCEL);
		close();
	}

	public String getTaskKey() {
		return taskKey;
	}

	public String getTaskId() {
		return taskId;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public void setReviewItem(CrucibleFile reviewItem) {
		this.crucibleFile = reviewItem;
	}

	public void setParentComment(Comment comment) {
		this.parentComment = comment;
	}

	public void setCommentLines(LineRange commentLines2) {
		this.commentLines = commentLines2;
	}

	protected void setReview(Review review) {
		this.review = review;
	}

}
