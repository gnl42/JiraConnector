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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public final class SwtUtil {
	
	private SwtUtil() {
	}

	public static final long FADE_RESCHEDULE_DELAY = 80;

	public static final int FADE_IN_INCREMENT = 15;

	public static final int FADE_OUT_INCREMENT = -20;

	/**
	 * API-3.0: get rid of reflection on 3.4 branch
	 */
	public static boolean setAlpha(Shell shell, int value) {
		Method method = null;
		try {
			method = shell.getClass().getMethod("setAlpha", new Class[] { int.class });
			method.setAccessible(true);
			//shell.setAlpha(value);
			method.invoke(shell, new Object[] { value });
			return true;
		} catch (Exception e) {
			// ignore, not supported on Eclipse 3.3
			return false;
		}
	}

	/**
	 * API-3.0: get rid of reflection on 3.4 branch
	 */
	public static int getAlpha(Shell shell) {
		Method method = null;
		try {
			method = shell.getClass().getMethod("getAlpha");
			method.setAccessible(true);
			return (Integer) method.invoke(shell);
		} catch (Exception e) {
			return 0xFF;
		}
	}

	public static FadeJob fadeIn(Shell shell, IFadeListener listener) {
		return new FadeJob(shell, FADE_IN_INCREMENT, FADE_RESCHEDULE_DELAY, listener);		
	}

	public static FadeJob fadeOut(Shell shell, IFadeListener listener) {
		return new FadeJob(shell, FADE_OUT_INCREMENT, FADE_RESCHEDULE_DELAY, listener);		
	}

	/**
	 * API-3.0: get rid of reflection on 3.4 branch
	 */
	public static void fade(Shell shell, boolean fadeIn, int increment, int speed) {
		try {
			Method method = shell.getClass().getMethod("setAlpha", new Class[] { int.class });
			method.setAccessible(true);

			if (fadeIn) {
				for (int i = 0; i <= 255; i += increment) {
					// shell.setAlpha(i);
					method.invoke(shell, new Object[] { i });
					try {
						Thread.sleep(speed);
					} catch (InterruptedException e) {
						// ignore
					}
				}
				// shell.setAlpha(255);
				method.invoke(shell, new Object[] { 255 });
			} else {
				for (int i = 244; i >= 0; i -= increment) {
					// shell.setAlpha(i);
					method.invoke(shell, new Object[] { i });
					try {
						Thread.sleep(speed);
					} catch (InterruptedException e) {
						// ignore
					}
				}
				// shell.setAlpha(0);
				method.invoke(shell, new Object[] { 0 });
			}
		} catch (Exception e) {
			// ignore, not supported on Eclipse 3.3
		}
	}

	public static class FadeJob extends Job {

		private static final int FADE_CONSTANT = 255;

		private final Shell shell;

		private final int increment;

		private volatile boolean stopped;

		private volatile int currentAlpha;

		private final long delay;

		private final IFadeListener fadeListener;

		public FadeJob(Shell shell, int increment, long delay, IFadeListener fadeListener) {
			super("Fading");
			if (increment < -FADE_CONSTANT || increment == 0 || increment > FADE_CONSTANT) {
				throw new IllegalArgumentException("-255 <= increment <= 255 && increment != 0");
			}
			if (delay < 1) {
				throw new IllegalArgumentException("delay must be > 0");
			}
			this.currentAlpha = getAlpha(shell);
			this.shell = shell;
			this.increment = increment;
			this.delay = delay;
			this.fadeListener = fadeListener;

			setSystem(true);
			schedule(delay);
		}

		@Override
		protected void canceling() {
			stopped = true;
		}

		private void reschedule() {
			if (stopped) {
				return;
			}
			schedule(delay);
		}

		public void cancelAndWait(final boolean setAlpha) {
			if (stopped) {
				return;
			}
			cancel();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (setAlpha) {
						SwtUtil.setAlpha(shell, getLastAlpha());
					}
				}				
			});				
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (stopped) { 
				return Status.OK_STATUS;
			}
			
			currentAlpha += increment;
			if (currentAlpha <= 0) {
				currentAlpha = 0;
			} else if (currentAlpha >= 255) {
				currentAlpha = 255;
			}

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (stopped) { 
						return;
					}
					
					if (shell.isDisposed()) {
						stopped = true;
						return;
					}
					
					if (!SwtUtil.setAlpha(shell, currentAlpha)) {
						// just in case it failed for some other reason than lack of support on the platform
						currentAlpha = getLastAlpha();
						SwtUtil.setAlpha(shell, currentAlpha);
						stopped = true;
					}
					
					if (fadeListener != null) {
						fadeListener.faded(shell, currentAlpha);
					}
				}				
			});

			if (currentAlpha == 0 || currentAlpha == 255) {
				stopped = true;
			}

			reschedule();
			return Status.OK_STATUS;
		}

		private int getLastAlpha() {
			return (increment < 0) ? 0 : 255;
		}

	}

	public static interface IFadeListener {
		
		void faded(Shell shell, int alpha);
		
	}
	
}
