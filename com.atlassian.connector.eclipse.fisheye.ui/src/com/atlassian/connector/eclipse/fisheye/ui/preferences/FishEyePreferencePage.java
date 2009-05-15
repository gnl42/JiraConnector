package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.Iterator;
import java.util.List;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class FishEyePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public FishEyePreferencePage() {
		super("FishEye Preferences");
		setPreferenceStore(FishEyeUiPlugin.getDefault().getPreferenceStore());
		setDescription("Add, remove or edit FishEye mapping configuration.");
		noDefaultAndApplyButton();
		int a = 50 + 14230;
		System.out.println(a);
	}

//	/**
//	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
//	 * types of preferences. Each field editor knows how to save and restore itself.
//	 */
//	public void createFieldEditors() {
//		addField(new StringFieldEditor(PreferenceConstants.SERVER_URL, "Default &FishEye server:",
//				getFieldEditorParent()));
////		addField(new DirectoryFieldEditor(PreferenceConstants.SERVER_URL, "Default &FishEye server:",
////				getFieldEditorParent()));
//		addField(new StringFieldEditor(PreferenceConstants.REPO, "&Default repository:", getFieldEditorParent()));
//
//		addField(new StringFieldEditor(PreferenceConstants.P_BOOLEAN, "&Project path", getFieldEditorParent()));
//
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(ancestor);

		Composite parent = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

		final TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
//		Table table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
//		table.setLinesVisible(true);
//		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getControl());
		Composite panel = new Composite(parent, SWT.NONE);
		RowLayout panelLayout = new RowLayout(SWT.VERTICAL);
		panelLayout.fill = true;
		panel.setLayout(panelLayout);

		GridDataFactory.fillDefaults().grab(false, false).applyTo(panel);
		final Button addButton = new Button(panel, SWT.PUSH);
		addButton.setText("Add");
		final Button removeButton = new Button(panel, SWT.PUSH);
		removeButton.setText("Remove");
		final Button editButton = new Button(panel, SWT.PUSH);
		editButton.setText("Edit");

		final List<FishEyeMappingConfiguration> mapping = FishEyeUiPlugin.getDefault()
				.getFishEyeSettingsManager()
				.getMappings();
//			MiscUtil.buildArrayList(new FishEyeMappingConfiguration[] {
//				new FishEyeMappingConfiguration("https://fsdfsd", "server1", "repo1"),
//				new FishEyeMappingConfiguration("https://fdsfsdfsd", "server2", "repo1"),
//				new FishEyeMappingConfiguration("https://fsdfsd.com", "server1", "repo2") });

		String[] titles = { "SCM Path", "FishEye Server", "FishEye Repository" };
		int[] bounds = { 100, 200, 100 };
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		tableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return mapping.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		tableViewer.setLabelProvider(new TableLabelProvider());

		Table tableControl = tableViewer.getTable();
		tableControl.setHeaderVisible(true);
		tableControl.setLinesVisible(true);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
					editButton.setEnabled(ssel.size() == 1);
					removeButton.setEnabled(ssel.size() > 0);
				}

			}

		});
//		for (int i = 0; i < 100; i++) {
//			TableItem item = new TableItem(table, SWT.NONE);
//			item.setText(0, "x");
//			item.setText(1, "y");
//			item.setText(2, "!");
//		}

//		for (int i = 0; i < titles.length; i++) {
//			table.getColumn(i).pack();
//		}
		tableViewer.setInput(mapping);
		tableViewer.setSelection(null);
		addButton.setFocus();
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				AddFishEyeMappingDialog dialog = new AddFishEyeMappingDialog(getShell());
				if (dialog.open() == Window.OK) {
					final FishEyeMappingConfiguration cfg = dialog.getCfg();
					if (cfg != null) {
						mapping.add(cfg);
						tableViewer.refresh();
					}
				}
			}

		});
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ssel = (IStructuredSelection) tableViewer.getSelection();
				for (Iterator<?> it = ssel.iterator(); it.hasNext();) {
					mapping.remove(it.next());
				}
				tableViewer.refresh();
			}
		});

		tableViewer.getControl().setSize(tableViewer.getControl().computeSize(SWT.DEFAULT, 200));

		return ancestor;
	}

}