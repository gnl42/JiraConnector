package com.atlassian.connector.eclipse.internal.crucible.ui.views;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A simple label provider
 */
public class ReviewExplorerLabelProvider extends AbstractCrucibleReviewItemLabelProvider implements
		IStyledLabelProvider {

	private static final String READ_COLOR_KEY = "Review Explorer Read Comment Color";

	private final ReviewExplorerView reviewExplorerView;

	public ReviewExplorerLabelProvider(ReviewExplorerView view) {
		this.reviewExplorerView = view;
		JFaceResources.getColorRegistry().put(READ_COLOR_KEY, new RGB(100, 100, 100));
	}

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
			if (reviewExplorerView.isFocusedOnUnreadComments() && !comment.isEffectivelyUnread()) {
				return new StyledString(headerText, StyledString.createColorRegistryStyler(READ_COLOR_KEY, null));
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
		final ImageDescriptor imageDescriptor = fEditorRegistry.getImageDescriptor(cfi.getFileDescriptor().getName());

		// we want to leave some space for defect decorations (which are nicer and more readable when
		// applied besides the main icon, so we use the trick with images by shifting them a little bit
		// thus we use OffsettingCompositeImageDescriptor
		return CrucibleImages.getImage(new OffsettingCompositeImageDescriptor(imageDescriptor, null));
	}

}