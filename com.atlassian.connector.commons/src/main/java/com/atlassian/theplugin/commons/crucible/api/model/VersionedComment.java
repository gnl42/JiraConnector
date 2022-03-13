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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class VersionedComment extends Comment {
	private PermId reviewItemId;

	private int fromStartLine;

	private int fromEndLine;

	private boolean fromLineInfo;

	private int toStartLine;

	private int toEndLine;

	private boolean toLineInfo;

	private IntRanges toLineRanges;

	private IntRanges fromLineRanges;

    private Map<String, IntRanges> lineRanges;

	@NotNull
	private final CrucibleFileInfo crucibleFileInfo;

	public VersionedComment(VersionedComment bean) {
		super(bean);
		if (bean.isFromLineInfo()) {
			setFromLineInfo(true);
			setFromStartLine(bean.getFromStartLine());
			setFromEndLine(bean.getFromEndLine());
		}
		if (bean.isToLineInfo()) {
			setToLineInfo(true);
			setToStartLine(bean.getToStartLine());
			setToEndLine(bean.getToEndLine());
		}
		this.crucibleFileInfo = bean.crucibleFileInfo;
	}

	public VersionedComment(Review review, @NotNull CrucibleFileInfo crucibleFileInfo) {
		super(review, null); // I assume that versioned comments are always root comments (not replies)
		this.crucibleFileInfo = crucibleFileInfo;
	}

	@NotNull
	public CrucibleFileInfo getCrucibleFileInfo() {
		return crucibleFileInfo;
	}

	public PermId getReviewItemId() {
		return reviewItemId;
	}

	/**
	 * I am goint to remove this method as soon as refactoring around injecting here parent CrucibleFileInfo is finished
	 */
	@Deprecated
	public void setReviewItemId(PermId reviewItemId) {
		this.reviewItemId = reviewItemId;
	}

	@Deprecated
	public int getFromStartLine() {
		return fromStartLine;
	}

	public void setFromStartLine(int startLine) {
		this.fromStartLine = startLine;
	}

	@Deprecated
	public int getFromEndLine() {
		return fromEndLine;
	}

	@Deprecated
	public IntRanges getFromLineRanges() {
		return fromLineRanges;
	}

	@Override
	protected Comment createReplyBean(Comment reply) {
		return new GeneralComment(reply);
	}

	public void setFromLineRanges(final IntRanges fromLineRanges) {
		this.fromLineRanges = fromLineRanges;
		setFromLineInfo(true);
		setFromStartLine(fromLineRanges.getTotalMin());
		setFromEndLine(fromLineRanges.getTotalMax());
	}

	@Deprecated
	public IntRanges getToLineRanges() {
		return toLineRanges;
	}

    public Map<String, IntRanges> getLineRanges() {
        return lineRanges;
    }

    public void setLineRanges(Map<String, IntRanges> lineRanges) {
        this.lineRanges = lineRanges;
    }

    public void setToLineRanges(IntRanges toLineRanges) {
		this.toLineRanges = toLineRanges;
		setToLineInfo(true);
		setToStartLine(toLineRanges.getTotalMin());
		setToEndLine(toLineRanges.getTotalMax());
	}

	public void setFromEndLine(int endLine) {
		this.fromEndLine = endLine;
	}

	@Deprecated
	public int getToStartLine() {
		return toStartLine;
	}

	public void setToStartLine(int startLine) {
		this.toStartLine = startLine;
	}

	@Deprecated
	public int getToEndLine() {
		return toEndLine;
	}

	public void setToEndLine(int endLine) {
		this.toEndLine = endLine;
	}

	@Deprecated
	public boolean isFromLineInfo() {
		return fromLineInfo;
	}

	public void setFromLineInfo(boolean fromLineInfo) {
		this.fromLineInfo = fromLineInfo;
	}

	@Deprecated
	public boolean isToLineInfo() {
		return toLineInfo;
	}

	public void setToLineInfo(boolean toLineInfo) {
		this.toLineInfo = toLineInfo;
	}

	private static final int HASH_INT = 31;

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = HASH_INT * result + (lineRanges != null ? lineRanges.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof VersionedComment)) {
			return false;
		}
		VersionedComment other = (VersionedComment) obj;
		if (!MiscUtil.isEqual(crucibleFileInfo, other.crucibleFileInfo)) {
			return false;
		}

		if (fromEndLine != other.fromEndLine) {
			return false;
		}
		if (fromLineInfo != other.fromLineInfo) {
			return false;
		}

		if (!MiscUtil.isEqual(fromLineRanges, other.fromLineRanges)) {
			return false;
		}

		if (fromStartLine != other.fromStartLine) {
			return false;
		}

		if (!MiscUtil.isEqual(lineRanges, other.lineRanges)) {
			return false;
		}

		if (!MiscUtil.isEqual(reviewItemId, other.reviewItemId)) {
			return false;
		}

		if (toEndLine != other.toEndLine) {
			return false;
		}
		if (toLineInfo != other.toLineInfo) {
			return false;
		}

		if (!MiscUtil.isEqual(toLineRanges, other.toLineRanges)) {
			return false;
		}

		if (toStartLine != other.toStartLine) {
			return false;
		}
		return true;
	}

	public boolean deepEquals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof VersionedComment)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		VersionedComment that = (VersionedComment) o;

		if (fromEndLine != that.fromEndLine) {
			return false;
		}
		if (fromLineInfo != that.fromLineInfo) {
			return false;
		}
		if (fromStartLine != that.fromStartLine) {
			return false;
		}
		if (toEndLine != that.toEndLine) {
			return false;
		}
		if (toLineInfo != that.toLineInfo) {
			return false;
		}
		if (toStartLine != that.toStartLine) {
			return false;
		}

        if (lineRangesNotEqual(that.lineRanges)) {
            return false;
        }

		if (getReplies() != null ? !getReplies().equals(that.getReplies()) : that.getReplies() != null) {
			return false;
		}
		if (!reviewItemId.equals(that.reviewItemId)) {
			return false;
		}

		if (getReplies().size() != that.getReplies().size()) {
			return false;
		}

		for (Comment vc : getReplies()) {
			boolean found = false;
			for (Comment tvc : that.getReplies()) {
				if (vc.getPermId() == tvc.getPermId() && ((VersionedComment) vc).deepEquals(vc)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}

    private boolean lineRangesNotEqual(Map<String, IntRanges> thatLineRanges) {
        if (lineRanges == null && thatLineRanges == null) {
            return true;
        }
        if (lineRanges == null) {
            return false;
        }
        if (thatLineRanges == null) {
            return false;
        }
        return lineRanges.equals(thatLineRanges);
    }

}