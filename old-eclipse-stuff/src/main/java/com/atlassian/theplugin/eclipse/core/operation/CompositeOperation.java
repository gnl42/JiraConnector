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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import com.atlassian.theplugin.eclipse.util.ProgressMonitorUtility;

/**
 * Composite operation provide way to combine different operations
 * 
 * @author Alexander Gurov
 */
public class CompositeOperation extends AbstractActionOperation implements
		IConsoleStream {
	protected List<Pair> operations;
	protected boolean checkWarnings;

	public CompositeOperation(String operationName) {
		this(operationName, false);
	}

	public CompositeOperation(String operationName, boolean checkWarnings) {
		super(operationName);
		this.operations = new ArrayList<Pair>();
		this.checkWarnings = checkWarnings;
	}

	public void add(IActionOperation operation) {
		this.add(operation, null);
	}

	public void add(IActionOperation operation,
			IActionOperation[] dependsOnOperation) {
		operation.setConsoleStream(this);
		this.operations.add(new Pair(operation, dependsOnOperation));
	}

	public void remove(IActionOperation operation) {
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext();) {
			Pair pair = (Pair) it.next();
			if (pair.operation == operation) {
				if (operation.getConsoleStream() == this) {
					operation.setConsoleStream(null);
				}
				it.remove();
				break;
			}
		}
	}

	public ISchedulingRule getSchedulingRule() {
		ISchedulingRule retVal = null;
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext();) {
			Pair pair = (Pair) it.next();
			retVal = MultiRule.combine(retVal, pair.operation
					.getSchedulingRule());
		}
		return retVal;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		int j = 0;
		for (Iterator<Pair> it = this.operations.iterator(); it.hasNext()
				&& !monitor.isCanceled();) {
			Pair pair = (Pair) it.next();

			boolean errorFound = false;
			if (pair.dependsOnOperation != null) {
				for (int i = 0; i < pair.dependsOnOperation.length; i++) {
					if (pair.dependsOnOperation[i].getStatus().getSeverity() == IStatus.ERROR
							|| this.checkWarnings
							&& pair.dependsOnOperation[i].getStatus()
									.getSeverity() == IStatus.WARNING
							|| pair.dependsOnOperation[i].getExecutionState() == IActionOperation.NOTEXECUTED) {
						errorFound = true;
						break;
					}
				}
			}
			if (!errorFound) {
				ProgressMonitorUtility.doTask(pair.operation, monitor,
						this.operations.size());
				this.reportStatus(pair.operation.getStatus());
				ProgressMonitorUtility.progress(monitor, j++, this.operations
						.size());
			}
		}
	}

	protected class Pair {
		public IActionOperation operation;
		public IActionOperation[] dependsOnOperation;

		public Pair(IActionOperation operation,
				IActionOperation[] dependsOnOperation) {
			this.operation = operation;
			this.dependsOnOperation = dependsOnOperation;
		}
	}

	public void markEnd() {
		// do nothing
	}

	public void markStart(String data) {
		// do nothing
	}

	public void write(int severity, String data) {
		this.writeToConsole(severity, data);
	}

	public void doComplexWrite(Runnable runnable) {
		this.complexWriteToConsole(runnable);
	}

	public void markCancelled() {
		this.writeCancelledToConsole();
	}

}
