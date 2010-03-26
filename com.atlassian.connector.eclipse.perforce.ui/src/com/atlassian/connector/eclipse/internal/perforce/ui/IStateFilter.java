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
package com.atlassian.connector.eclipse.internal.perforce.ui;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IgnoredFiles;

import org.eclipse.core.resources.IResource;

/**
 * Resource state filter interface and most useful implementations
 * 
 * @author Alexander Gurov
 */
public interface IStateFilter {

	boolean accept(IResource resource, FileSpecOpStatus state);

	boolean accept(IP4Resource resource);

	boolean allowsRecursion(IP4Resource resource);

	boolean allowsRecursion(IResource resource, FileSpecOpStatus state);

	public abstract class AbstractStateFilter implements IStateFilter {
		public boolean accept(IP4Resource resource) {
			return resource instanceof IP4File
					&& FileUtility.isManagedByPerforce(((IP4File) resource).getLocalFileForLocation())
					&& this.acceptImpl(resource, ((IP4File) resource).getLocalFileForLocation(),
							((IP4File) resource).getStatus());
		}

		public boolean accept(IResource resource, FileSpecOpStatus state) {
			return state != null && FileUtility.isManagedByPerforce(resource.getProject())
					&& this.acceptImpl(P4Workspace.getWorkspace().getResource(resource), resource, state);
		}

		public boolean allowsRecursion(IP4Resource resource) {
			if (resource instanceof IP4File) {
				return this.allowsRecursionImpl(null, ((IP4File) resource).getLocalFileForLocation(),
						((IP4File) resource).getStatus());
			} else {
				return true;
			}
		}

		public boolean allowsRecursion(IResource resource, FileSpecOpStatus state) {
			return FileUtility.isManagedByPerforce(resource) && this.allowsRecursionImpl(null, resource, state);
		}

		protected abstract boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state);

		protected abstract boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state);
	}

	public static final IStateFilter SF_ALL = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}

		@Override
		protected boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	public static final IStateFilter SF_ANY_CHANGE = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return local instanceof IP4File && ((IP4File) local).isOpened() && ((IP4File) local).openedByOwner();
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	// FIXME: 
	public static final IStateFilter SF_ADDED = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return !IgnoredFiles.isIgnored(resource) && local instanceof IP4File
					&& P4File.isActionAdd(((IP4File) local).getAction());
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	public static final IStateFilter SF_UNVERSIONED = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return state.equals(FileSpecOpStatus.UNKNOWN);
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	public static final IStateFilter SF_VERSIONED = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return !SF_UNVERSIONED.accept(local);
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	public static final IStateFilter SF_IGNORED = new AbstractStateFilter() {

		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return IgnoredFiles.isIgnored(resource) || IStateFilter.SF_UNVERSIONED.accept(resource, state);
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

	public static final IStateFilter SF_DELETED = new AbstractStateFilter() {
		@Override
		protected boolean acceptImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return local instanceof IP4File && P4File.isActionDelete(((IP4File) local).getHeadAction())
					&& ((IP4File) local).getHaveRevision() == 0;
		}

		@Override
		public boolean allowsRecursionImpl(IP4Resource local, IResource resource, FileSpecOpStatus state) {
			return true;
		}
	};

}
