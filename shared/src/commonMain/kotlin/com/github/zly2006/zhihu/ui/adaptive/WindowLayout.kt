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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.platform.WindowHinge
import com.github.zly2006.zhihu.platform.WindowHingeOrientation
import com.github.zly2006.zhihu.platform.isWindowPostureSupported
import com.github.zly2006.zhihu.platform.rememberWindowHinge

/** 主壳使用的窗口宽度档位，断点与 Material 3 窗口尺寸类一致。 */
enum class ZhihuWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

/** 主壳形态。Compact 保持底部导航单栏，宽屏改用侧边导航，展开态再拆成双栏。 */
enum class ShellLayoutMode {
    BottomBarSinglePane,
    RailSinglePane,
    RailTwoPane,
}

/** 用户对大屏内容布局的选择；[SingleColumn] 是宽屏下强制单栏的退出口。 */
enum class LargeScreenLayoutPreference(
    val preferenceValue: String,
    val label: String,
) {
    Auto(
        preferenceValue = "auto",
        label = "自动",
    ),
    SingleColumn(
        preferenceValue = "single",
        label = "始终单栏",
    ),
    ;

    companion object {
        fun fromPreference(value: String?): LargeScreenLayoutPreference = entries.firstOrNull {
            it.preferenceValue == value
        } ?: Auto
    }
}

const val LARGE_SCREEN_LAYOUT_PREFERENCE_KEY = "largeScreenLayoutMode"

const val COMPACT_WIDTH_BREAKPOINT_DP = 600f

/** 展开态双栏的宽度门槛：低于该宽度时两栏都会窄到不可读，此时只放侧边导航。 */
const val EXPANDED_WIDTH_BREAKPOINT_DP = 840f

const val NAVIGATION_RAIL_WIDTH_DP = 80f

/** 双栏下列表栏的默认宽度；有竖向铰链时会被铰链位置覆盖。 */
const val DEFAULT_LIST_PANE_WIDTH_DP = 360f

/** 铰链两侧各留的空白，避免内容压在铰链上。 */
const val HINGE_SIDE_PADDING_DP = 12f

const val MIN_LIST_PANE_WIDTH_DP = 280f

const val MIN_DETAIL_PANE_WIDTH_DP = 320f

fun zhihuWindowSizeClassForWidth(widthDp: Float): ZhihuWindowSizeClass = when {
    widthDp < COMPACT_WIDTH_BREAKPOINT_DP -> ZhihuWindowSizeClass.Compact
    widthDp < EXPANDED_WIDTH_BREAKPOINT_DP -> ZhihuWindowSizeClass.Medium
    else -> ZhihuWindowSizeClass.Expanded
}

fun shellLayoutModeFor(
    widthDp: Float,
    preference: LargeScreenLayoutPreference,
): ShellLayoutMode = when {
    widthDp < COMPACT_WIDTH_BREAKPOINT_DP -> ShellLayoutMode.BottomBarSinglePane
    preference == LargeScreenLayoutPreference.SingleColumn -> ShellLayoutMode.RailSinglePane
    widthDp < EXPANDED_WIDTH_BREAKPOINT_DP -> ShellLayoutMode.RailSinglePane
    else -> ShellLayoutMode.RailTwoPane
}

/** 双栏的横向分配结果。 */
data class TwoPaneMetrics(
    val listPaneWidthDp: Float,
    val hingeGapDp: Float,
)

/**
 * 计算双栏的列表栏宽度和铰链留白。
 *
 * [availableWidthDp] 是导航栏之后两栏共用的宽度；[panesLeftInWindowDp] 是两栏区域左边界在窗口坐标系中的位置，
 * 铰链矩形同样使用窗口坐标系，两者相减得到铰链在两栏区域内的位置。
 * 没有竖向分离铰链时使用固定列表栏宽度；有铰链时列表栏收窄到铰链左边界，铰链区间整体留空。
 * 铰链位置会让任一栏窄于可读下限时退回固定宽度分配，宁可让内容跨过铰链也不把栏压到不可用。
 */
