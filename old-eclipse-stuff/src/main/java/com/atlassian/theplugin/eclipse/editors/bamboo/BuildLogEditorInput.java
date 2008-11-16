package com.atlassian.theplugin.eclipse.editors.bamboo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class BuildLogEditorInput implements IEditorInput {

	private String log;
	private String title;

	public BuildLogEditorInput(String log, String title) {
		this.log = log;
		this.title = title;
	}
	
	public String getLog() {
		return log;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return title;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Log for build " + getName();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
