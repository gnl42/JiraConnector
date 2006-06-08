/**
 * 
 */
package org.eclipse.mylar.internal.jira;

import org.eclipse.mylar.internal.tasklist.AbstractAttributeFactory;

/**
 * @author Mik Kersten
 */
public class JiraAttributeFactory extends AbstractAttributeFactory {
	
	private static final long serialVersionUID = -4685044081450189855L;

	@Override
	public boolean getIsHidden(String key) {
		return false;
	}

	@Override
	public String getName(String key) {
		return key;
	}

	@Override
	public String mapCommonAttributeKey(String key) {
		return key;
	}

	@Override
	public boolean isReadOnly(String key) {				
		return false;
	}
}