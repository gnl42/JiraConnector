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
/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.subclipse.ui.operations;

import com.atlassian.connector.eclipse.ui.team.IGenerateDiffOperation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An operation to run the SVN diff operation on a set of resources. The result of the diff is written to a file. If
 * there are no differences found, the user is notified and the output file is not created.
 */
public class GenerateDiffOperation implements IGenerateDiffOperation {
	private static String ECLIPSE_PATCH_HEADER = "### Eclipse Workspace Patch 1.0"; //$NON-NLS-1$

	private static String ECLIPSE_PROJECT_MARKER = "#P "; //$NON-NLS-1$

	private static String EOL = System.getProperty("line.separator");

	private final IResource[] resources;

	private final boolean recursive;

	private IResource[] selectedResources;

	private final boolean eclipseFormat;

	private final boolean projectRelative;

	private String patchText;

	public GenerateDiffOperation(IResource[] resources, boolean recursive, boolean eclipseFormat,
			boolean projectRelative) {
		this.resources = resources;
		this.eclipseFormat = eclipseFormat;
		this.projectRelative = projectRelative;
		this.recursive = recursive;
	}

	public String getPatch() {
		return patchText;
	}

	public boolean hasPatch() {
		return !patchText.isEmpty();
	}

	/**
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			monitor.beginTask("", 500); //$NON-NLS-1$
			monitor.setTaskName("Running SVN diff..."); //$NON-NLS-1$

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			File tmpFile = File.createTempFile("sub", ""); //$NON-NLS-1$ //$NON-NLS-2$
			tmpFile.deleteOnExit();

			ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[0]);
			ISVNClientAdapter svnClient = svnResource.getRepository().getSVNClient();
			try {
				monitor.worked(100);
				File[] files = { getVersionedFiles()[1] };
				if (selectedResources == null) {
					svnClient.diff(files, tmpFile, recursive);
				} else {
					if (eclipseFormat) {
						createEclipsePatch(resources, tmpFile, recursive);
					} else {
						File relativeToPath = null;
						if (projectRelative) {
							relativeToPath = selectedResources[0].getProject().getLocation().toFile();
						} else {
							relativeToPath = getRelativeToPath();
							if (relativeToPath.isFile()) {
								relativeToPath = relativeToPath.getParentFile();
							}
						}
						svnClient.createPatch(files, relativeToPath, tmpFile, recursive);
					}
				}
				monitor.worked(300);
				InputStream is = new FileInputStream(tmpFile);
				byte[] buffer = new byte[30000];
				int length;
				while ((length = is.read(buffer)) != -1) {
					os.write(buffer, 0, length);
				}
			} finally {
				os.close();
			}

			patchText = os.toString();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

	private File getRelativeToPath() {
		if (selectedResources.length == 1) {
			return new File(selectedResources[0].getLocation().toString());
		}
		String commonRoot = null;
		String path = selectedResources[0].getLocation().toString();
		tag1: for (int i = 0; i < path.length(); i++) {
			String partialPath = path.substring(0, i + 1);
			if (partialPath.endsWith("/") || partialPath.endsWith("\\")) {
				for (int j = 1; j < selectedResources.length; j++) {
					if (!selectedResources[j].getLocation().toString().startsWith(partialPath)) {
						break tag1;
					}
				}
				commonRoot = partialPath.substring(0, i);
			}
		}
		if (commonRoot != null) {
			return new File(commonRoot);
		}
		return null;
	}

	private File[] getVersionedFiles() {
		ArrayList<File> versionedFileList = new ArrayList<File>();
		for (IResource resource : resources) {
			versionedFileList.add(new File(resource.getLocation().toOSString()));
		}
		return versionedFileList.toArray(new File[versionedFileList.size()]);
	}

	private boolean containsResource(List<IResource> list, IResource resource) {
		if (list.contains(resource)) {
			return true;
		}
		IResource parent = resource;
		while (parent != null) {
			parent = parent.getParent();
			if (list.contains(parent)) {
				return true;
			}
		}
		return false;
	}

	public void setSelectedResources(IResource[] selectedResources) {
		this.selectedResources = selectedResources;
	}

	private void createEclipsePatch(IResource[] paths, File outputFile, boolean recurse) throws SVNClientException {
		FileOutputStream os = null;
		InputStream is = null;
		try {
			byte[] buffer = new byte[4096];

			os = new FileOutputStream(outputFile);
			if (paths.length > 0) {
				os.write(ECLIPSE_PATCH_HEADER.getBytes());
				os.write(EOL.getBytes());
			}

			Map<IProject, List<File>> projectToResources = new HashMap<IProject, List<File>>();

			for (IResource resource : paths) {
				IProject project = resource.getProject();
				List<File> files = projectToResources.get(project);
				if (files == null) {
					files = new ArrayList<File>();
					projectToResources.put(project, files);
				}
				files.add(resource.getLocation().toFile());
			}

			for (Map.Entry<IProject, List<File>> entry : projectToResources.entrySet()) {

				IResource project = entry.getKey();
				List<File> files = entry.getValue();

				ISVNClientAdapter client = SVNWorkspaceRoot.getSVNResourceFor(project).getRepository().getSVNClient();

				os.write(ECLIPSE_PROJECT_MARKER.getBytes());
				os.write(project.getName().getBytes());
				os.write(EOL.getBytes());

				File tempFile = File.createTempFile("tempDiff", ".txt");
				tempFile.deleteOnExit();
				client.createPatch(files.toArray(new File[files.size()]), project.getLocation().toFile(), tempFile,
						recurse);

				try {
					is = new FileInputStream(tempFile);

					int bytes_read;
					while ((bytes_read = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytes_read);
					}
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} catch (Exception e) {
			throw new SVNClientException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public IStatus getStatus() {
		return Status.OK_STATUS;
	}

}