package com.atlassian.theplugin.eclipse.editors.bamboo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.atlassian.theplugin.eclipse.preferences.Activator;

public class BuildLogEditor extends EditorPart {

	private Text text;

	public BuildLogEditor() {
		super();
		//setDocumentProvider(new TextFileDocumentProvider());
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setBackground(Activator.getDefault().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		if (getEditorInput() != null) {
			BuildLogEditorInput editorInput = (BuildLogEditorInput) getEditorInput();
			text.setText(editorInput.getLog());
			setPartName(editorInput.getName());
		}
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		
		if (text != null) {
			BuildLogEditorInput editorInput = (BuildLogEditorInput) getEditorInput();
			text.setText(editorInput.getLog());
			setPartName(editorInput.getName());
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	@Override
	public String getTitleToolTip() {
		if (getEditorInput() != null) {
			return ((BuildLogEditorInput) getEditorInput()).getToolTipText();
		}
		
		return "";
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}
	
	@Override
	public void doSaveAs() {
	}

}
