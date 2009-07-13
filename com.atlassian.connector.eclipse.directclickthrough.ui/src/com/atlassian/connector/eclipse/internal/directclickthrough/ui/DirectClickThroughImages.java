package com.atlassian.connector.eclipse.internal.directclickthrough.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

public class DirectClickThroughImages {

	public static final URL BASE_URL = DirectClickThroughUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	public static final String PATH_OBJECT = "obj16"; //$NON-NLS-1$
	
 	public static final String PATH_ECLIPSE = PATH_OBJECT + "/eclipse.gif"; //$NON-NLS-1$  
		
	public static final ImageDescriptor ECLIPSE = create(PATH_ECLIPSE); //$NON-NLS-1$

	private static ImageDescriptor create(String path) {
		try {
			return ImageDescriptor.createFromURL(new URL(BASE_URL, path));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

}
