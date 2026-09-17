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

package com.github.zly2006.zhihu.platform

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntRect
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal actual val isWindowPostureSupported: Boolean = true

/** 把平台窗口布局信息映射成主壳使用的折叠姿态；窗口里没有转轴时返回 null。 */
fun WindowLayoutInfo.toWindowHinge(): WindowHinge? {
    val feature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull() ?: return null
    return WindowHinge(
        isSeparating = feature.isSeparating,
        orientation = when (feature.orientation) {
            FoldingFeature.Orientation.VERTICAL -> WindowHingeOrientation.Vertical
            else -> WindowHingeOrientation.Horizontal
        },
        isHalfOpened = feature.state == FoldingFeature.State.HALF_OPENED,
        boundsInWindowPx = IntRect(
            left = feature.bounds.left,
            top = feature.bounds.top,
            right = feature.bounds.right,
            bottom = feature.bounds.bottom,
        ),
    )
}

/**
 * 观察指定 Activity 窗口的折叠姿态。
 *
 * 非 Compose 入口（视频播放页需要按铰链把画面放到上半屏）直接调用它，避免把 androidx.window 的类型暴露给 app 模块。
 */
fun windowHingeFlow(activity: Activity): Flow<WindowHinge?> = WindowInfoTracker
    .getOrCreate(activity)
    .windowLayoutInfo(activity)
    .map { it.toWindowHinge() }

@Composable
actual fun rememberWindowHinge(): WindowHinge? {
    val activity = LocalActivity.current ?: return null
    val hingeFlow = remember(activity) { windowHingeFlow(activity) }
    val hinge by hingeFlow.collectAsState(initial = null)
    return hinge
}
