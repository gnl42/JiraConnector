/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.actions;

import com.atlassian.connector.eclipse.team.ui.AbstractTeamUiConnector;
import com.atlassian.connector.eclipse.team.ui.CrucibleFile;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.team.ui.LocalStatus;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamConnectorType;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.ui.IEditorInput;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public class LocalTeamResourceConnector extends AbstractTeamUiConnector {

	public boolean isEnabled() {
		return true;
	}

	public SortedSet<ICustomChangesetLogEntry> getLatestChangesets(String repositoryUrl, int limit,
			IProgressMonitor monitor) throws CoreException {
		throw new NotImplementedException();
	}

	public LocalStatus getLocalRevision(IResource resource) throws CoreException {
		throw new NotImplementedException();
	}

	public ScmRepository getApplicableRepository(IResource resource) throws CoreException {
		throw new NotImplementedException();
	}

	public String getName() {
		throw new NotImplementedException();
	}

	public TeamConnectorType getType() {
		throw new NotImplementedException();
	}

	public boolean haveMatchingResourcesRecursive(IResource[] roots, State filter) {
		throw new NotImplementedException();
	}

	public List<IResource> getResourcesByFilterRecursive(IResource[] roots, State filter) {
		final List<IResource> result = MiscUtil.buildArrayList();
		if (filter == State.SF_ALL || filter == State.SF_UNVERSIONED) {
			for (IResource root : roots) {
				try {
					root.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							result.add(resource);
							return true;
						}
					});
				} catch (CoreException e) {
					StatusHandler.log(e.getStatus());
				}
			}
		}
		return result;
	}

	public Collection<UploadItem> getUploadItemsForResources(IResource[] resources, IProgressMonitor monitor)
			throws CoreException {
		List<UploadItem> items = MiscUtil.buildArrayList();
		for (IResource resource : resources) {
			if (resource.getType() != IResource.FILE) {
				// ignore anything but files
				continue;
			}

			final String fileName = getResourcePathWithProjectName(resource);

			byte[] newContent = getResourceContent(((IFile) resource).getContents());
			items.add(new UploadItem(fileName, new byte[0], newContent.length == 0 ? EMPTY_ITEM : newContent));
		}
		return items;
	}

	public IResource[] getMembersForContainer(IContainer element) throws CoreException {
		throw new NotImplementedException();
	}

	public boolean isResourceManagedBy(IResource resource) {
		return true;
	}

	public boolean canHandleEditorInput(IEditorInput editorInput) {
		throw new NotImplementedException();
	}

	public boolean canHandleFile(IFile file) {
		throw new NotImplementedException();
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, String fileUrl, String revision) {
		throw new NotImplementedException();
	}

	public CrucibleFile getCrucibleFileFromReview(Review activeReview, IFile file) {
		throw new NotImplementedException();
	}

	public Collection<ScmRepository> getRepositories(IProgressMonitor monitor) {
		throw new NotImplementedException();
	}

	public boolean isResourceAcceptedByFilter(IResource resource, State state) {
		return (state == State.SF_ALL || state == State.SF_UNVERSIONED);
	}

}
