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

package com.github.zly2006.zhihu.ui.adaptive

import androidx.compose.ui.unit.IntRect
import com.github.zly2006.zhihu.platform.WindowHinge
import com.github.zly2006.zhihu.platform.WindowHingeOrientation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WindowLayoutTest {
    private fun hinge(
        orientation: WindowHingeOrientation = WindowHingeOrientation.Vertical,
        isSeparating: Boolean = true,
        left: Int = 0,
        right: Int = 0,
    ) = WindowHinge(
        isSeparating = isSeparating,
        orientation = orientation,
        isHalfOpened = false,
        boundsInWindowPx = IntRect(left = left, top = 0, right = right, bottom = 100),
    )

    /**
     * 小米 18 Fold 的三种形态宽度：外屏竖持约 445dp、内屏竖持约 637dp、内屏横持约 900dp。
     * 这些数字只用于确认断点覆盖了真实机型，主壳一律运行时判定。
     */
    @Test
    fun foldableWidthsMapToExpectedSizeClasses() {
        assertEquals(ZhihuWindowSizeClass.Compact, zhihuWindowSizeClassForWidth(445f))
        assertEquals(ZhihuWindowSizeClass.Medium, zhihuWindowSizeClassForWidth(637f))
        assertEquals(ZhihuWindowSizeClass.Expanded, zhihuWindowSizeClassForWidth(900f))
    }

    @Test
    fun sizeClassUsesMaterialBreakpoints() {
        assertEquals(ZhihuWindowSizeClass.Compact, zhihuWindowSizeClassForWidth(0f))
        assertEquals(ZhihuWindowSizeClass.Compact, zhihuWindowSizeClassForWidth(599.9f))
        assertEquals(ZhihuWindowSizeClass.Medium, zhihuWindowSizeClassForWidth(600f))
        assertEquals(ZhihuWindowSizeClass.Medium, zhihuWindowSizeClassForWidth(839.9f))
        assertEquals(ZhihuWindowSizeClass.Expanded, zhihuWindowSizeClassForWidth(840f))
        assertEquals(ZhihuWindowSizeClass.Expanded, zhihuWindowSizeClassForWidth(1600f))
    }

    @Test
    fun compactWidthKeepsBottomBarEvenWhenSingleColumnIsForced() {
        assertEquals(
            ShellLayoutMode.BottomBarSinglePane,
            shellLayoutModeFor(445f, LargeScreenLayoutPreference.Auto),
        )
        assertEquals(
            ShellLayoutMode.BottomBarSinglePane,
            shellLayoutModeFor(445f, LargeScreenLayoutPreference.SingleColumn),
        )
    }

    @Test
    fun innerScreenPortraitUsesRailWithoutSplitting() {
        assertEquals(
            ShellLayoutMode.RailSinglePane,
            shellLayoutModeFor(637f, LargeScreenLayoutPreference.Auto),
        )
    }

    @Test
    fun expandedWidthSplitsPanesOnlyInAutoMode() {
        assertEquals(
            ShellLayoutMode.RailTwoPane,
            shellLayoutModeFor(900f, LargeScreenLayoutPreference.Auto),
        )
        assertEquals(
            ShellLayoutMode.RailSinglePane,
            shellLayoutModeFor(900f, LargeScreenLayoutPreference.SingleColumn),
        )
    }

    @Test
    fun metricsWithoutHingeUseDefaultListWidth() {
        assertEquals(
            TwoPaneMetrics(DEFAULT_LIST_PANE_WIDTH_DP, 0f),
            twoPaneMetricsFor(availableWidthDp = 820f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = null, hingeRightInWindowDp = null),
        )
    }

    @Test
    fun metricsSplitAtVerticalHinge() {
        assertEquals(
            TwoPaneMetrics(listPaneWidthDp = 358f, hingeGapDp = HINGE_SIDE_PADDING_DP * 2),
            twoPaneMetricsFor(availableWidthDp = 820f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = 450f, hingeRightInWindowDp = 450f),
        )
    }

    @Test
    fun metricsIncludeHingeWidthInGap() {
        assertEquals(
            TwoPaneMetrics(listPaneWidthDp = 360f, hingeGapDp = 4f + HINGE_SIDE_PADDING_DP * 2),
            twoPaneMetricsFor(availableWidthDp = 820f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = 452f, hingeRightInWindowDp = 456f),
        )
    }

    @Test
    fun metricsFallBackWhenHingeLeavesNoReadablePane() {
        // 铰链贴在列表栏左侧：列表栏只剩几 dp，退回固定分配。
        assertEquals(
            TwoPaneMetrics(DEFAULT_LIST_PANE_WIDTH_DP, 0f),
            twoPaneMetricsFor(availableWidthDp = 820f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = 100f, hingeRightInWindowDp = 100f),
        )
        // 可用宽度本身不足：任何分配都会让详情栏窄于下限。
        assertEquals(
            TwoPaneMetrics(DEFAULT_LIST_PANE_WIDTH_DP, 0f),
            twoPaneMetricsFor(availableWidthDp = 500f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = 600f, hingeRightInWindowDp = 600f),
        )
    }

    @Test
    fun metricsClampListWidthWhenHingeIsFarRight() {
        assertEquals(
            TwoPaneMetrics(listPaneWidthDp = 476f, hingeGapDp = HINGE_SIDE_PADDING_DP * 2),
            twoPaneMetricsFor(availableWidthDp = 820f, panesLeftInWindowDp = 80f, hingeLeftInWindowDp = 1400f, hingeRightInWindowDp = 1400f),
        )
    }

    @Test
    fun windowLayoutDerivesNavigationAndPaneFlags() {
        val compact = WindowLayout(445f, ZhihuWindowSizeClass.Compact, ShellLayoutMode.BottomBarSinglePane, hinge = null)
        assertEquals(false, compact.useNavigationRail)
        assertEquals(false, compact.twoPane)

        val split = WindowLayout(900f, ZhihuWindowSizeClass.Expanded, ShellLayoutMode.RailTwoPane, hinge = hinge())
        assertEquals(true, split.useNavigationRail)
        assertEquals(true, split.twoPane)
        assertNotNull(split.verticalSeparatingHinge)
    }

    @Test
    fun onlySeparatingVerticalHingeSplitsPanes() {
        val horizontal = WindowLayout(900f, ZhihuWindowSizeClass.Expanded, ShellLayoutMode.RailTwoPane, hinge(orientation = WindowHingeOrientation.Horizontal))
        assertNull(horizontal.verticalSeparatingHinge)

        val flat = WindowLayout(900f, ZhihuWindowSizeClass.Expanded, ShellLayoutMode.RailTwoPane, hinge(isSeparating = false))
        assertNull(flat.verticalSeparatingHinge)
    }
}
