/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.connector.eclipse.internal.directclickthrough.ui;

import java.util.Map;

/**
 * @author Jacek Jaroczynski
 */
class IdeHttpServerHandler {
	private final byte[] icon;

	public IdeHttpServerHandler(final byte[] iconArray) {
		this.icon = iconArray;
	}

	/*
	public Response handleRequest(final Type type, final String url, final Map<String, String> parameters) {

		final String method = StringUtil.removeTrailingSlashes(url);

		Response response = new Response();

		if (method.equals("icon")) {
			writeIcon(response);
		} else if (method.equals("file")) {
			writeIcon(response);
			handleOpenFileRequest(parameters);
		} else if (method.equals("issue")) {
			writeIcon(response);
			handleOpenIssueRequest(parameters);
		} else if (method.equals("review")) {
			writeIcon(response);
			handleOpenReviewRequest(parameters);
		} else {
			response.setNoContent();
			StatusHandler.log(new Status(IStatus.WARNING, DirectClickThroughUiPlugin.PLUGIN_ID, 
					"Unknown command received: [" + method + "]"));
		}

		return response;
	}
	 */
	private void handleOpenReviewRequest(final Map<String, String> parameters) {
		/*final String reviewKey = parameters.get("review_key");
		final String serverUrl = parameters.get("server_url");
		final String filePath = parameters.get("file_path");
		final String commentId = parameters.get("comment_id");
		if (reviewKey != null && serverUrl != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					// try to open received reviewKey in all open projects
					for (final Project project : ProjectManager.getInstance().getOpenProjects()) {

						bringIdeaToFront(project);

						ProgressManager.getInstance().run(
								new FindAndOpenReviewTask(project, "Looking for Review " + reviewKey, false, reviewKey,
										serverUrl, filePath, commentId));
					}
				}
			});
		} else {
			StatusHandler.log(new Status(IStatus.WARNING, AtlassianVqembwebUiPlugin.PLUGIN_ID, 
					"Cannot open review: review_key or server_url parameter is null"));
		}*/
	}

	private void handleOpenFileRequest(final Map<String, String> parameters) {
		/*final String file = StringUtil.removePrefixSlashes(parameters.get("file"));
		final String path = StringUtil.removeSuffixSlashes(parameters.get("path"));
		final String vcsRoot = StringUtil.removeSuffixSlashes(parameters.get("vcs_root"));
		final String line = parameters.get("line");
		if (file != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					openRequestedFile(path, file, vcsRoot, line);
				}
			});
		}*/
	}

	private void openRequestedFile(String path, String file, String vcsRoot, String line) {
		/*boolean found = false;
		// try to open requested file in all open projects
		for (Project project : ProjectManager.getInstance().getOpenProjects()) {
			String filePath = (path == null ? file : path + "/" + file);
			// find file by name (and path if provided)
			Collection<PsiFile> psiFiles = CodeNavigationUtil.findPsiFiles(project, filePath);

			// narrow found list of files by VCS
			if (psiFiles != null && psiFiles.size() > 0 && isDefined(vcsRoot)) {
				Collection<PsiFile> pf = CodeNavigationUtil.findPsiFilesWithVcsUrl(psiFiles, vcsRoot, project);
				// if VCS narrowed to empty list then return without narrowing
				// VCS could not match because of different configuration in IDE and web client (JIRA, FishEye, etc)
				if (pf != null && pf.size() > 0) {
					psiFiles = pf;
				}
			}
			// open file or show popup if more than one file found
			if (psiFiles != null && psiFiles.size() > 0) {
				found = true;
				if (psiFiles.size() == 1) {
					openFile(project, psiFiles.iterator().next(), line);
				} else if (psiFiles.size() > 1) {
					ListPopup popup = JBPopupFactory.getInstance().createListPopup(
							new FileListPopupStep("Select File to Open", new ArrayList<PsiFile>(psiFiles), line,
									project));
					popup.showCenteredInCurrentWindow(project);
				}
			}
			bringIdeaToFront(project);
		}
		// message box showed only if the file was not found at all (in all project)
		if (!found) {
			String msg = "";
			if (ProjectManager.getInstance().getOpenProjects().length > 0) {
				msg = "Project does not contain requested file" + file;
			} else {
				msg = "Please open a project in order to indicate search path for file " + file;
			}
			Messages.showInfoMessage(msg, PluginUtil.PRODUCT_NAME);
		}*/
	}

