package com.atlassian.connector.eclipse.ui.forms;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * A composite that can cache its size for faster layout computation
 * 
 * @author Shawn Minto
 */
public class SizeCachingComposite extends Composite {

	private Point cachedSize = null;

	public SizeCachingComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (cachedSize == null) {
			cachedSize = super.computeSize(wHint, hHint, changed);
		}
		return cachedSize;
	}

	public void clearCache() {
		cachedSize = null;
	}
}