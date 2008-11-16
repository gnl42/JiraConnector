package com.atlassian.theplugin.eclipse.core.bamboo;

public interface IBambooServer {

	String getId();

	String getLabel();

	void setLabel(String label);

	String getUsername();

	void setUsername(String username);

	String getPassword();

	void setPassword(String password);

	boolean isPasswordSaved();

	void setPasswordSaved(boolean passwordSaved);

	String getUrl();

	void setUrl(String url);
}