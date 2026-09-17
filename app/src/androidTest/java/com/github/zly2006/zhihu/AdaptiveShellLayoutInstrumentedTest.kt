/*
 * Zhihu++ - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.IntRect
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.zly2006.zhihu.platform.WindowHinge
import com.github.zly2006.zhihu.platform.WindowHingeOrientation
import com.github.zly2006.zhihu.test.resetAppPreferences
import com.github.zly2006.zhihu.test.setZhihuMainContent
import com.github.zly2006.zhihu.ui.PREFERENCE_NAME
import com.github.zly2006.zhihu.ui.adaptive.DEFAULT_LIST_PANE_WIDTH_DP
import com.github.zly2006.zhihu.ui.adaptive.HINGE_SIDE_PADDING_DP
import com.github.zly2006.zhihu.ui.adaptive.LARGE_SCREEN_LAYOUT_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.adaptive.LargeScreenLayoutPreference
import com.github.zly2006.zhihu.ui.adaptive.MIN_LIST_PANE_WIDTH_DP
import com.github.zly2006.zhihu.ui.adaptive.NAVIGATION_RAIL_WIDTH_DP
import com.github.zly2006.zhihu.ui.adaptive.WindowLayoutOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

/**
 * 主壳自适应形态的仪器验证：折叠屏各档宽度与铰链姿态下，导航形态和双栏必须按预期出现。
 *
 * 模拟器是直板尺寸，所以用 [WindowLayoutOverride] 注入内屏/外屏的逻辑宽度与铰链姿态。
 * 真实像素仍按设备算，宽屏用例因此只断言节点存在与相对几何，不断言整屏可见；
 * 铰链用例刻意把列表栏收窄到可读下限，保证两栏在真实窗口里都还有可见空间。
 */
@RunWith(AndroidJUnit4::class)
class AdaptiveShellLayoutInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val paneWidthToleranceDp = 2f

    @Before
    fun resetPreferences() {
        composeRule.resetAppPreferences()
    }

    @Test
    fun outerScreenKeepsBottomBarWithoutPanes() {
        composeRule.setZhihuMainContent(windowLayout = WindowLayoutOverride(widthDp = 445f))

        composeRule.onNodeWithTag("nav_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("main_navigation_rail").assertDoesNotExist()
        composeRule.onNodeWithTag("main_list_pane").assertDoesNotExist()
        composeRule.onNodeWithTag("main_detail_pane").assertDoesNotExist()
    }

    @Test
    fun innerScreenPortraitUsesRailWithoutSplitting() {
        composeRule.setZhihuMainContent(windowLayout = WindowLayoutOverride(widthDp = 637f))

        composeRule.onNodeWithTag("main_navigation_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("main_list_pane").assertDoesNotExist()
        composeRule.onNodeWithTag("main_detail_pane").assertDoesNotExist()
    }

    @Test
    fun innerScreenLandscapeSplitsListAndDetail() {
        composeRule.setZhihuMainContent(windowLayout = WindowLayoutOverride(widthDp = 900f))

        composeRule.onNodeWithTag("main_list_pane").assertExists()
        composeRule.onNodeWithTag("main_detail_pane").assertExists()
        composeRule.onNodeWithTag("main_detail_placeholder").assertExists()
        val listPaneBounds = composeRule.onNodeWithTag("main_list_pane").getBoundsInRoot()
        assertEquals(DEFAULT_LIST_PANE_WIDTH_DP, listPaneBounds.width.value, paneWidthToleranceDp)
    }

    @Test
    fun forcedSingleColumnKeepsRailWithoutSplitting() {
        composeRule.activity
            .getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(
                    LARGE_SCREEN_LAYOUT_PREFERENCE_KEY,
                    LargeScreenLayoutPreference.SingleColumn.preferenceValue,
                )
            }
        composeRule.setZhihuMainContent(windowLayout = WindowLayoutOverride(widthDp = 900f))

        composeRule.onNodeWithTag("main_navigation_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("main_list_pane").assertDoesNotExist()
        composeRule.onNodeWithTag("main_detail_pane").assertDoesNotExist()
    }

    @Test
    fun verticalHingeNarrowsListPaneAndLeavesHingeGap() {
        val density = composeRule.activity.resources.displayMetrics.density
        val hingeLeftPx = ((NAVIGATION_RAIL_WIDTH_DP + MIN_LIST_PANE_WIDTH_DP + HINGE_SIDE_PADDING_DP) * density).roundToInt()
        val hinge = WindowHinge(
            isSeparating = true,
            orientation = WindowHingeOrientation.Vertical,
            isHalfOpened = false,
            boundsInWindowPx = IntRect(left = hingeLeftPx, top = 0, right = hingeLeftPx, bottom = 100),
        )
        composeRule.setZhihuMainContent(
            windowLayout = WindowLayoutOverride(widthDp = 900f, hinge = hinge),
        )

        val listBounds = composeRule.onNodeWithTag("main_list_pane").getBoundsInRoot()
        assertEquals(MIN_LIST_PANE_WIDTH_DP, listBounds.width.value, paneWidthToleranceDp)
        val detailBounds = composeRule.onNodeWithTag("main_detail_placeholder").getBoundsInRoot()
        assertTrue(
            "详情栏必须让开铰链留白：detail.left=${detailBounds.left} list.right=${listBounds.right}",
            detailBounds.left.value >= listBounds.right.value + HINGE_SIDE_PADDING_DP * 2 - paneWidthToleranceDp,
        )
    }
}
