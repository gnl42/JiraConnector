/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.forms.widgets.*;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.tigris.jira.core.model.Comment;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Brock Janiczak
 * @author Mik Kersten (minor fixes)
 */
public class JiraTaskEditor extends EditorPart {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

	private JiraIssueEditorInput input;

	private Text comment;

	private boolean isDirty = false;

	private Map<Comment, ExpandableComposite> commentToControlMap = new HashMap<Comment, ExpandableComposite>();

	private JiraServer server;

	private Issue issue;

	private ScrolledComposite sc;

	private Section commentsSection;

	public JiraTaskEditor() {
		// commentImage =
		// MylarJiraPlugin.getImageDescriptor("icons/ctool16/comment.gif").createImage();
		// commentFont =
		// JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	}

	public void doSave(IProgressMonitor monitor) {
		server.addCommentToIssue(issue, comment.getText());
		comment.setText("");
		isDirty = false;
//		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//
//			public void run() {
//				JiraTaskEditor.this.getSite().getPage().closeEditor(JiraTaskEditor.this, false);
//			} });
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		this.input = (JiraIssueEditorInput) input;
		setSite(site);
		setInput(input);
		setPartName(this.input.getName());
		JiraTask task = this.input.getTask();
		TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(task.getRepositoryKind(),
				task.getRepositoryUrl());
		server = JiraServerFacade.getDefault().getJiraServer(repository);
		String key = task.getKey();
		if (key == null || key.trim().equals("")) {
			throw new PartInitException("Could not find issue key, synchronize query to resolve.");
		} else {
			issue = server.getIssue(key);
		}
	}

	public boolean isDirty() {
		if (comment != null) {
			int charCount = comment.getCharCount();
			isDirty = charCount > 0;
			return isDirty;
		} else {
			return false;
		}
	}

	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(getSite().getShell().getDisplay());

		Form form = toolkit.createForm(parent);
		form.setText(issue.getKey() + ": " + issue.getSummary());

		GridLayout formLayout = new GridLayout(1, true);

		final Composite formBody = form.getBody();
		formBody.setLayout(formLayout);

		Section summarySection = toolkit.createSection(formBody, ExpandableComposite.TITLE_BAR
				| ExpandableComposite.TWISTIE);
		summarySection.setText("Attributes");
		summarySection.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		summarySection.setExpanded(true);
		Composite summary = toolkit.createComposite(summarySection);
		summary.setLayout(new GridLayout(6, false));

		Label lblCreated = toolkit.createLabel(summary, "Created:");
		lblCreated.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, DATE_FORMAT.format(issue.getCreated()));

