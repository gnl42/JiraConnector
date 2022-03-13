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

import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrucibleFileInfo implements VersionedFileInfo {
	@Nullable
	private VersionedVirtualFile fileDescriptor;
	@Nullable
	private VersionedVirtualFile oldFileDescriptor;
	private String repositoryName;
	private FileType fileType;
	private String authorName;
	private Date commitDate;
	private CommitType commitType;
	private RepositoryType repositoryType;

	private PermId permId;

	private List<VersionedComment> versionedComments;
	private static final int HASH_NUMBER = 31;

	public CrucibleFileInfo() {
	}

	public CrucibleFileInfo(@Nullable VersionedVirtualFile fileDescriptor,
			@Nullable VersionedVirtualFile oldFileDescriptor,
			@NotNull PermId permId) {
		this.fileDescriptor = fileDescriptor;
		this.oldFileDescriptor = oldFileDescriptor;
		this.permId = permId;
		versionedComments = new ArrayList<VersionedComment>();
	}

	public List<VersionedComment> getVersionedComments() {
		return versionedComments;
	}

	public void setVersionedComments(final List<VersionedComment> versionedComments) {
		this.versionedComments = versionedComments;
	}

	public int getNumberOfCommentsDefects() {
		if (versionedComments == null) {
			return 0;
		}
		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isDefectRaised()) {
				++counter;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDefectRaised()) {
					++counter;
				}
			}
		}
		return counter;
	}

	public int getNumberOfCommentsDefects(final String userName) {
		if (versionedComments == null) {
			return 0;
		}

		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isDefectRaised() && comment.getAuthor().getUsername().equals(userName)) {
				++counter;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDefectRaised() && reply.getAuthor().getUsername().equals(userName)) {
					++counter;
				}
			}
		}

		return counter;
	}

	public int getNumberOfCommentsDrafts() {
		if (versionedComments == null) {
			return 0;
		}
		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isDraft()) {
				++counter;
			}
			counter += comment.getNumberOfDraftReplies();
		}
		return counter;
	}

	public int getNumberOfCommentsDrafts(final String userName) {
		if (versionedComments == null) {
			return 0;
		}

		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isDraft() && comment.getAuthor().getUsername().equals(userName)) {
				++counter;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDraft() && reply.getAuthor().getUsername().equals(userName)) {
					++counter;
				}
			}
		}

		return counter;
	}

	public int getNumberOfLineComments() {
		if (versionedComments == null) {
			return 0;
		}

		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.isFromLineInfo() || comment.isToLineInfo()) {
				++counter;
			}
		}

		return counter;
	}

    public int getNumberOfUnreadComments() {
        if (versionedComments == null) {
            return 0;
        }

        int counter = 0;
        for (VersionedComment comment : versionedComments) {
			if (comment.isEffectivelyUnread()) {
                ++counter;
            }
			counter += comment.getNumberOfUnreadReplies();
        }

        return counter;
    }

    public int getNumberOfComments(final String userName) {
		if (versionedComments == null) {
			return 0;
		}

		int counter = 0;
		for (VersionedComment comment : versionedComments) {
			if (comment.getAuthor().getUsername().equals(userName)) {
				++counter;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.getAuthor().getUsername().equals(userName)) {
					++counter;
				}
			}
		}

		return counter;
	}

	public int getNumberOfComments() {
		if (versionedComments == null) {
			return 0;
		}
		int n = versionedComments.size();
		for (VersionedComment c : versionedComments) {
			n += c.getNumReplies();
		}
		return n;
	}

//	public CrucibleFileInfoImpl(PermId permId) {
//		this(null, null);
//		this.permId = permId;
//	}

	public VersionedVirtualFile getOldFileDescriptor() {
		return oldFileDescriptor;
	}

	public PermId getPermId() {
		return permId;
	}

	public void setOldFileDescriptor(VersionedVirtualFile oldFileDescriptor) {
		this.oldFileDescriptor = oldFileDescriptor;
	}

	public void setFileDescriptor(VersionedVirtualFile fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	public VersionedVirtualFile getFileDescriptor() {
		return fileDescriptor;
	}

	@Override
	public String toString() {
		VersionedVirtualFile oldFile = getOldFileDescriptor();
		VersionedVirtualFile newFile = getFileDescriptor();
		if (oldFile != null && oldFile.getUrl().length() > 0
				&& newFile != null && newFile.getUrl().length() > 0) {
			return oldFile.getUrl() + " (mod)";
		} else if (oldFile != null && oldFile.getUrl().length() > 0) {
			return oldFile.getUrl() + " (del)";
		} else if (newFile != null && newFile.getUrl().length() > 0) {
			return newFile.getUrl() + " (new)";
		} else {
			return "(unknown state)";
		}

	}

//	public void setVersionedComments(List<VersionedComment> commentList) {
//		versionedComments = commentList;
//	}
//
//	public void addVersionedComment(VersionedComment comment) {
//		versionedComments.add(comment);
//	}
//
//
//	public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
//		if (versionedComments == null) {
//			throw new ValueNotYetInitialized("Object trasferred only partially");
//		}
//		return versionedComments;
//	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(final FileType fileType) {
		this.fileType = fileType;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(final String authorName) {
		this.authorName = authorName;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(final Date commitDate) {
		this.commitDate = commitDate;
	}

	public CommitType getCommitType() {
		return commitType;
	}

	public void addComment(final VersionedComment comment) {
		this.versionedComments.add(comment);
	}

	public void setCommitType(final CommitType commitType) {
		this.commitType = commitType;
	}

	public void setFilePermId(final PermId aPermId) {
		this.permId = aPermId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CrucibleFileInfo that = (CrucibleFileInfo) o;

		if (fileType != that.fileType) {
			return false;
		}
		if (!permId.equals(that.permId)) {
			return false;
		}
		if (repositoryName != null ? !repositoryName.equals(that.repositoryName) : that.repositoryName != null) {
			return false;
		}
		if (oldFileDescriptor != null ? !oldFileDescriptor.equals(that.oldFileDescriptor) : that.oldFileDescriptor != null) {
			return false;
		}
		if (fileDescriptor != null ? !fileDescriptor.equals(that.fileDescriptor) : that.fileDescriptor != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (repositoryName != null ? repositoryName.hashCode() : 0);
		result = HASH_NUMBER * result + (fileDescriptor != null ? fileDescriptor.hashCode() : 0);
		result = HASH_NUMBER * result + (oldFileDescriptor != null ? oldFileDescriptor.hashCode() : 0);
		result = HASH_NUMBER * result + (fileType != null ? fileType.hashCode() : 0);
		result = HASH_NUMBER * result + permId.hashCode();
		return result;
	}

	public RepositoryType getRepositoryType() {
		return repositoryType;
	}

	public void setRepositoryType(final RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}
}