package com.atlassian.connector.eclipse.internal.directclickthrough.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.Messages;
import org.eclipse.mylyn.internal.tasks.ui.OpenRepositoryTaskJob;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.atlassian.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughImages;
import com.atlassian.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughUiPlugin;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;

@SuppressWarnings("serial")
public class DirectClickThroughServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		final String path = req.getPathInfo();

		if ("/icon".equals(path)) {
			writeIcon(resp);
			return;
		}

		bringEclipseToFront();

		if ("/file".equals(path)) {
			writeIcon(resp);
			handleOpenFileRequest(req);
		} else if ("/issue".equals(path)) {
			writeIcon(resp);
			handleOpenIssueRequest(req);
		} else if ("/review".equals(path)) {
			writeIcon(resp);
			handleOpenReviewRequest(req);
		} else if ("/build".equals(path)) {
			writeIcon(resp);
			handleOpenBuildRequest(req);
		} else {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			StatusHandler
					.log(new Status(
							IStatus.WARNING,
							DirectClickThroughUiPlugin.PLUGIN_ID,
							NLS
									.bind(
											"Direct Click Through server received unknown command: [{0}]",
											path)));
		}
	}

	private void handleOpenFileRequest(final HttpServletRequest req) {
		final String file = StringUtil.removePrefixSlashes(req
				.getParameter("file"));
		final String path = StringUtil.removeSuffixSlashes(req
				.getParameter("path"));
		final String vcsRoot = StringUtil.removeSuffixSlashes(req
				.getParameter("vcs_root"));
		final String line = req.getParameter("line");
		if (file != null) {
			openRequestedFile(path, file, vcsRoot, line);
		}
	}

	@SuppressWarnings("restriction")
	private void openRequestedFile(final String path, final String file,
			final String vcsRoot, final String line) {
		final List<IResource> resources = MiscUtil.buildArrayList();
		final int[] matchLevel = new int[] { 0 };

		Job searchAndOpenJob = new Job(
				"Search and open files requested using Direct Click Through") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Searching for files",
						IProgressMonitor.UNKNOWN);
				try {
					ResourcesPlugin.getWorkspace().getRoot().accept(
							new IResourceVisitor() {
								private int matchingLastSegments(
										IPath firstPath, IPath secondPath) {
									int firstPathLen = firstPath.segmentCount();
									int secondPathLen = secondPath
											.segmentCount();
									int max = Math.min(firstPathLen,
											secondPathLen);
									int count = 0;
									for (int i = 1; i <= max; i++) {
										if (!firstPath
												.segment(firstPathLen - i)
												.equals(
														secondPath
																.segment(secondPathLen
																		- i))) {
											return count;
										}
										count++;
									}
									return count;
								}

								public boolean visit(IResource resource)
										throws CoreException {
									if (!(resource instanceof IFile)) {
										return true; // skip it if it's not a
														// file, but check its
														// members
									}

									int matchCount = matchingLastSegments(
											new Path(path == null ? file : path
													+ "/" + file), resource
													.getLocation());
									if (matchCount > 0) {
										if (matchCount > matchLevel[0]) {
											resources.clear();
											matchLevel[0] = matchCount;
										}
										if (matchCount == matchLevel[0]) {
											resources.add(resource);
										}
									}

									return true; // visit also members of this
													// resource
								}
							});
				} catch (CoreException e) {
					return new Status(
							IStatus.ERROR,
							DirectClickThroughUiPlugin.PLUGIN_ID,
							"Direct Click Through server failed to find matching files",
							e);
				} finally {
					monitor.done();
				}
				return new Status(IStatus.OK,
						DirectClickThroughUiPlugin.PLUGIN_ID, "Search finished");
			}
		};

		searchAndOpenJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (!event.getResult().isOK()) {
					return;
				}
				if (resources.size() > 0) {
					if (resources.size() == 1) {
						openFile(resources.iterator().next(), line);
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										final ListDialog ld = new ListDialog(
												WorkbenchUtil.getShell());
										ld.setAddCancelButton(true);
										ld
												.setContentProvider(new ArrayContentProvider());
										ld
												.setInput(resources
														.toArray(new IResource[resources
																.size()]));
										ld.setTitle("Select File to Open");
										ld
												.setLabelProvider(new LabelProvider() {
													@Override
													public String getText(
															Object element) {
														if (element instanceof IResource) {
															return ((IResource) element)
																	.getFullPath()
																	.toString();
														}
														return super
																.getText(element);
													}
												});
										ld
												.setMessage("Direct Click Through request matches multiple files.\n"
														+ "Please select apropriate file that will be open in editor.");

										if (ld.open() == Window.OK) {
											final Object[] result = ld
													.getResult();
											if (result != null
													&& result.length > 0) {
												for (Object selected : result) {
													if (selected instanceof IResource) {
														openFile(
																(IResource) selected,
																line);
													}
												}
											}
										}
									}
								});
					}
				} else {
					PlatformUI.getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									new MessageDialog(
											WorkbenchUtil.getShell(),
											"Unable to open file",
											null,
											"Unable to open file requested using Direct Click Through. Either file is missing or all projects are closed.",
											MessageDialog.INFORMATION,
											new String[] { IDialogConstants.OK_LABEL },
											0).open();
								}
							});
				}
			}
		});

		searchAndOpenJob.schedule();
	}

	private void openFile(final IResource resource, final String line) {
		Assert.isNotNull(resource);
		
		if (resource.getType() != IResource.FILE) {
			return;
		}

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IEditorPart editor;
				try {
					editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), (IFile) resource);
				} catch (PartInitException e1) {
					StatusHandler.log(e1.getStatus());
					return;
				}
				
				if (editor != null) {
					if (line != null && line.length() > 0 && editor instanceof ITextEditor) {
						try {
							final int l = Integer.parseInt(line);
							gotoLine((ITextEditor) editor, l);
						} catch (NumberFormatException e) {
							StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, NLS.bind("Wrong line number format when requesting to open file in the IDE [{0}]", line), e));
							return;
						}
					}
				}
			}
		});
	}

	/**
	 * Jumps to the given line.
	 * 
	 * @param line
	 *            the line to jump to
	 */
	private void gotoLine(ITextEditor editor, int line) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		try {

			int start = document.getLineOffset(line);
			editor.selectAndReveal(start, 0);

			IWorkbenchPage page = editor.getSite().getPage();
			page.activate(editor);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	private void handleOpenBuildRequest(final HttpServletRequest req) {
		final String buildKey = req.getParameter("build_key");
		final String buildNumber = req.getParameter("build_number");
		final String repositoryUrl = req.getParameter("server_url");

		// FIXME: not used
		final String testPackage = req.getParameter("test_package");
		final String testClass = req.getParameter("test_class");
		final String testMethod = req.getParameter("test_method");

		if (buildKey == null || buildKey.length() == 0 || repositoryUrl == null
				|| repositoryUrl.length() == 0 || buildNumber == null
				|| buildNumber.length() == 0) {
			StatusHandler
					.log(new Status(IStatus.WARNING,
							DirectClickThroughUiPlugin.PLUGIN_ID,
							"Cannot open build: build_key or build_number or server_url parameter is null"));
		}

		try {
			Class<?> corePluginClass = Class
					.forName("com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin");

			String connectorKind = (String) corePluginClass.getDeclaredField(
					"CONNECTOR_KIND").get(null);

			IRepositoryManager repositoryManager = TasksUi
					.getRepositoryManager();

			TaskRepository taskRepository = repositoryManager.getRepository(
					connectorKind, repositoryUrl);

			if (taskRepository == null) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {
							@SuppressWarnings("restriction")
							public void run() {
								MessageDialog
										.openError(
												null,
												Messages.OpenRepositoryTaskJob_Repository_Not_Found,
												MessageFormat
														.format(
																Messages.OpenRepositoryTaskJob_Could_not_find_repository_configuration_for_X,
																repositoryUrl)
														+ "\n" + //$NON-NLS-1$
														MessageFormat
																.format(
																		Messages.OpenRepositoryTaskJob_Please_set_up_repository_via_X,
																		Messages.TasksUiPlugin_Task_Repositories));
							}

						});
				return;
			}

			Class<?> openBuildJobClass = Class
					.forName("com.atlassian.connector.eclipse.internal.bamboo.ui.operations.OpenBambooBuildJob");

			Constructor<?> openBuildJobConstructor = openBuildJobClass
					.getConstructor(String.class, int.class,
							TaskRepository.class);

			Job openBambooBuildJob = (Job) openBuildJobConstructor.newInstance(
					buildKey, Integer.valueOf(buildNumber), taskRepository);

			openBambooBuildJob.schedule();
		} catch (Exception e) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					new MessageDialog(
							WorkbenchUtil.getShell(),
							"Unable to handle request",
							null,
							"Direct Click Through failed to open issue because Atlassian Bamboo Integration is missing.",
							MessageDialog.INFORMATION,
							new String[] { IDialogConstants.OK_LABEL }, 0)
							.open();
				}
			});
		}

	}

	@SuppressWarnings("restriction")
	private void handleOpenReviewRequest(final HttpServletRequest req) {
		final String taskId = req.getParameter("review_key");
		final String repositoryUrl = req.getParameter("server_url");
		// TODO: add support for those two
		// final String filePath = req.getParameter("file_path");
		// final String commentId = req.getParameter("comment_id");

		if (taskId == null || repositoryUrl == null) {
			StatusHandler
					.log(new Status(IStatus.WARNING,
							DirectClickThroughUiPlugin.PLUGIN_ID,
							"Cannot open issue: review_key or server_url parameter is null"));
		}

		try {
			Class<?> cls = Class
					.forName("com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin");

			Field connectorKindField = cls.getDeclaredField("CONNECTOR_KIND");

			String connectorKind = (String) connectorKindField.get(null);

			IRepositoryManager repositoryManager = TasksUi
					.getRepositoryManager();
			AbstractRepositoryConnector connector = repositoryManager
					.getRepositoryConnector(connectorKind);
			String taskUrl = connector == null ? null : connector.getTaskUrl(
					repositoryUrl, taskId);

			new OpenRepositoryTaskJob(connectorKind, repositoryUrl, taskId,
					taskUrl, null).schedule();
		} catch (Exception e) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					new MessageDialog(
							WorkbenchUtil.getShell(),
							"Unable to handle request",
							null,
							"Direct Click Through failed to open issue because Atlassian Crucible & FishEye Integration is missing.",
							MessageDialog.INFORMATION,
							new String[] { IDialogConstants.OK_LABEL }, 0)
							.open();
				}
			});
		}
	}

	@SuppressWarnings("restriction")
	private void handleOpenIssueRequest(final HttpServletRequest req) {
		final String taskId = req.getParameter("issue_key");
		final String repositoryUrl = req.getParameter("server_url");

		if (taskId == null || repositoryUrl == null) {
			StatusHandler
					.log(new Status(IStatus.WARNING,
							DirectClickThroughUiPlugin.PLUGIN_ID,
							"Cannot open issue: issue_key or server_url parameter is null"));
		}

		try {
			Class<?> cls = Class
					.forName("org.eclipse.mylyn.internal.jira.core.JiraCorePlugin");

			Field connectorKindField = cls.getDeclaredField("CONNECTOR_KIND");

			String connectorKind = (String) connectorKindField.get(null);

			IRepositoryManager repositoryManager = TasksUi
					.getRepositoryManager();
			AbstractRepositoryConnector connector = repositoryManager
					.getRepositoryConnector(connectorKind);
			String taskUrl = connector == null ? null : connector.getTaskUrl(
					repositoryUrl, taskId);

			new OpenRepositoryTaskJob(connectorKind, repositoryUrl, taskId,
					taskUrl, null).schedule();
		} catch (Exception e) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					new MessageDialog(
							WorkbenchUtil.getShell(),
							"Unable to handle request",
							null,
							"Direct Click Through failed to open issue because Mylyn JIRA Connector is missing.",
							MessageDialog.INFORMATION,
							new String[] { IDialogConstants.OK_LABEL }, 0)
							.open();
				}
			});
		}

	}

	private static Shell getModalDialogOpen(Shell shell) {
		Shell[] shells = shell.getShells();
		for (int i = 0; i < shells.length; i++) {
			Shell dialog = shells[i];
			if ((dialog.getStyle() & (SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) > 0) {
				return dialog;
			}
		}
		return null;
	}

	private static void bringEclipseToFront() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				if (windows == null || windows.length == 0) {
					return;
				}

				Shell shell = windows[0].getShell();
				if (shell != null) {
					Shell dialog = getModalDialogOpen(shell);
					if (shell.getMinimized()) {
						shell.setMinimized(false);
						if (dialog != null) {
							dialog.setFocus();
						}
					}
					// If a model dialog is open on the shell, don't activate it
					if (dialog == null) {
						shell.forceActive();
					}
				}
			}
		});
	}

	private void writeIcon(final HttpServletResponse response)
			throws IOException {
		InputStream icon = new BufferedInputStream(new URL(
				DirectClickThroughImages.BASE_URL,
				DirectClickThroughImages.PATH_ECLIPSE).openStream());
		try {
			response.setContentType("image/gif");
			response.setStatus(HttpServletResponse.SC_OK);

			OutputStream output = response.getOutputStream();
			for (int b = icon.read(); b != -1; b = icon.read()) {
				output.write(b);
			}
		} finally {
			try {
				icon.close();
			} catch (Exception e) { /* ignore */
			}
		}
	}
}
