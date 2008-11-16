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

import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * Abstract implementation of the operation that does not require locking of any other operation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractNonLockingOperation extends AbstractActionOperation {
	public static NonLockingRule NON_LOCKING_RULE = new NonLockingRule();
	
	public AbstractNonLockingOperation(String operationName) {
		super(operationName);
	}

	public ISchedulingRule getSchedulingRule() {
		return AbstractNonLockingOperation.NON_LOCKING_RULE;
	}
	
	public static class NonLockingRule implements ISchedulingRule {
		public boolean isConflicting(ISchedulingRule rule) {
			return this == rule;
		}
		
		public boolean contains(ISchedulingRule rule) {
			if (rule == null) {
				// this avoid a NullPointerException
				return false;
			}
			return NonLockingRule.class.equals(rule.getClass());
		}
		
	}
	
}
