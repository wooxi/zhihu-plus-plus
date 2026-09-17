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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntRect
import kotlinx.io.files.Path

internal expect val platformBottomBarItemLimit: Int?

/** 折叠屏转轴/铰链方向。 */
enum class WindowHingeOrientation {
    Vertical,
    Horizontal,
}

/**
 * 平台报告的折叠姿态。
 *
 * [boundsInWindowPx] 是铰链在窗口坐标系中的矩形；[isHalfOpened] 表示设备处于半开（悬停/桌面）姿态。
 * 只有能读到转轴信息的平台才会产生该对象，普通直板设备与桌面窗口读到的都是 null。
 */
data class WindowHinge(
    val isSeparating: Boolean,
    val orientation: WindowHingeOrientation,
    val isHalfOpened: Boolean,
    val boundsInWindowPx: IntRect,
)

/** 平台是否提供折叠姿态信息；为 false 的平台不得调用 [rememberWindowHinge]。 */
internal expect val isWindowPostureSupported: Boolean

/** 观察当前窗口的铰链姿态，无铰链时返回 null。 */
@Composable
expect fun rememberWindowHinge(): WindowHinge?

expect val platformName: String

expect val isJvm: Boolean

expect val isNative: Boolean

expect val isAigcVoteSupported: Boolean

expect val isBlocklistNlpSupported: Boolean

expect val isSentenceSimilaritySupported: Boolean

expect val isArticleHtmlExportSupported: Boolean

expect val isArticleImageExportSupported: Boolean

expect val isPageTurnSupported: Boolean

expect val isAnswerSwipeSupported: Boolean

enum class UserMessageDuration {
    Short,
    Long,
}

interface UserMessageSink {
    fun showShortMessage(message: String)

    fun showLongMessage(message: String) = showShortMessage(message)

    fun showMessage(
        message: String,
        duration: UserMessageDuration = UserMessageDuration.Short,
    ) {
        when (duration) {
            UserMessageDuration.Short -> showShortMessage(message)
            UserMessageDuration.Long -> showLongMessage(message)
        }
    }
}

@Composable
expect fun rememberUserMessageSink(): UserMessageSink

interface SettingsStore {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun putBoolean(key: String, value: Boolean)

    fun getString(key: String, defaultValue: String): String

    fun putString(key: String, value: String)

    fun getStringOrNull(key: String): String?

    fun putStringSet(key: String, value: Set<String>)

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>

    fun getInt(key: String, defaultValue: Int): Int

    fun putInt(key: String, value: Int)

    fun getLong(key: String, defaultValue: Long): Long

    fun putLong(key: String, value: Long)

    fun getFloat(key: String, defaultValue: Float): Float

    fun putFloat(key: String, value: Float)

    fun remove(key: String)

    fun removeByPrefix(prefix: String) = Unit

    fun observeKeyChanges(onChanged: (String) -> Unit): AutoCloseable = AutoCloseable { }
}

interface SystemUrlOpener {
    operator fun invoke(url: String)
}

interface ZhihuWebUrlOpener {
    operator fun invoke(url: String)
}

interface ImagePreviewOpener {
    operator fun invoke(url: String)
}

interface ExternalUrlOpener :
    SystemUrlOpener,
    ZhihuWebUrlOpener,
    ImagePreviewOpener

/** Opens a third-party page inside the app's embedded WebView. */
interface WebViewUrlOpener {
    operator fun invoke(url: String)
}

interface ImageGalleryOpener {
    operator fun invoke(urls: List<String>, initialIndex: Int)
}

interface ImageSaver {
    operator fun invoke(url: String)
}

interface ImageSharer {
    operator fun invoke(url: String)
}

interface PlainTextClipboard {
    operator fun invoke(label: String, text: String)
}

@Composable
expect fun rememberSettingsStore(): SettingsStore

expect fun Modifier.exportTestTagsForUiAutomation(): Modifier

@Composable
expect fun rememberAppPrivateDirectory(): Path

@Composable
expect fun rememberExternalUrlOpener(): ExternalUrlOpener

@Composable
expect fun rememberWebViewUrlOpener(): WebViewUrlOpener

@Composable
expect fun rememberSystemUrlOpener(): SystemUrlOpener

@Composable
expect fun rememberZhihuWebUrlOpener(): ZhihuWebUrlOpener

@Composable
expect fun rememberImagePreviewOpener(): ImagePreviewOpener

@Composable
expect fun rememberImageGalleryOpener(): ImageGalleryOpener

@Composable
expect fun rememberImageSaver(): ImageSaver

@Composable
expect fun rememberImageSharer(): ImageSharer

@Composable
expect fun rememberPlainTextClipboard(): PlainTextClipboard

@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)

@Composable
expect fun PlatformPredictiveBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
)

@Composable
expect fun rememberIsLiteVariant(): Boolean
