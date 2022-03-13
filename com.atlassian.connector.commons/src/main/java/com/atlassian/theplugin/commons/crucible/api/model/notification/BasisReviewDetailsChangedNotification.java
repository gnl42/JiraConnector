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
package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import org.jetbrains.annotations.NotNull;

public class BasisReviewDetailsChangedNotification extends AbstractReviewNotification {
	private final CrucibleNotificationType notificationType;
	private final String message;

	public BasisReviewDetailsChangedNotification(@NotNull Review review,
			@NotNull CrucibleNotificationType notificationType, @NotNull String message) {
		super(review);
		this.notificationType = notificationType;
		this.message = message;
	}

	@Override
	public CrucibleNotificationType getType() {
		return notificationType;
	}

	@Override
	public String getPresentationMessage() {
		return message;
	}
}
