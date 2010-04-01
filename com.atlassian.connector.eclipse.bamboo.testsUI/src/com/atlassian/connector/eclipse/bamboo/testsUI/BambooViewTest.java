/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/
package com.atlassian.connector.eclipse.bamboo.testsUI;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertEnabled;
import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertNotEnabled;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class BambooViewTest extends BambooSWTBotTestCase {

	private static final String BAMBOO_VIEW = "Bamboo";
	
	public void testBambooView() {
		// show Bamboo view
		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.shell("Show View").activate();
		bot.tree().expandNode("Atlassian").select(BAMBOO_VIEW);
		bot.button("OK").click();
		
		// open New Repository wizard
		bot.viewByTitle(BAMBOO_VIEW).toolbarDropDownButton("Add Bamboo Repository...").menuItem("Add New Repository...").click();
		bot.shell("Add Task Repository").activate();
		
		// fill New Repository wizard
		bot.comboBox().setText(TEST_SEVER_INSTANCE);
		bot.text(1).setText(CREDENTIALS.username);
		bot.text(2).setText(CREDENTIALS.password);
		
		bot.button("Validate Settings").click();

		// wait till plans are retrieved
		bot.waitUntil(new ICondition() {
			
			@Override
			public boolean test() throws Exception {
				return bot.button("Favourites").isEnabled();
			}
			
			@Override
			public void init(SWTBot bot) {
			}
			
			@Override
			public String getFailureMessage() {
				return "Favourites button is not enabled.";
			}
		});
		
		// select plans and close wizard
		bot.button("Select All").click();
//		bot.button("Favourites").click();
		bot.button("Finish").click();
		
		// wait till builds are listed in Bamboo view
		bot.waitUntilWidgetAppears(new ICondition() {
			
			@Override
			public boolean test() throws Exception {
				return bot.viewByTitle(BAMBOO_VIEW).bot().tree().isVisible();
			}
			
			@Override
			public void init(SWTBot bot) {
			}
			
			@Override
			public String getFailureMessage() {
				return "Build list is not visible.";
			}
		});
		
		// assert build actions in toolbar are disabled
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.OPEN_WITH_BROWSER_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.SHOW_BUILD_LOG_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.SHOW_TEST_RESULTS_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.RUN_BUILD_ACTION_TOOLTIP));
		
		// assert build actions in context menu are disabled
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_BUILD_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_WITH_BROWSER_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.SHOW_BUILD_LOG_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.SHOW_TEST_RESULTS_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.RUN_BUILD_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.ADD_LABEL_TO_BUILD_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.ADD_COMMENT_TO_BUILD_ACTION_LABEL));
		assertNotEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_REPOSITORY_PROPERTIES_ACTION_LABEL));
		
		// assert there are builds listed in the view
		assertTrue(bot.viewByTitle(BAMBOO_VIEW).bot().tree().rowCount() > 0);
		
		// select first build
		bot.viewByTitle(BAMBOO_VIEW).bot().tree().select(0);
		
		// assert build actions in toolbar are enabled
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.OPEN_WITH_BROWSER_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.SHOW_BUILD_LOG_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).toolbarButton(BambooConstants.RUN_BUILD_ACTION_TOOLTIP));
		
		// assert build actions in context menu are enabled
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_BUILD_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_WITH_BROWSER_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.SHOW_BUILD_LOG_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.RUN_BUILD_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.ADD_LABEL_TO_BUILD_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.ADD_COMMENT_TO_BUILD_ACTION_LABEL));
		assertEnabled(bot.viewByTitle(BAMBOO_VIEW).bot().tree().contextMenu(BambooConstants.OPEN_REPOSITORY_PROPERTIES_ACTION_LABEL));
	}
}
