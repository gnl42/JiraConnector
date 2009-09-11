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

package com.atlassian.connector.eclipse.internal.subclipse.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.jetbrains.annotations.NotNull;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

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
			
			ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(roots[i]);
			try {
				if (filter.accept(local)) {
					return true;
				}
				else if (roots[i] instanceof IContainer && depth != IResource.DEPTH_ZERO && filter.allowsRecursion(local)) {
					recursiveCheck.add(roots[i]);
				}
			} catch (SVNException e) {
				StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, "Failed to check resource", e));
			}
		}
		
		// no resources accepted, check recursively (performance optimizations)
		if (depth != IResource.DEPTH_ZERO) {
			for (Iterator<IResource> it = recursiveCheck.iterator(); it.hasNext(); ) {
				IContainer local = (IContainer)it.next();
				try {
					if (FileUtility.checkForResourcesPresence(FileUtility.getAllMembers(local), filter, nextDepth)) {
						return true;
					}
				} catch (SVNException e) {
					StatusHandler.log(new Status(IStatus.WARNING, AtlassianSubclipseCorePlugin.PLUGIN_ID, "Failed to check resource", e));
				}
			}
		}
		return false;
	}
	
	@NotNull
	public static IResource []getAllMembers(IContainer container) throws SVNException {
		ISVNLocalFolder folder = SVNWorkspaceRoot.getSVNFolderFor(container);
		if (folder != null) {
			ISVNResource[] svnResources = folder.members(new NullProgressMonitor(), ISVNLocalFolder.ALL_MEMBERS);
			if (svnResources != null) {
				IResource[] resources = new IResource[svnResources.length];
				for(int i = 0, s = svnResources.length; i < s; ++i) {
					resources[i] = svnResources[i].getResource();
				}
				return resources;
			}
		}
		return new IResource[0];
	}
	
	private FileUtility() {
	}

	public static boolean isSVNInternals(IResource resource) {
		if (SVNProviderPlugin.getPlugin().isAdminDirectory(resource.getName())) {
			return true;
		}
		IResource parent = resource.getParent();
		return parent == null ? false : FileUtility.isSVNInternals(parent);
	}

}
