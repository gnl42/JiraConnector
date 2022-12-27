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

package me.glindholm.theplugin.commons;

import java.util.TimerTask;

public interface SchedulableChecker {
	/**
	 * Create a new instance of TimerTask for this component so it can be used for (re)scheduling in
	 * {@link java.util.Timer}
	 * @return new instance of TimerTask
	 */
	TimerTask newTimerTask();

	/**
	 * Provides info whether the component should be scheduled - ie. if there is any work for it to be done on timer.
	 * @return true when this component wants to be scheduled.
	 */
	boolean canSchedule();

	/**
	 * Preferred scheduling interval in milliseconds.
	 * <p>
	 * Will be used for {@link java.util.Timer#schedule(java.util.TimerTask, long)}.
	 *  
	 * @return Preferred scheduling interval in milliseconds.
	 */
	long getInterval();

	/**
	 * Resets state of registered listeners (sets state to default/empty) 
	 */
	void resetListenersState();

	/**
	 *
	 * @return name of the checker which can be used for example in UI
	 */
	String getName();
}
