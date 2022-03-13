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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.VirtualFileSystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BambooChangeSetImpl implements BambooChangeSet {
	private String author;
	private Date commitDate;
	private String comment;
	private List<BambooFileInfo> files;
	private VirtualFileSystem virtualFileSystem;

	public BambooChangeSetImpl() {
		files = new ArrayList<BambooFileInfo>();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCommitDate() {
		return new Date(commitDate.getTime());
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = new Date(commitDate.getTime());
	}

	public List<BambooFileInfo> getFiles() {
		return files;
	}

	public void addCommitFile(BambooFileInfo file) {
		files.add(file);
	}

	public VirtualFileSystem getVirtualFileSystem() {
		if (virtualFileSystem == null) {
			virtualFileSystem = new VirtualFileSystem();
		}
		return virtualFileSystem;
	}

	public void setVirtualFileSystem(VirtualFileSystem virtualFileSystem) {
		this.virtualFileSystem = virtualFileSystem;
	}
}
