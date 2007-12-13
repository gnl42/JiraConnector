/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylyn.internal.tasks.ui.TaskListColorsAndFonts;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorActionContributor;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.RepositoryTaskData;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.DatePicker;
import org.eclipse.mylyn.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskTextViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.themes.IThemeManager;

@SuppressWarnings("restriction")
// TODO move to tasks ui
public class TaskUiFactory {

	public interface DateExternalizer {

		public Date toDate(String value);

		public String toString(Date value);

	}

	private static final Font TEXT_FONT = JFaceResources.getDefaultFont();

	private final FormToolkit toolkit;

	private final JiraTaskEditor editor;

	private Color colorIncoming;

	// TODO remove dependency on editor
	public TaskUiFactory(FormToolkit toolkit, JiraTaskEditor editor) {
		this.toolkit = toolkit;
		this.editor = editor;

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		colorIncoming = themeManager.getCurrentTheme().getColorRegistry().get(
				TaskListColorsAndFonts.THEME_COLOR_TASKS_INCOMING_BACKGROUND);

	}

	public Label createLabel(Composite composite, RepositoryTaskAttribute attribute) {
		Label label;
		if (hasOutgoingChange(attribute)) {
			label = toolkit.createLabel(composite, "*" + attribute.getName());
		} else {
			label = toolkit.createLabel(composite, attribute.getName());
		}
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		return label;
	}

	private boolean hasOutgoingChange(RepositoryTaskAttribute attribute) {
		return ((RepositoryTaskEditorInput) editor.getEditorInput()).getOldEdits().contains(attribute);
	}