		Label lblUpdated = toolkit.createLabel(summary, "Updated:");
		lblUpdated.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, DATE_FORMAT.format(issue.getUpdated()));

		if (issue.getDue() != null) {
			Label lblDue = toolkit.createLabel(summary, "Due:");
			lblDue.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			toolkit.createLabel(summary, DATE_FORMAT.format(issue.getDue()));
		} else {
			Label spacer = toolkit.createLabel(summary, "");
			spacer.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		}

		Label lblStatus = toolkit.createLabel(summary, "Status:");
		lblStatus.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, issue.getStatus().getName());

		Label lblResolution = toolkit.createLabel(summary, "Resolution:");
		lblResolution.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		if (issue.getResolution() != null) {
			Label resolution = toolkit.createLabel(summary, issue.getResolution().getName());
			resolution.setToolTipText(issue.getResolution().getDescription());
		} else {
			toolkit.createLabel(summary, "Unresolved");
		}

		Label lblPriority = toolkit.createLabel(summary, "Priority:");
		lblPriority.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		if (issue.getPriority() != null) {
			Label priority = toolkit.createLabel(summary, issue.getPriority().getName());
			priority.setToolTipText(issue.getPriority().getDescription());
		} else {
			toolkit.createLabel(summary, "No Priority");
		}

		Label lblReporter = toolkit.createLabel(summary, "Reporter:");
		lblReporter.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, issue.getReporter());

		Label lblAssignee = toolkit.createLabel(summary, "Assignee");
		lblAssignee.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, issue.getAssignee());

		// spacer
		Label spacer2 = toolkit.createLabel(summary, "");
		spacer2.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());

		StringBuffer sbComponents = new StringBuffer();
		if (issue.getComponents().length > 0) {
			for (int i = 0; i < issue.getComponents().length; i++) {
				if (i != 0) {
					sbComponents.append(", ");
				}

				sbComponents.append(issue.getComponents()[i].getName());
			}
		} else {
			sbComponents.append("None");
		}

		Label lblComponents = toolkit.createLabel(summary, "Components");
		lblComponents.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, sbComponents.toString());

		StringBuffer sbAffectsVersions = new StringBuffer();
		if (issue.getReportedVersions().length > 0) {
			for (int i = 0; i < issue.getReportedVersions().length; i++) {
				if (i != 0) {
					sbAffectsVersions.append(", ");
				}

				sbAffectsVersions.append(issue.getReportedVersions()[i].getName());
			}
		} else {
			sbAffectsVersions.append("None");
		}

		Label lblAffectsVersion = toolkit.createLabel(summary, "Affects Versions");
		lblAffectsVersion.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, sbAffectsVersions.toString());

		StringBuffer sbFixVersions = new StringBuffer();
		if (issue.getFixVersions().length > 0) {
			for (int i = 0; i < issue.getFixVersions().length; i++) {
				if (i != 0) {
					sbFixVersions.append(", ");
				}

				sbFixVersions.append(issue.getFixVersions()[i].getName());
			}
		} else {
			sbFixVersions.append("None");
		}

		Label lblFixVersions = toolkit.createLabel(summary, "Fix Versions");
		lblFixVersions.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		toolkit.createLabel(summary, sbFixVersions.toString());

		summarySection.setClient(summary);

		// created, updated, due (optional)

		final Section descriptionSection = toolkit.createSection(formBody, ExpandableComposite.TITLE_BAR
				| ExpandableComposite.TWISTIE);
		descriptionSection.setExpanded(true);
		descriptionSection.setText("Description");
		final GridData dgd = GridDataFactory.fillDefaults().grab(true, false).create();
		descriptionSection.setLayoutData(dgd);

		Composite c = toolkit.createComposite(descriptionSection);
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);

		Text description = toolkit.createText(c, issue.getDescription(), SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		description.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).minSize(200, 80).hint(200, 80)
				.create());

		description.setText(issue.getDescription());
		description.setEditable(false);
		description.setFont(JFaceResources.getTextFont());
		descriptionSection.setClient(c);

		commentsSection = toolkit.createSection(formBody, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		commentsSection.setExpanded(true);
		ImageHyperlink sortOrder = toolkit.createImageHyperlink(commentsSection, SWT.NONE);
		sortOrder.setText("Direction");

		commentsSection.setTextClient(sortOrder);
		commentsSection.setText("Comments");
		commentsSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		sc = new ScrolledComposite(commentsSection, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite c1 = toolkit.createComposite(sc);
		GridLayout commentsLayout = new GridLayout(1, true);
		commentsLayout.marginWidth = 0;
		commentsLayout.marginHeight = 0;

		c1.setLayout(commentsLayout);

		for (int i = 0; i < issue.getComments().length; i++) {
			Comment comment = issue.getComments()[i];

			ExpandableComposite expandableComposite = toolkit.createExpandableComposite(c1,
					ExpandableComposite.TREE_NODE);
			expandableComposite.setText("Comment by " + comment.getAuthor() + " ["
					+ DATE_FORMAT.format(comment.getCreated()) + "]");
			expandableComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
			expandableComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			Text t = toolkit.createText(expandableComposite, comment.getComment(), SWT.MULTI | SWT.WRAP);
			t.setEditable(false);
			t.setFont(JFaceResources.getTextFont());

			expandableComposite.setClient(t);
			expandableComposite.addExpansionListener(new ExpansionAdapter() {

				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					sc.layout(true, true);
				}

			});
			commentToControlMap.put(comment, expandableComposite);

			t.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

			if (i == issue.getComments().length - 1) {
				expandableComposite.setExpanded(true);
			}

		}

		commentsSection.setClient(sc);

		sc.addControlListener(new ControlAdapter() {

			public void controlResized(ControlEvent e) {
				Point size = c1.computeSize(sc.getClientArea().width, SWT.DEFAULT);
				sc.setMinSize(size);
			}

		});

		final Section commentSection = toolkit.createSection(formBody, ExpandableComposite.TWISTIE
				| ExpandableComposite.TITLE_BAR);
		commentSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		commentSection.setText("Add Comment");
		commentSection.setExpanded(false);
		commentSection.addExpansionListener(new ExpansionAdapter() {

			public void expansionStateChanged(ExpansionEvent e) {
				formBody.layout(true);
			}
		});

		final Composite commentArea = toolkit.createComposite(commentSection);
		GridLayout commentAreaLayout = new GridLayout(1, false);
		commentAreaLayout.marginHeight = 0;
		commentAreaLayout.marginWidth = 0;
		commentAreaLayout.verticalSpacing = 0;
		commentAreaLayout.horizontalSpacing = 0;
		commentArea.setLayout(commentAreaLayout);

		commentSection.setClient(commentArea);

		comment = new Text(commentArea, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		comment.setFont(JFaceResources.getTextFont());
		final GridData commentGd = new GridData(SWT.FILL, SWT.FILL, true, false);
		commentGd.heightHint = 80;
		comment.setLayoutData(commentGd);

		comment.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				int charCount = comment.getCharCount();
				if ((isDirty && charCount == 0) || (!isDirty && charCount > 0)) {
					firePropertyChange(PROP_DIRTY);
				}
			}

		});
		sc.setContent(c1);
		sc.setMinSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@SuppressWarnings("restriction")
	public void revealComment(Comment comment) {
		Control control = (Control) this.commentToControlMap.get(comment);
		if (control != null) {
			commentsSection.setExpanded(true);
			((ExpandableComposite) control).setExpanded(true);
			// XXX Clone or create a new version of this
			FormUtil.ensureVisible(sc, control);
			sc.layout(true, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		// commentImage.dispose();

		// Don't dispose JFace resources
		// commentFont.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			return new JiraIssueContentOutlinePage(this, this.issue);
		}

		return super.getAdapter(adapter);
	}

}
