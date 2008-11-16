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

package com.atlassian.theplugin.eclipse.dialogs.bamboo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;

public class LabelBuildDialog {
	
	private Shell shell = null;
	
	private int returnCode = 0;
	private String returnText = null;

	private String buildPlan = null;
	private Text text = null;

	private Composite compositeRowButtons;

	public LabelBuildDialog(Shell parent, BambooBuildAdapterEclipse build) {
		shell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.APPLICATION_MODAL);
		shell.setText("Label Build");
		
		// place the window in the center of parent
		shell.setLocation(parent.getLocation().x + parent.getSize().x / 2, 
				parent.getLocation().y + parent.getSize().y / 2);
		
		this.buildPlan = build.getBuildKey() + " " + build.getBuildNumber();
		
		initialize();
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);
		
		shell.setSize(260, 140);
		createRowUpper();
		createRowBottom();
		createButtons();
	}

	private void createButtons() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		compositeRowButtons = new Composite(shell, SWT.NONE);
		compositeRowButtons.setLayout(gridLayout);
		Button buttonOk = new Button(compositeRowButtons, SWT.NONE);
		buttonOk.setText("Add Label");
		
		buttonOk.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				LabelBuildDialog.this.returnCode = SWT.OK;
				LabelBuildDialog.this.returnText = text.getText();
				shell.close();
			}
			
		});
		
		shell.setDefaultButton(buttonOk);
		
		Button buttonCancel = new Button(compositeRowButtons, SWT.NONE);
		buttonCancel.setText("Cancel");
		buttonCancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				LabelBuildDialog.this.returnCode = SWT.CANCEL;
				shell.close();
			}
			
		});
		
		
	}

	/**
	 * This method initializes composite	
	 *
	 */
	private void createRowUpper() {
		Composite compositeRowUpper = null;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		//GridData gridData = new GridData();
		compositeRowUpper = new Composite(shell, SWT.NONE);
		//compositeRowUpper.setLayoutData(gridData);
		compositeRowUpper.setLayout(gridLayout);
		Label text = new Label(compositeRowUpper, SWT.NONE);
		text.setText("Add label to build " + this.buildPlan);
	}

	/**
	 * This method initializes composite1	
	 *
	 */
	private void createRowBottom() {
		Composite compositeRowBottom = null;
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.widthHint = 220;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		compositeRowBottom = new Composite(shell, SWT.NONE);
		compositeRowBottom.setLayout(gridLayout);
		text = new Text(compositeRowBottom, SWT.BORDER);
		text.setLayoutData(gridData);
	}
	
	public void open() {
		shell.open();
		Display display = shell.getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) { 
				display.sleep();
			}
		}
	}

	public int getReturnCode() {
		return returnCode;
	}

	public String getLabel() {
		return returnText;
	}

}