	public CCombo createCCombo(Composite attributesComposite, final RepositoryTaskAttribute attribute) {
		final CCombo combo = new CCombo(attributesComposite, SWT.FLAT | SWT.READ_ONLY);
		toolkit.adapt(combo, true, true);
		combo.setFont(TEXT_FONT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

		GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.horizontalSpan = 1;
		data.widthHint = 140;
		combo.setLayoutData(data);

		if (attribute.getOptions() != null) {
			for (String val : attribute.getOptions()) {
				combo.add(val);
			}
		}

		String value = attribute.getValue();
		if (value == null) {
			value = "";
		}
		if (combo.indexOf(value) != -1) {
			combo.select(combo.indexOf(value));
		}
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (combo.getSelectionIndex() > -1) {
					String sel = combo.getItem(combo.getSelectionIndex());
					attribute.setValue(sel);
					attributeChanged(attribute);
				}

			}
		});

		if (hasChanged(attribute)) {
			combo.setBackground(getColorIncoming());
		}
		return combo;

	}

	public List createList(Composite attributesComposite, final RepositoryTaskAttribute attribute) {
		final List list = new List(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL);
		toolkit.adapt(list, true, true);
		list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		list.setFont(TEXT_FONT);

		GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.horizontalSpan = 1;
		data.widthHint = 125;
		data.heightHint = 45;
		list.setLayoutData(data);

		if (!attribute.getOptions().isEmpty()) {
			list.setItems(attribute.getOptions().toArray(new String[1]));
			for (String value : attribute.getValues()) {
				list.select(list.indexOf(value));
			}
			final RepositoryTaskAttribute attr = attribute;
			list.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					attr.clearValues();
					attr.setValues(Arrays.asList(list.getSelection()));
					attributeChanged(attr);
				}
			});
			list.showSelection();
		}

		if (hasChanged(attribute)) {
			list.setBackground(getColorIncoming());
		}
		return list;
	}

	public TextViewer createLink(Composite attributesComposite, TaskRepository repository,
			final RepositoryTaskAttribute attribute) {
		TextViewer viewer = createTextViewer(repository, attributesComposite, attribute.getValue(), SWT.FLAT
				| SWT.READ_ONLY);

		StyledText text = viewer.getTextWidget();

		GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.horizontalSpan = 1;
		data.widthHint = 135;
		text.setLayoutData(data);

		if (hasChanged(attribute)) {
			text.setBackground(getColorIncoming());
		}

		return viewer;
	}

	public Text createText(Composite attributesComposite, final RepositoryTaskAttribute attribute) {
		int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;
		Text text = createTextField(attributesComposite, attribute, SWT.FLAT | style);

		GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.horizontalSpan = 1;
		data.widthHint = 135;
		text.setLayoutData(data);

		if (hasContentAssist(attribute)) {
			ContentAssistCommandAdapter adapter = editor.applyContentAssist(text,
					editor.createContentProposalProvider(attribute));

			ILabelProvider propsalLabelProvider = editor.createProposalLabelProvider(attribute);
			if (propsalLabelProvider != null) {
				adapter.setLabelProvider(propsalLabelProvider);
			}
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}

		if (hasChanged(attribute)) {
			text.setBackground(getColorIncoming());
		}
		return text;
	}

	private boolean hasContentAssist(RepositoryTaskAttribute attribute) {
		return editor.hasContentAssist(attribute);
	}

	protected Text createTextField(Composite composite, RepositoryTaskAttribute attribute, int style) {
		String value;
		if (attribute == null || attribute.getValue() == null) {
			value = "";
		} else {
			value = attribute.getValue();
		}

		final Text text;
		if ((SWT.READ_ONLY & style) == SWT.READ_ONLY) {
			text = new Text(composite, style);
			toolkit.adapt(text, true, true);
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setText(value);
		} else {
			text = toolkit.createText(composite, value, style);
		}

		if (attribute != null && !attribute.isReadOnly()) {
			text.setData(attribute);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String newValue = text.getText();
					RepositoryTaskAttribute attribute = (RepositoryTaskAttribute) text.getData();
					attribute.setValue(newValue);
					attributeChanged(attribute);
				}
			});
		}
		if (hasChanged(attribute)) {
			text.setBackground(colorIncoming);
		}
		return text;
	}

	private Color getColorIncoming() {
		return colorIncoming;
	}

	private boolean hasChanged(RepositoryTaskAttribute newAttribute) {
		if (newAttribute == null)
			return false;
		RepositoryTaskData oldTaskData = ((RepositoryTaskEditorInput) editor.getEditorInput()).getOldTaskData();
		if (oldTaskData == null)
			return false;

		if (hasOutgoingChange(newAttribute)) {
			return false;
		}

		RepositoryTaskAttribute oldAttribute = oldTaskData.getAttribute(newAttribute.getId());
		if (oldAttribute == null)
			return true;
		if (oldAttribute.getValue() != null && !oldAttribute.getValue().equals(newAttribute.getValue())) {
			return true;
		} else if (oldAttribute.getValues() != null && !oldAttribute.getValues().equals(newAttribute.getValues())) {
			return true;
		}
		return false;
	}

	protected TextViewer createTextViewer(TaskRepository repository, Composite composite, String text, int style) {
		final TaskEditorActionContributor actionContributor = ((TaskEditor) editor.getEditor()).getContributor();

		final RepositoryTextViewer commentViewer = new RepositoryTextViewer(repository, composite, style);

		// NOTE: Configuration must be applied before the document is set in
		// order for
		// Hyperlink colouring to work. (Presenter needs document object up
		// front)
		TaskTextViewerConfiguration repositoryViewerConfig = new TaskTextViewerConfiguration(false);
		commentViewer.configure(repositoryViewerConfig);

		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();

		commentViewer.getTextWidget().setFont(
				themeManager.getCurrentTheme().getFontRegistry().get(TaskListColorsAndFonts.TASK_EDITOR_FONT));

		commentViewer.addSelectionChangedListener(actionContributor);

		commentViewer.getTextWidget().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

				actionContributor.updateSelectableActions(commentViewer.getSelection());

			}

			public void focusLost(FocusEvent e) {
				StyledText st = (StyledText) e.widget;
				st.setSelectionRange(st.getCaretOffset(), 0);
				actionContributor.forceActionsEnabled();
			}
		});

		commentViewer.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				actionContributor.updateSelectableActions(commentViewer.getSelection());
			}
		});

		commentViewer.setEditable(false);
		commentViewer.getTextWidget().setMenu(editor.getManagedForm().getForm().getMenu());
		Document document = new Document(text);
		commentViewer.setDocument(document);

		addTextViewer(commentViewer);
		return commentViewer;
	}

	private void addTextViewer(RepositoryTextViewer commentViewer) {
		editor.addTextViewer(commentViewer);
	}

	public void createDatePicker(Composite composite, final RepositoryTaskAttribute attribute,
			final DateExternalizer externalizer) {
		Composite dateWithClear = toolkit.createComposite(composite);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 1;
		dateWithClear.setLayout(layout);

		String value = "";
		Date date = externalizer.toDate(attribute.getValue());
		if (date != null) {
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			value = f.format(date);
		}

		final DatePicker deadlinePicker = new DatePicker(dateWithClear, /* SWT.NONE */SWT.BORDER, value);
		deadlinePicker.setEnabled(!attribute.isReadOnly());
		deadlinePicker.setFont(TEXT_FONT);
		deadlinePicker.setDatePattern("yyyy-MM-dd");
		if (hasChanged(attribute)) {
			deadlinePicker.setBackground(getColorIncoming());
		}
		deadlinePicker.addPickerSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}

			public void widgetSelected(SelectionEvent e) {
				Calendar cal = deadlinePicker.getDate();
				if (cal != null) {
					Date d = cal.getTime();

					attribute.setValue(externalizer.toString(d));
					attributeChanged(attribute);
					// TODO goes dirty even if user presses cancel
					// markDirty(true);
				} else {
					attribute.setValue("");
					attributeChanged(attribute);
					deadlinePicker.setDate(null);
				}
			}
		});

		ImageHyperlink clearDeadlineDate = toolkit.createImageHyperlink(dateWithClear, SWT.NONE);
		clearDeadlineDate.setImage(TasksUiImages.getImage(TasksUiImages.REMOVE));
		clearDeadlineDate.setToolTipText("Clear");
		clearDeadlineDate.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				attribute.setValue("");
				attributeChanged(attribute);
				deadlinePicker.setDate(null);
			}

		});
	}

	private void attributeChanged(RepositoryTaskAttribute attribute) {
		editor.attributeChanged(attribute);
	}

	public void createLinkList(Composite attributesComposite, TaskRepository repository, RepositoryTaskAttribute attribute) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String key : attribute.getValues()) {
			sb.append(sep).append(key);
			sep = ", ";
		}
		TextViewer viewer = createTextViewer(repository, attributesComposite, sb.toString(), SWT.FLAT | SWT.MULTI
				| SWT.READ_ONLY | SWT.WRAP);

		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		data.horizontalSpan = 3;
		data.widthHint = 380;
		data.heightHint = 20;

		StyledText text = viewer.getTextWidget();
		text.setLayoutData(data);

		toolkit.adapt(text, true, true);

		if (hasChanged(attribute)) {
			text.setBackground(getColorIncoming());
		}		
	}

	public StyledText createTextArea(Composite attributesComposite, RepositoryTaskAttribute attribute) {
		Label label = createLabel(attributesComposite, attribute);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

		int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;

		// TextViewer viewer = addTextEditor(repository,
		// attributesComposite, attribute.getValue(), true, SWT.FLAT |
		// SWT.BORDER | SWT.MULTI | SWT.WRAP | style);
		TextViewer viewer = new TextViewer(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
				| style);
		viewer.setDocument(new Document(attribute.getValue()));

		final StyledText text = viewer.getTextWidget();

		// GridDataFactory.fillDefaults().span(3, 1).hint(300,
		// 40).applyTo(text);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		data.horizontalSpan = 3;
		data.widthHint = 380;
		data.heightHint = 55;
		text.setLayoutData(data);

		toolkit.adapt(text, true, true);

		if (attribute.isReadOnly()) {
			viewer.setEditable(false);
		} else {
			viewer.setEditable(true);
			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			text.setData(attribute);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String newValue = text.getText();
					RepositoryTaskAttribute attribute = (RepositoryTaskAttribute) text.getData();
					attribute.setValue(newValue);
					attributeChanged(attribute);
				}
			});
		}

		if (hasChanged(attribute)) {
			text.setBackground(getColorIncoming());
		}
		
		return text;
	}


}
