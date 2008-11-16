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

package com.atlassian.theplugin.eclipse.view.popup;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.PlatformObject;

import com.atlassian.theplugin.eclipse.view.popup.AbstractTask.PriorityLevel;

/**
 * Top-level Task List element that can contain other Task List elements.
 * 
 * @author Mik Kersten
 * @since 2.0
 */
public abstract class AbstractTaskContainer extends PlatformObject implements Comparable<AbstractTaskContainer> {

	private String handleIdentifier = "";

	private Set<AbstractTask> children = new CopyOnWriteArraySet<AbstractTask>();

	/**
	 * Optional URL corresponding to the web resource associated with this container.
	 */
	protected String url = null;

	public AbstractTaskContainer(String handleAndDescription) {
		assert handleIdentifier != null;
		this.handleIdentifier = handleAndDescription;
	}

	/**
	 * Use {@link TaskList} methods instead.
	 */
	public void internalAddChild(AbstractTask task) {
		children.add(task);
	}

	/**
	 * Use {@link TaskList} methods instead.
	 */
	public void internalRemoveChild(AbstractTask task) {
		children.remove(task);
	}

	/**
	 * Remove all children held by this container
	 * Does not delete tasks from TaskList
	 */
	public void clear() {
		children.clear();
	}

	/**
	 * Removes any cyclic dependencies in children.
	 * 
	 * TODO: review to make sure that this is too expensive, or move to creation.
	 */
	public Set<AbstractTask> getChildren() {
		if (children.isEmpty()) {
			return Collections.emptySet();
		}

		Set<AbstractTask> childrenWithoutCycles = new HashSet<AbstractTask>(children.size());
		for (AbstractTask child : children) {
			if (!child.contains(this.getHandleIdentifier())) {
				childrenWithoutCycles.add(child);
			}
		}
		return childrenWithoutCycles;
	}

	/**
	 * Internal method. Do not use.
	 * 
	 * API-3.0: remove this method (bug 207659)
	 * 
	 * @since 2.2
	 */
	public Set<AbstractTask> getChildrenInternal() {
		return children;
	}

	/**
	 * Maxes out at a depth of 10.
	 * 
	 * TODO: review policy
	 */
	public boolean contains(String handle) {
		return containsHelper(getChildrenInternal(), handle, 0);
	}

	private boolean containsHelper(Set<AbstractTask> children, String handle, int depth) {
		if (depth < ITasksCoreConstants.MAX_SUBTASK_DEPTH && children != null && !children.isEmpty()) {
			for (AbstractTask child : children) {
				if (handle.equals(child.getHandleIdentifier())
						|| containsHelper(child.getChildrenInternal(), handle, depth + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getSummary() {
		return handleIdentifier;
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	public String getHandleIdentifier() {
		return handleIdentifier;
	}

	public void setHandleIdentifier(String handleIdentifier) {
		this.handleIdentifier = handleIdentifier;
	}

	@Override
	public int hashCode() {
		return handleIdentifier.hashCode();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return can be null
	 */
	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object instanceof AbstractTaskContainer) {
			AbstractTaskContainer compare = (AbstractTaskContainer) object;
			return this.getHandleIdentifier().equals(compare.getHandleIdentifier());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "container: " + handleIdentifier;
	}

	public String getPriority() {
		String highestPriority = PriorityLevel.P5.toString();
		Set<AbstractTask> tasks = getChildren();
		if (tasks.isEmpty()) {
			return PriorityLevel.P1.toString();
		}
		for (AbstractTask task : tasks) {
			if (highestPriority.compareTo(task.getPriority()) > 0) {
				highestPriority = task.getPriority();
			}
		}
		return highestPriority;
	}

	/**
	 * The handle for most containers is their summary. Override to specify a different natural ordering.
	 */
	public int compareTo(AbstractTaskContainer taskListElement) {
		return getHandleIdentifier().compareTo(taskListElement.getHandleIdentifier());
	}
	
	/**
	 * If false, user is unable to manipulate (i.e. rename/delete), no preferences are available.
	 * @since 2.3
	 */
	public boolean isUserManaged() {
		return true;
	}
		
}
