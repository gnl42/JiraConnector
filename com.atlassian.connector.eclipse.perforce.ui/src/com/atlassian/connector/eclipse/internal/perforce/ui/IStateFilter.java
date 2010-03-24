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
import com.perforce.team.core.p4java.P4Workspace;

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
			return resource instanceof IP4File
					&& FileUtility.isManagedByPerforce(((IP4File) resource).getLocalFileForLocation())
					&& this.allowsRecursionImpl(null, ((IP4File) resource).getLocalFileForLocation(),
							((IP4File) resource).getStatus());
		}

		public boolean allowsRecursion(IResource resource, FileSpecOpStatus state) {
			return state != null && FileUtility.isManagedByPerforce(resource.getProject())
					&& this.allowsRecursionImpl(null, resource, state);
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

	public static final IStateFilter SF_ANY_CHANGE = SF_ALL;

	public static final IStateFilter SF_UNVERSIONED = SF_ALL;

	public static final IStateFilter SF_VERSIONED = SF_ALL;

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

}
