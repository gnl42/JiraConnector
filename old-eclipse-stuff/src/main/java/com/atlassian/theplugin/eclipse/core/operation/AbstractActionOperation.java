/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.core.operation;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.atlassian.theplugin.eclipse.core.ClientWrapperCancelException;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.ProgressMonitorUtility;

/**
 * Abstract IActionOperation implementation provides default implementation of
 * the status processing and console support
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractActionOperation implements IActionOperation {
	private final MultiStatus status = new MultiStatus(Activator.PLUGIN_ID,
			IStatus.OK, "", null);
	protected String nameId;
	protected String name;
	protected boolean isExecuted;
	protected IConsoleStream consoleStream;

	public AbstractActionOperation(String operationName) {
		this.isExecuted = false;
		this.setOperationName(operationName);
		this.updateStatusMessage();
	}

	public IConsoleStream getConsoleStream() {
		return this.consoleStream;
	}

	public void setConsoleStream(IConsoleStream stream) {
		this.consoleStream = stream;
	}

	public IStatus getStatus() {
		return this.status;
	}

	public int getExecutionState() {
		if (this.status.isOK()) {
			return this.isExecuted ? IActionOperation.OK
					: AbstractActionOperation.NOTEXECUTED;
		} else {
			return AbstractActionOperation.ERROR;
		}
	}

	public final IActionOperation run(IProgressMonitor monitor) {
		try {
			this.updateStatusMessage();
			this.isExecuted = true;
			if (this.consoleStream != null) {
				this.consoleStream.markStart(this.name);
			}
			this.runImpl(monitor);
			if (monitor.isCanceled()) {
				boolean hasCanceledException = false;
				// We have a multi status, so we iterate the children
				IStatus[] children = this.status.getChildren();
				for (int i = 0; i < children.length; i++) {
					Throwable exception = children[i].getException();
					if (exception instanceof ClientWrapperCancelException
							|| exception instanceof ActivityCancelledException) {
						hasCanceledException = true;
						break;
					}
				}
				if (!hasCanceledException) {
					throw new ActivityCancelledException();
				}
			}
		} catch (Throwable t) {
			this.reportError(t);
		} finally {
			if (this.consoleStream != null) {
				this.consoleStream.markEnd();
			}
		}

		return this;
	}

	public void setOperationName(String name) {
		this.nameId = name;
		this.name = this.getNationalizedString(name);
	}

	public String getOperationName() {
		return this.name;
	}

	public String getId() {
		return this.nameId;
	}

	protected abstract void runImpl(IProgressMonitor monitor) throws Exception;

	protected void writeCancelledToConsole() {
		if (this.consoleStream != null) {
			this.consoleStream.markCancelled();
		}
	}

	protected void writeToConsole(int severity, String data) {
		if (this.consoleStream != null) {
			this.consoleStream.write(severity, data);
		}
	}

	protected void complexWriteToConsole(Runnable runnable) {
		if (this.consoleStream != null) {
			this.consoleStream.doComplexWrite(runnable);
		}
	}

	protected void protectStep(IUnprotectedOperation step,
			IProgressMonitor monitor, int subTaskCnt) {
		try {
			ProgressMonitorUtility.doSubTask(this, step, monitor, subTaskCnt);
		} catch (Throwable t) {
			this.reportError(t);
		}
	}

	protected void reportError(Throwable t) {
		if (t instanceof ClientWrapperCancelException
				|| t instanceof ActivityCancelledException) {
			this.writeCancelledToConsole();
		} else {
			this.writeToConsole(IConsoleStream.LEVEL_ERROR,
					(t.getMessage() != null ? t.getMessage() : this
							.getShortErrorMessage(t))
							+ "\n");
		}

		this.reportStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				IStatus.OK, this.getShortErrorMessage(t), t));
	}

	protected String getShortErrorMessage(Throwable t) {
		String key = this.nameId + ".Error";
		String retVal = this.getNationalizedString(key);
		if (retVal.equals(key)) {
			return this.status.getMessage() + ": " + t.getMessage();
		}
		return retVal;
	}

	protected void reportStatus(IStatus st) {
		if (st.getSeverity() != IStatus.OK) {
			this.status.merge(st);
		}
	}

	protected String getOperationResource(String key) {
		return this.getNationalizedString(this.nameId + "." + key);
	}

	protected final String getNationalizedString(String key) {
		String retVal = Activator.getDefault().getResource(key);
		/*
		 * if (retVal.equals(key)) { return
		 * CoreExtensionsManager.instance().getOptionProvider
		 * ().getResource(key); }
		 */
		return retVal;
	}

	private void updateStatusMessage() {
		String errMessage = Activator.getDefault().getResource(
				"Operation.Error.LogHeader");
		String key = this.nameId + ".Id";
		String prefix = this.getNationalizedString(key);
		prefix = prefix.equals(key) ? "" : (prefix + ": ");
		this.status.setMessage(MessageFormat.format(errMessage,
				new Object[] { prefix + this.name }));
	}

	/**
	 * Give us an accessible version of MultiStatus to be able to update the
	 * message.
	 * 
	 * @author Alessandro Nistico
	 */
	private static class MultiStatus extends
			org.eclipse.core.runtime.MultiStatus {
		public MultiStatus(String pluginId, int code, IStatus[] newChildren,
				String message, Throwable exception) {
			super(pluginId, code, newChildren, message, exception);
		}

		public MultiStatus(String pluginId, int code, String message,
				Throwable exception) {
			super(pluginId, code, message, exception);
		}

		public void setMessage(String message) {
			super.setMessage(message);
		}
	}

}
