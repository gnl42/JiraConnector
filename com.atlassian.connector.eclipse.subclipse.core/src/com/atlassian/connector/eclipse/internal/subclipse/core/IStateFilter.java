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
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor.Action;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor.Operation;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor.Reason;


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
			return resource.getStatus() != null && SVNWorkspaceRoot.isManagedBySubclipse(resource.getIResource().getProject()) 
				&& this.acceptImpl(resource, resource.getResource(), resource.getStatus());
		}
		public boolean accept(IResource resource, LocalResourceStatus state) {
			return state != null && SVNWorkspaceRoot.isManagedBySubclipse(resource.getProject()) && this.acceptImpl(null, resource, state);
		}
		public boolean allowsRecursion(ISVNLocalResource resource) throws SVNException {
			return resource.getStatus() != null 
				&& SVNWorkspaceRoot.isManagedBySubclipse(resource.getIResource().getProject())
				&& this.allowsRecursionImpl(null, resource.getResource(), resource.getStatus());
		}
		public boolean allowsRecursion(IResource resource, LocalResourceStatus state) {
			return state != null 
				&& SVNWorkspaceRoot.isManagedBySubclipse(resource.getProject())
				&& this.allowsRecursionImpl(null, resource, state);
		}
		
		protected abstract boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state);
		protected abstract boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state);
		
		protected ISVNLocalResource takeLocal(LocalResource local, IResource resource) {
			return local != null ? local : SVNWorkspaceRoot.getSVNResourceFor(resource); 
		}
	}
	
	public static abstract class AbstractTreeConflictingStateFilter extends AbstractStateFilter {
		/*
		 * Note: as we're trying to retrieve local resource from remote storage (if it is null) then we must not call
		 * particular filters in order to avoid stack overflow (e.g. SF_UNVERSIONED, it's called during calculating of local resource)
		 */
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			try {
				if (local.getStatus().hasTreeConflict()) {
					SVNConflictDescriptor treeConflict = local.getStatus().getConflictDescriptor();
					return this.acceptTreeConflict(treeConflict, local);
				}
			} catch (SVNException e) {
				return false;
			}									
			return false;
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return IStateFilter.SF_ONREPOSITORY.accept(resource, state);
		}
		protected abstract boolean acceptTreeConflict(SVNConflictDescriptor treeConflict, ISVNLocalResource local);
	}
	
	public static final IStateFilter SF_TREE_CONFLICTING_REPOSITORY_EXIST = new TreeConflictingRepositoryExistStateFilter();
	
	public static final IStateFilter SF_ONREPOSITORY = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			try {
				if (local.getStatus().hasTreeConflict()) {
					return IStateFilter.SF_TREE_CONFLICTING_REPOSITORY_EXIST.accept(local);
				}
			} catch (SVNException e) {
				return false;
			}
			return state.isReplaced() || state.getStatusKind().equals(SVNStatusKind.NORMAL)
				|| state.isDirty() || state.isDeleted() || state.isPropConflicted() || state.isTextConflicted()
				|| state.isDeleted() || state.isMissing();
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return IStateFilter.SF_VERSIONED.accept(resource, state);
		}
	};
	
	public static final IStateFilter SF_UNVERSIONED = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return state.isUnversioned() || (resource != null && !SVNWorkspaceRoot.isManagedBySubclipse(resource.getProject()));
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return true;
		}
	};
	
	public static final IStateFilter SF_IGNORED = new AbstractStateFilter() {
	    
		@Override
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return state.isIgnored() || IStateFilter.SF_UNVERSIONED.accept(resource, state) && SubclipseUtil.isIgnored(resource); 
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
	
	public static final IStateFilter SF_ADDED = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return state.isReplaced() || state.isAdded() || state.isUnversioned();
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return IStateFilter.SF_VERSIONED.accept(resource, state);
		}
	};
	
	public static class TreeConflictingRepositoryExistStateFilter extends AbstractTreeConflictingStateFilter {
		protected boolean acceptTreeConflict(SVNConflictDescriptor treeConflict, ISVNLocalResource resource) {			
			/*
			 * For update operation resource exists on repository if action isn't 'Delete'
			 * 
			 * For switch or merge operations we can't exactly detect if resource exists remotely.
			 * Probably, we could determine it be exploring sync info's (AbstractSVNSyncInfo) remote resource variant,
			 * but such solution isn't applicable here (also I found following why we can't use it: while calculating
			 * sync info some filters are called(e.g. SF_ONREPOSITORY) and we get stack overflow). 
			 * So we consider that resource exists remotely if conflict descriptor reason is 'modified'
			 *  
			 * TODO Probably, we can add more specific conditions for merge and switch operations here
			 * 		Take into account IResourceChange ?
			 */
			if (treeConflict.getOperation() == Operation._update || treeConflict.getOperation() == Operation._switch) {
				/*
				 * 1. Action 'Delete'
				 * 2. Not (Action 'Add' and reason 'Add') 
				 */
				return treeConflict.getAction() != Action.delete && !(treeConflict.getAction() == Action.add && treeConflict.getReason() == Reason.added);
			} else if (treeConflict.getOperation() == Operation._merge) {
				return treeConflict.getAction() != Action.delete && treeConflict.getReason() == Reason.edited;
			}
			return false;	
		}
	}
	
	public static final IStateFilter SF_VERSIONED = new AbstractStateFilter() {
		protected boolean acceptImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			try {
				if (local.getStatus().hasTreeConflict()) {
					return new TreeConflictingRepositoryExistStateFilter() {			
						protected boolean acceptTreeConflict(SVNConflictDescriptor treeConflict, ISVNLocalResource resource) {
							return super.acceptTreeConflict(treeConflict, resource) || Reason.added == treeConflict.getReason();
						}
					}.accept(local);
				}
			} catch (SVNException e) {
				return false;
			}													
			return state.isReplaced() || state.isAdded() || state.getStatusKind().equals(SVNStatusKind.NORMAL) || state.isDirty()
				|| state.isPropConflicted() || state.isTextConflicted() || state.isDeleted() || state.getStatusKind().equals(SVNStatusKind.OBSTRUCTED);
		}
		protected boolean allowsRecursionImpl(ISVNLocalResource local, IResource resource, LocalResourceStatus state) {
			return this.accept(resource, state);
		}
	};
	
}
