package org.eclipse.mylyn.monitor.core;

@SuppressWarnings("serial")
public class UserInteractionEvent extends InteractionEvent {

	private final String uid;

	public UserInteractionEvent(Kind kind, String structureKind, String handle,
			String originId, String uid) {
		super(kind, structureKind, handle, originId);
		this.uid = uid;
	}

	public UserInteractionEvent(InteractionEvent ie, String uid) {
		this(ie.getKind(), ie.getStructureKind(), ie.getStructureHandle(), ie
				.getOriginId(), uid);
	}

	public String getUid() {
		return uid;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof UserInteractionEvent)) {
			return false;
		}
		UserInteractionEvent event = (UserInteractionEvent) object;

		if (super.equals(object) && uid == null ? event.uid == null : uid
				.equals(event.uid)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if (uid != null) {
			hashCode += uid.hashCode();
		}
		return hashCode;
	}

}
