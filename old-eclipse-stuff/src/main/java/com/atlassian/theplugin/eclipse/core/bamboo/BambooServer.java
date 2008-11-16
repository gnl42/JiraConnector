package com.atlassian.theplugin.eclipse.core.bamboo;

import java.io.Serializable;

public class BambooServer implements Serializable, IBambooServer {
	private static final long serialVersionUID = -4407899892845483487L;
	private String id;
	private String label;
	private String username;
	private String password;
	private boolean passwordSaved;
	private String url;

	public BambooServer() {
	
	}
	
	public BambooServer(String id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#getId()
	 */
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#getLabel()
	 */
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#getUsername()
	 */
	public String getUsername() {
		return username;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#getPassword()
	 */
	public String getPassword() {
		return password;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#isPasswordSaved()
	 */
	public boolean isPasswordSaved() {
		return passwordSaved;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#setPasswordSaved(boolean)
	 */
	public void setPasswordSaved(boolean passwordSaved) {
		this.passwordSaved = passwordSaved;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#getUrl()
	 */
	public String getUrl() {
		return url;
	}
	
	/* (non-Javadoc)
	 * @see com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer#setUrl(java.lang.String)
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IBambooServer))
			return false;
		final IBambooServer other = (IBambooServer) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}

}