	/*private static void openFile(final Project project, final PsiFile psiFile, final String line) {
		if (psiFile != null) {
			psiFile.navigate(true); // open file

			final VirtualFile virtualFile = psiFile.getVirtualFile();

			if (virtualFile != null && line != null && line.length() > 0) { //place cursor in specified line
				try {
					Integer iLine = Integer.valueOf(line);
					if (iLine != null) {
						OpenFileDescriptor display = new OpenFileDescriptor(project, virtualFile, iLine, 0);
						if (display.canNavigateToSource()) {
							display.navigate(false);
						}
					}
				} catch (NumberFormatException e) {
					PluginUtil.getLogger().warn(
							"Wrong line number format when requesting to open file in the IDE [" + line + "]", e);
				}
			}
		}
	}*/

	private static boolean isDefined(final String param) {
		return param != null && param.length() > 0;
	}

	private void handleOpenIssueRequest(final Map<String, String> parameters) {
		/*final String issueKey = parameters.get("issue_key");
		final String serverUrl = parameters.get("server_url");
		if (issueKey != null && serverUrl != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					boolean found = false;
					// try to open received issueKey in all open projects
					for (Project project : ProjectManager.getInstance().getOpenProjects()) {

						if (IdeaHelper.getIssueListToolWindowPanel(project).openIssue(issueKey, serverUrl)) {
							found = true;
						}

						bringIdeaToFront(project);
					}

					if (!found) {
						Messages.showInfoMessage("Cannot find issue " + issueKey, PluginUtil.PRODUCT_NAME);
					}
				}
			});
		} else {
			PluginUtil.getLogger().warn("Cannot open issue: issue_key or server_url parameter is null");
		}*/
	}

	/*
	private static void bringIdeaToFront(final Project project) {
		WindowManager.getInstance().getFrame(project).setVisible(true);

		String osName = System.getProperty("os.name");
		osName = osName.toLowerCase();

		if (osName.contains("windows") || osName.contains("mac os x")) {
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(true);
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(false);

		} else { //for linux
			WindowManager.getInstance().getFrame(project).toFront();
		}

		// how to set focus???
		WindowManager.getInstance().getFrame(project).setFocusable(true);
		WindowManager.getInstance().getFrame(project).setFocusableWindowState(true);
		WindowManager.getInstance().getFrame(project).requestFocus();
		WindowManager.getInstance().getFrame(project).requestFocusInWindow();
	}*/

	/*
	private void writeIcon(final Response response) {
		response.setContentType("image/png");
		response.setBinaryContent(icon);
		response.setOk();
	}
	*/

	/*
	private static class FileListPopupStep extends BaseListPopupStep<PsiFile> {
		private final String line;

		private final Project project;

		public FileListPopupStep(final String title, final List<PsiFile> psiFiles, final String line,
				final Project project) {
			super(title, psiFiles);
			this.line = line;
			this.project = project;
		}

		public PopupStep onChosen(final PsiFile selectedValue, final boolean finalChoice) {
			openFile(project, selectedValue, line);
			return null;
		}

		@NotNull
		public String getTextFor(final PsiFile value) {
			String display = value.getName();
			final VirtualFile virtualFile = value.getVirtualFile();

			if (virtualFile != null) {
				display += " (" + virtualFile.getPath() + ")";
			}
			return display;
		}

		public Icon getIconFor(final PsiFile value) {
			final VirtualFile virtualFile = value.getVirtualFile();

			if (virtualFile != null) {
				return virtualFile.getIcon();
			}

			return null;
		}
	}*/

