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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class OpenUploadedVirtualFileAction extends AbstractUploadedVirtualFileAction {

	public OpenUploadedVirtualFileAction(final VersionedVirtualFile virtualFile, final Review crucibleReview,
			final Shell shell, final IWorkbenchPage iWorkbenchPage) {
		super("", crucibleReview, null, shell, "Fetching File", null, new RemoteCrucibleOperation() {

			public void run(CrucibleServerFacade2 crucibleServerFacade, ConnectionCfg crucibleServerCfg)
					throws CrucibleLoginException, RemoteApiException, ServerPasswordNotProvidedException {
				open(crucibleReview, virtualFile, crucibleServerFacade, crucibleServerCfg, iWorkbenchPage, shell);
			}

		}, false);
	}

	private static void open(Review crucibleReview, final VersionedVirtualFile virtualFile,
			CrucibleServerFacade2 crucibleServerFacade, final ConnectionCfg crucibleServerCfg,
			final IWorkbenchPage iWorkbenchPage, Shell shell) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleUiUtil.checkAndRequestReviewActivation(crucibleReview);

//		final ICompareAnnotationModel annotationModel = new CrucibleCompareAnnotationModel(virtualFile, crucibleReview,
//				null);

		final ReviewFileContent file = getContent(virtualFile.getContentUrl(), crucibleServerFacade, crucibleServerCfg);

		shell.getDisplay().asyncExec(new Runnable() {

			public void run() {
				try {
					iWorkbenchPage.openEditor(new StringInput(new StringStorage(virtualFile.getName(),
							virtualFile.getContentUrl(), file.getContent())), "org.eclipse.ui.DefaultTextEditor");
				} catch (PartInitException e) {
					// TODO jj handle exception
					e.printStackTrace();
				}

			}
		});
	}

	private static class StringInput implements IStorageEditorInput {
		private final IStorage storage;

		public StringInput(IStorage storage) {
			this.storage = storage;
		}

		public boolean exists() {
			return true;
		}

		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		public String getName() {
			return storage.getName();
		}

		public IPersistableElement getPersistable() {
			return null;
		}

		public IStorage getStorage() {
			return storage;
		}

		public String getToolTipText() {
			return storage.getFullPath().toString();
		}

		public Object getAdapter(Class adapter) {
			return null;
		}
	}

	private static class StringStorage implements IStorage {
		private final byte[] content;

		private final String name;

		private final String url;

		public StringStorage(String name, String url, byte[] content) {
			this.name = name;
			this.url = url;
			this.content = content;
		}

		public InputStream getContents() {
			return new ByteArrayInputStream(content);
		}

		public IPath getFullPath() {
			return new Path(url);
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public String getName() {
			return name;
		}

		public boolean isReadOnly() {
			return true;
		}

	}

}
