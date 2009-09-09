/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.subclipse.core;

import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.LocalResource;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;


/**
 * Resource state filter interface and most useful implementations
 * 
 * @author Alexander Gurov
 */
public interface IStateFilter {

	boolean accept(IResource resource, LocalResourceStatus state);
	
	boolean accept(ISVNLocalResource resource) throws SVNException;
	
	boolean allowsRecursion(ISVNLocalResource resource) throws SVNException;

	boolean allowsRecursion(IResource resource, LocalResourceStatus state);

	public abstract class AbstractStateFilter implements IStateFilter {
		public boolean accept(ISVNLocalResource resource) throws SVNException {
			return resource.getStatus() != LocalResourceStatus.NONE && this.acceptImpl(resource, resource.getResource(), resource.getStatus());
		}
		public boolean accept(IResource resource, LocalResourceStatus state) {
			return state != LocalResourceStatus.NONE && this.acceptImpl(null, resource, state);
		}
		public boolean allowsRecursion(ISVNLocalResource resource) throws SVNException {
			return resource.getStatus() != LocalResourceStatus.NONE && this.allowsRecursionImpl(null, resource.getResource(), resource.getStatus());
		}
		public boolean allowsRecursion(IResource resource, LocalResourceStatus state) {
			return state != LocalResourceStatus.NONE && this.allowsRecursionImpl(null, resource, state);
		}
		
		protected abstract boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state);
		protected abstract boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state);
		
		protected ISVNLocalResource takeLocal(LocalResource local, IResource resource) {
			return local != null ? local : SVNWorkspaceRoot.getSVNResourceFor(resource); 
		}
	}
	
	public static final IStateFilter SF_UNVERSIONED = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return state.isUnversioned();
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return true;
		}
	};
	
	public static final IStateFilter SF_IGNORED = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return state.isIgnored() || IStateFilter.SF_UNVERSIONED.accept(resource, state); 
		}

		@Override
		public boolean allowsRecursionImpl(ISVNLocalResource local,
				IResource resource, LocalResourceStatus state) {
			return true;
		}
	};
	
	public static final IStateFilter SF_ANY_CHANGE = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			final SVNStatusKind kind = state.getStatusKind();
			return 
				!IStateFilter.SF_IGNORED.accept(resource, state) && !kind.equals(SVNStatusKind.NORMAL) && 
				!kind.equals(SVNStatusKind.OBSTRUCTED) && !kind.equals(SVNStatusKind.EXTERNAL);
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			final SVNStatusKind kind = state.getStatusKind();
			return !IStateFilter.SF_IGNORED.accept(resource, state) && !kind.equals(SVNStatusKind.OBSTRUCTED) && !kind.equals(SVNStatusKind.EXTERNAL);
		}
	};

	public static final IStateFilter SF_ALL = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return true;
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return true;
		}
	};
}