fun twoPaneMetricsFor(
    availableWidthDp: Float,
    panesLeftInWindowDp: Float,
    hingeLeftInWindowDp: Float?,
    hingeRightInWindowDp: Float?,
): TwoPaneMetrics {
    if (hingeLeftInWindowDp == null || hingeRightInWindowDp == null) {
        return TwoPaneMetrics(DEFAULT_LIST_PANE_WIDTH_DP, 0f)
    }
    val hingeLeftInPanes = hingeLeftInWindowDp - panesLeftInWindowDp
    val hingeRightInPanes = hingeRightInWindowDp - panesLeftInWindowDp
    val hingeWidth = (hingeRightInPanes - hingeLeftInPanes).coerceAtLeast(0f)
    val hingeGap = hingeWidth + HINGE_SIDE_PADDING_DP * 2
    val listPaneWidth = hingeLeftInPanes - HINGE_SIDE_PADDING_DP
    val maxListPaneWidth = availableWidthDp - MIN_DETAIL_PANE_WIDTH_DP - hingeGap
    return if (listPaneWidth < MIN_LIST_PANE_WIDTH_DP || maxListPaneWidth < MIN_LIST_PANE_WIDTH_DP) {
        TwoPaneMetrics(DEFAULT_LIST_PANE_WIDTH_DP, 0f)
    } else {
        TwoPaneMetrics(listPaneWidth.coerceAtMost(maxListPaneWidth), hingeGap)
    }
}

/**
 * 当前窗口的布局决策。
 *
 * [widthDp] 只用于决策，业务页面不应该按它做像素级布局，避免把设备尺寸写进页面。
 */
data class WindowLayout(
    val widthDp: Float,
    val sizeClass: ZhihuWindowSizeClass,
    val mode: ShellLayoutMode,
    val hinge: WindowHinge?,
) {
    val useNavigationRail: Boolean
        get() = mode != ShellLayoutMode.BottomBarSinglePane

    val twoPane: Boolean
        get() = mode == ShellLayoutMode.RailTwoPane

    /** 竖向分离铰链把两栏自然分到两侧，横向铰链（半开桌面姿态）留给播放器等页面单独处理。 */
    val verticalSeparatingHinge: WindowHinge?
        get() = hinge?.takeIf { it.isSeparating && it.orientation == WindowHingeOrientation.Vertical }
}

/**
 * 测试注入点：非 null 时替换真实窗口宽度与铰链。
 *
 * 仪器测试运行在固定尺寸的模拟器上，只能靠它构造折叠屏各档尺寸与铰链姿态；
 * 生产代码不要提供该值，让它保持 null 并使用真实窗口信息。
 */
data class WindowLayoutOverride(
    val widthDp: Float,
    val hinge: WindowHinge? = null,
)

val LocalWindowLayoutOverride = staticCompositionLocalOf<WindowLayoutOverride?> { null }

/**
 * 读取当前窗口的布局决策。
 *
 * 宽度优先使用 [LocalWindowLayoutOverride]，否则取 [LocalWindowInfo] 的容器宽度；
 * 折叠姿态只在平台支持时读取，不支持姿态检测的平台按 [isWindowPostureSupported] 跳过。
 */
@Composable
fun rememberWindowLayout(preference: LargeScreenLayoutPreference): WindowLayout {
    val override = LocalWindowLayoutOverride.current
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    val hinge = override?.hinge ?: if (isWindowPostureSupported) rememberWindowHinge() else null
    val widthDp = override?.widthDp ?: with(density) { containerSize.width.toDp().value }
    return WindowLayout(
        widthDp = widthDp,
        sizeClass = zhihuWindowSizeClassForWidth(widthDp),
        mode = shellLayoutModeFor(widthDp, preference),
        hinge = hinge,
    )
}

/** 正文列的宽度上限，避免展开态下正文行长超出舒适阅读范围。 */
val ReadingColumnMaxWidth = 840.dp
