/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.text.ParseException;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
 */
public class JiraEditorUtil {

    private static final String DECORATOR_KEY = "DECORATOR"; //$NON-NLS-1$

    public static void setTimeSpentDecorator(final Text toDecorate, final boolean isZeroValid, final TaskRepository repository, final boolean isEmptyValid) {
        boolean invalid = false;

        if (isEmptyValid && (toDecorate.getText() == null || toDecorate.getText().length() == 0)) {
            invalid = false;
        } else {
            try {
                final long amount = JiraUtil.getTimeFormat(repository).parse(toDecorate.getText());
                invalid = isZeroValid ? amount < 0 : amount < 1;
            } catch (final ParseException e) {
                invalid = true;
            }
        }

        showTimeSpentDecorator(toDecorate, repository, invalid);
    }

    public static void showTimeSpentDecorator(final Text toDecorate, final TaskRepository repository, final boolean show) {
        final String errorDescription = NLS.bind(Messages.JiraEditorUtil_Time_Spent_Error_Decorator_Hover, JiraUtil.getWorkDaysPerWeek(repository),
                JiraUtil.getWorkHoursPerDay(repository));
        showTimeSpentDecorator(toDecorate, repository, errorDescription, show);
    }

    public static void showTimeSpentDecorator(final Text toDecorate, final TaskRepository repository, final String errorDescription, final boolean show) {
        if (toDecorate == null || toDecorate.isDisposed()) {
            return;
        }
        final String decorationId = FieldDecorationRegistry.DEC_ERROR;
        ControlDecoration amountTextControlDecoration = null;
        if (toDecorate.getData(DECORATOR_KEY) instanceof ControlDecoration) {
            amountTextControlDecoration = (ControlDecoration) toDecorate.getData(DECORATOR_KEY);
        }
        if (amountTextControlDecoration == null) {
            amountTextControlDecoration = new ControlDecoration(toDecorate, SWT.TOP | SWT.LEFT);
            toDecorate.setData(DECORATOR_KEY, amountTextControlDecoration);
            amountTextControlDecoration.setShowOnlyOnFocus(false);
        }
        amountTextControlDecoration.setDescriptionText(errorDescription);

        final FieldDecoration errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(decorationId);
        amountTextControlDecoration.setImage(errorImage.getImage());
        if (show) {
            amountTextControlDecoration.show();
        } else {
            amountTextControlDecoration.hide();
        }
    }

}
