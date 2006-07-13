package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.mylar.internal.jira.JiraTask;
import org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory;
import org.eclipse.mylar.internal.tasklist.ui.editors.MylarTaskEditor;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class JiraTaskEditorFactory implements ITaskEditorFactory {

	public JiraTaskEditorFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#canCreateEditorFor(org.eclipse.mylar.provisional.tasklist.ITask)
	 */
	public boolean canCreateEditorFor(ITask task) {
		return task instanceof JiraTask;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#createEditor(org.eclipse.mylar.internal.tasklist.ui.editors.MylarTaskEditor)
	 */
	public IEditorPart createEditor(MylarTaskEditor parentEditor) {
		return new JiraTaskEditor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#createEditorInput(org.eclipse.mylar.provisional.tasklist.ITask)
	 */
	public IEditorInput createEditorInput(ITask task) {
		return new JiraIssueEditorInput((JiraTask) task);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#getTitle()
	 */
	public String getTitle() {
		return "Jira";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#notifyEditorActivationChange(org.eclipse.ui.IEditorPart)
	 */
	public void notifyEditorActivationChange(IEditorPart editor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.internal.tasklist.ui.ITaskEditorFactory#providesOutline()
	 */
	public boolean providesOutline() {
		return true;
	}

}