	/*
	private static class FindAndOpenReviewTask extends Task.Modal {
		private final Project project;

		private final String reviewKey;

		private final String serverUrl;

		private final String filePath;

		private final String commentId;

		private ReviewAdapter review;

		public FindAndOpenReviewTask(final Project project, final String title, final boolean cancellable,
				final String reviewKey, final String serverUrl, final String filePath, final String commentId) {

			super(project, title, cancellable);

			this.project = project;
			this.reviewKey = reviewKey;
			this.serverUrl = serverUrl;
			this.filePath = filePath;
			this.commentId = commentId;
		}

		public void run(final ProgressIndicator indicator) {
			indicator.setIndeterminate(true);

			// open review
			review = IdeaHelper.getReviewListToolWindowPanel(project).openReviewWithDetails(reviewKey, serverUrl);

			if (review != null && (isDefined(filePath) || isDefined(commentId))) {
				try {
					// get details for review (files and comments)
					CrucibleServerFacadeImpl.getInstance().getDetailsForReview(review);
				} catch (RemoteApiException e) {
					PluginUtil.getLogger().warn("Error when retrieving review details", e);
					return;
				} catch (ServerPasswordNotProvidedException e) {
					PluginUtil.getLogger().warn("Missing password exception caught when retrieving review details", e);
					return;
				}

				CrucibleFileInfo file = null;

				// find file
				if (isDefined(filePath)) {
					final Set<CrucibleFileInfo> files;
					try {
						files = review.getFiles();
					} catch (ValueNotYetInitialized e) {
						PluginUtil.getLogger().warn("Files collection not available for review", e);
						return;
					}

					for (final CrucibleFileInfo f : files) {
						if (f.getFileDescriptor().getUrl().endsWith(filePath)) {
							file = f;
							break;
						}
					}
				}

				// find comment
				VersionedComment versionedComment = null;
				Comment versionedCommentReply = null;
				Comment generalComment = null;

				if (isDefined(commentId)) {

					// try to find general comment with specified ID
					final List<GeneralComment> generalComments;
					try {
						generalComments = review.getGeneralComments();
					} catch (ValueNotYetInitialized e) {
						PluginUtil.getLogger().warn("General comments collection not available for review", e);
						return;
					}

					for (GeneralComment comment : generalComments) {
						if (comment.getPermId().getId().equals(commentId)) {
							generalComment = comment;
							break;
						}
						boolean commentFound = false;
						for (Comment reply : comment.getReplies()) {
							if (reply.getPermId().getId().equals(commentId)) {
								commentFound = true;
								generalComment = reply;
								break;
							}
						}
						if (commentFound) {
							break;
						}
					}

					// try to find versioned comment with specified ID if general comment not found
					if (file != null && generalComment == null) {
						final List<VersionedComment> versionedComments = file.getVersionedComments();
						for (VersionedComment comment : versionedComments) {
							if (comment.getPermId().getId().equals(commentId)) {
								versionedComment = comment;
								break;
							}
							boolean commentFound = false;
							for (Comment reply : comment.getReplies()) {
								if (reply.getPermId().getId().equals(commentId)) {
									commentFound = true;
									versionedComment = comment;
									versionedCommentReply = reply;
									break;
								}
							}
							if (commentFound) {
								break;
							}
						}
					}
				}

				if (generalComment != null) {
					// select comment in the tree
					CrucibleHelper.selectGeneralComment(project, review, generalComment);
				}

				if (file != null) {
					if (versionedComment == null) {
						// simply open file (versioned comment not found)
						CrucibleHelper.showVirtualFileWithComments(project, review, file);

						// select file in the tree if general comment not selected
						if (generalComment == null) {
							CrucibleHelper.selectFile(project, review, file);
						}
					} else {
						// open file and focus on comment
						CrucibleHelper.openFileOnComment(project, review, file, versionedComment);

						// select comment in the tree
						if (versionedCommentReply != null) {
							CrucibleHelper.selectVersionedComment(project, review, file, versionedCommentReply);
						} else {
							CrucibleHelper.selectVersionedComment(project, review, file, versionedComment);
						}
					}
				}

			}
		}

		public void onSuccess() {
			if (review == null) {
				Messages.showInfoMessage("Cannot find review " + reviewKey, PluginUtil.PRODUCT_NAME);
			} else {
				bringIdeaToFront(project);
			}
		}
	}*/
}
