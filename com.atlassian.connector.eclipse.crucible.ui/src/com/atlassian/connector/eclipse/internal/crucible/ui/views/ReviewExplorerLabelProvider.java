package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.FileType;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A simple label provider
 */
public class ReviewExplorerLabelProvider extends AbstractCrucibleReviewItemLabelProvider implements
		IStyledLabelProvider {

	public String getText(Object element) {
		return getStyledText(element).toString();
	}

	@Override
	public Font getFont(Object element) {
		if (element instanceof Comment) {
			final Comment comment = (Comment) element;
			if (comment.getReadState().equals(ReadState.UNREAD)
					|| comment.getReadState().equals(ReadState.LEAVE_UNREAD)) {
				return CommonFonts.BOLD;
			}
			if (comment.isDraft()) {
				return CommonFonts.ITALIC;
			}
		}
		return super.getFont(element);
	}

	public StyledString getStyledText(Object element) {
		if (element instanceof ReviewTreeNode) {
			ReviewTreeNode node = (ReviewTreeNode) element;
			final CrucibleFileInfo cfi = node.getCrucibleFileInfo();

			if (cfi != null) {
				return getStyledText(cfi);
			}
		}
		if (element instanceof Comment) {
			Comment comment = (Comment) element;
			final String headerText;

			String msg = comment.getMessage();
			if (msg.length() > ReviewExplorerView.COMMENT_PREVIEW_LENGTH) {
				headerText = msg.substring(0, ReviewExplorerView.COMMENT_PREVIEW_LENGTH) + "...";
			} else {
				headerText = msg;
			}
			return new StyledString(headerText);
		}
		if (element instanceof CrucibleFileInfo) {
			return getStyledText((CrucibleFileInfo) element);
		}
		if (element instanceof Review) {
			return new StyledString(((Review) element).getPermId().toString());
		}

		return element == null ? new StyledString("") : new StyledString(element.toString());
	}

	private StyledString getStyledText(CrucibleFileInfo file) {
		StyledString styledString = new StyledString();
		styledString.append(file.getFileDescriptor().getName());

		final int numberOfComments = file.getNumberOfComments();
		if (numberOfComments > 0) {
			styledString.append(" " + numberOfComments, StyledString.DECORATIONS_STYLER);
			final int numberOfUnreadComments = file.getNumberOfUnreadComments();
			if (numberOfUnreadComments > 0) {
				styledString.append(" (");
				styledString.append(Integer.toString(numberOfUnreadComments), BOLD_FONT_DECORATION_STYLER);
				styledString.append(")");
			}
		}

		// StringBuilder revisionString = getRevisionInfo(file);
		// if (revisionString.length() > 0) {
		// styledString.append("  ");
		// styledString.append(revisionString.toString(), StyledString.DECORATIONS_STYLER);
		// }
		return styledString;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof CrucibleFileInfo) {
			final CrucibleFileInfo cfi = (CrucibleFileInfo) element;
			return getImage(cfi);
		}
		if (element instanceof Comment) {
			return getImage((Comment) element);
		}
		if (element instanceof ReviewTreeNode) {
			ReviewTreeNode reviewTreeNode = (ReviewTreeNode) element;
			if (reviewTreeNode.getCrucibleFileInfo() != null && reviewTreeNode.getChildren().isEmpty()) {
				return getImage(reviewTreeNode.getCrucibleFileInfo());
			}
			return CrucibleImages.getImage(new OffsettingCompositeImageDescriptor(PlatformUI.getWorkbench()
					.getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), null));
		}
		return null;
	}

	private Image getImage(final CrucibleFileInfo cfi) {
		IEditorRegistry fEditorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		final ImageDescriptor imageDescriptor = fEditorRegistry.getImageDescriptor(cfi.getFileDescriptor()
				.getName());

		// we want to leave some space for defect decorations (which are nicer and more readable when
		// applied besides the main icon, so we use the trick with images by shifting them a little bit
		// thus we use OffsettingCompositeImageDescriptor
		return CrucibleImages.getImage(new OffsettingCompositeImageDescriptor(imageDescriptor, null));
		// return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}

	private StringBuilder getRevisionInfo(CrucibleFileInfo file) {

		final VersionedVirtualFile oldFileDescriptor = file.getOldFileDescriptor();
		final VersionedVirtualFile newFileDescriptor = file.getFileDescriptor();

		boolean oldFileHasRevision = oldFileDescriptor != null && oldFileDescriptor.getRevision() != null
				&& oldFileDescriptor.getRevision().length() > 0;
		boolean oldFileHasUrl = oldFileDescriptor != null && oldFileDescriptor.getUrl() != null
				&& oldFileDescriptor.getUrl().length() > 0;

		boolean newFileHasRevision = newFileDescriptor != null && newFileDescriptor.getRevision() != null
				&& newFileDescriptor.getRevision().length() > 0;
		boolean newFileHasUrl = newFileDescriptor != null && newFileDescriptor.getUrl() != null
				&& newFileDescriptor.getUrl().length() > 0;

		FileType filetype = file.getFileType();

		StringBuilder revisionString = new StringBuilder();

		// if repository type is uploaded or patch, display alternative for now since we cannot open the file yet
		if (file.getRepositoryType() == RepositoryType.PATCH) {
			revisionString.append("Part of a Patch");
		} else if (file.getRepositoryType() == RepositoryType.UPLOAD) {
			revisionString.append("Pre-commit");
		} else {
			// if file is deleted or not a file, do not include any revisions
			// (we need a local resource to retrieve the old revision from SVN, which we do not have)
			if (file.getCommitType() == CommitType.Deleted || filetype != FileType.File) {
				revisionString.append("N/A ");
			} else {
				if (oldFileHasUrl && oldFileHasRevision) {
					revisionString.append(oldFileDescriptor.getRevision());
				}
				if (oldFileHasRevision) {
					if (newFileHasRevision) {
						revisionString.append("-");
					}
				}

				if (newFileHasUrl && newFileHasRevision && file.getCommitType() != CommitType.Deleted) {
					revisionString.append(newFileDescriptor.getRevision());
				}
			}
		}
		return revisionString;
	}
}