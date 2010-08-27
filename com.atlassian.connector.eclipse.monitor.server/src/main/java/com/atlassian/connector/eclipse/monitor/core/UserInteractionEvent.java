package com.atlassian.connector.eclipse.monitor.core;

import java.util.Date;

@SuppressWarnings("serial")
public class UserInteractionEvent extends InteractionEvent {

	private final String uid;
	
	private Integer id;

	public UserInteractionEvent(Kind kind, String pluginId, String eventId, String details, Date startDate, String uid, Integer id) {
		super(kind, pluginId, eventId, details, startDate);
			
		this.uid = uid;
		this.id = id;
	}

	public UserInteractionEvent(InteractionEvent ie, String uid, Integer id) {
		this(ie.getKind(), ie.getPluginId(), ie.getEventId(), ie.getDetails(), ie.getDate(), uid, id);
	}

	public String getUid() {
		return uid;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof UserInteractionEvent)) {
			return false;
		}
		UserInteractionEvent event = (UserInteractionEvent) object;

		if (!super.equals(object)) {
			return false;
		}
		
		if ((uid == null && event.uid != null)
				|| !uid.equals(event.uid)) {
			return false;
		}
		
		if ((id == null && event.id != null)
				|| !id.equals(event.id)) {
			return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if (uid != null) {
			hashCode += uid.hashCode();
		}
		if (id != null) {
			hashCode += id.hashCode();
		}
		return hashCode;
	}

}
