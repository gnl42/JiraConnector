/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.perforce.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.team.core.RepositoryProvider;
import org.jetbrains.annotations.NotNull;

import com.atlassian.theplugin.commons.util.MiscUtil;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * Common file processing functions
 * 
 * @author Alexander Gurov
 */
public final class FileUtility {
	public static final IResource []NO_CHILDREN = new IResource[0];
	
	public static boolean isLinked(IResource resource) {
    	// Eclipse 3.2 and higher
        return resource.isLinked(IResource.CHECK_ANCESTORS);
    }
    
	/*
     * If the resource is a derived, team private or linked resource, it is ignored
     */
    public static boolean isIgnored(IResource resource) {
    	return resource.isDerived() || resource.isTeamPrivateMember() || FileUtility.isLinked(resource); 
    }
    
    public static List<IResource> getResourcesByFilterRecursive(IResource []roots, IStateFilter filter) {
    	final List<IResource> result = MiscUtil.buildArrayList();
    	
		// first check all resources that are already accessible (performance optimizations)
		for (int i = 0; i < roots.length; i++) {
			//don't check ignored resources
			if (FileUtility.isIgnored(roots[i])) {//FileUtility.isSVNInternals(roots[i])
				continue;
			}
			
			IP4Resource local = P4Workspace.getWorkspace().getResource(roots[i]);
			if (filter.accept(local)) {
				result.add(roots[i]);
			}
			
			if (roots[i] instanceof IContainer && filter.allowsRecursion(local)) {
				result.addAll(getResourcesByFilterRecursive(getAllMembers((IContainer) roots[i]), filter));
			}
		}
		
		return result;
	}
	    
	public static boolean checkForResourcesPresenceRecursive(IResource []roots, IStateFilter filter) {
		return FileUtility.checkForResourcesPresence(roots, filter, IResource.DEPTH_INFINITE);
	}
	
	public static boolean checkForResourcesPresence(IResource []roots, IStateFilter filter, int depth) {
		ArrayList<IResource> recursiveCheck = null;
		int nextDepth = IResource.DEPTH_ZERO;
		if (depth != IResource.DEPTH_ZERO) {
			recursiveCheck = new ArrayList<IResource>();
			nextDepth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		}
		
		// first check all resources that are already accessible (performance optimizations)
		for (int i = 0; i < roots.length; i++) {
			//don't check ignored resources
			if (FileUtility.isIgnored(roots[i])) {//FileUtility.isSVNInternals(roots[i])
				continue;
			}
			
			IP4Resource local = P4Workspace.getWorkspace().getResource(roots[i]);
			if (filter.accept(local)) {
				return true;
			}
			else if (roots[i] instanceof IContainer && depth != IResource.DEPTH_ZERO && filter.allowsRecursion(local)) {
				recursiveCheck.add(roots[i]);
			}
		}
		
		// no resources accepted, check recursively (performance optimizations)
		if (depth != IResource.DEPTH_ZERO) {
			for (Iterator<IResource> it = recursiveCheck.iterator(); it.hasNext(); ) {
				IContainer local = (IContainer)it.next();
				if (FileUtility.checkForResourcesPresence(FileUtility.getAllMembers(local), filter, nextDepth)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@NotNull
	public static IResource []getAllMembers(IContainer container) {
		IP4Resource folder = P4Workspace.getWorkspace().getResource(container);
		if (folder != null && folder instanceof IP4Container) {
			IP4Resource[] scmResources = ((IP4Container) folder).members();
			if (scmResources != null) {
				List<IResource> resources = MiscUtil.buildArrayList();
				for(int i = 0, s = scmResources.length; i < s; ++i) {
					if (scmResources[i] instanceof IP4File) {
						resources.add(((IP4File)scmResources[i]).getLocalFileForLocation());
					} else if (scmResources[i] instanceof IP4Folder) {
						resources.addAll(Arrays.asList(((IP4Folder) scmResources[i]).getLocalContainers()));
					}
				}
				return resources.toArray(new IResource[resources.size()]);
			}
		}
		return new IResource[0];
	}
	
	private FileUtility() {
	}

	public static boolean isP4Internals(IResource resource) {
		IResource parent = resource.getParent();
		return parent == null ? false : FileUtility.isP4Internals(parent);
	}

	public static boolean isManagedByPerforce(IResource resource) {
		// check if project is associated with Perforce Team provider, 
		// if we don't test it asRepositoryResource will throw RuntimeException
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), PerforceTeamProvider.ID);
		if (provider != null) {
			return true;
		}
		return false;
	}
}
