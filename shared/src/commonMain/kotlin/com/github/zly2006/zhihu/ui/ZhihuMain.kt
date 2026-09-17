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

package com.github.zly2006.zhihu.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateLeftPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.github.zly2006.zhihu.account.LoginScreen
import com.github.zly2006.zhihu.filter.ContentOpenFrom
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.ArticleTypeNavType
import com.github.zly2006.zhihu.navigation.CollectionContent
import com.github.zly2006.zhihu.navigation.Collections
import com.github.zly2006.zhihu.navigation.Daily
import com.github.zly2006.zhihu.navigation.Follow
import com.github.zly2006.zhihu.navigation.History
import com.github.zly2006.zhihu.navigation.Home
import com.github.zly2006.zhihu.navigation.HotList
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Login
import com.github.zly2006.zhihu.navigation.MainTabs
import com.github.zly2006.zhihu.navigation.MyCollections
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.Navigator
import com.github.zly2006.zhihu.navigation.Notification
import com.github.zly2006.zhihu.navigation.OnlineHistory
import com.github.zly2006.zhihu.navigation.Person
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.Search
import com.github.zly2006.zhihu.navigation.SentenceSimilarityTest
import com.github.zly2006.zhihu.navigation.TopLevelDestination
import com.github.zly2006.zhihu.navigation.Topic
import com.github.zly2006.zhihu.navigation.WriteAnswer
import com.github.zly2006.zhihu.navigation.WritePin
import com.github.zly2006.zhihu.navigation.loginNavigationRequestFlow
import com.github.zly2006.zhihu.platform.PlatformBackHandler
import com.github.zly2006.zhihu.platform.platformName
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.reading.rememberReadingPlayerController
import com.github.zly2006.zhihu.reading.saveReadingPlaybackSpeed
import com.github.zly2006.zhihu.ui.adaptive.NAVIGATION_RAIL_WIDTH_DP
import com.github.zly2006.zhihu.ui.adaptive.ReadingColumnMaxWidth
import com.github.zly2006.zhihu.ui.adaptive.TwoPaneMetrics
import com.github.zly2006.zhihu.ui.adaptive.WindowLayout
import com.github.zly2006.zhihu.ui.adaptive.ZhihuWindowSizeClass
import com.github.zly2006.zhihu.ui.adaptive.rememberWindowLayout
import com.github.zly2006.zhihu.ui.adaptive.twoPaneMetricsFor
import com.github.zly2006.zhihu.ui.components.CompactReadingPlayerButton
import com.github.zly2006.zhihu.ui.components.NoOpPagerNestedScrollConnection
import com.github.zly2006.zhihu.ui.components.ReadingPlayerBar
import com.github.zly2006.zhihu.ui.components.ReadingQueueSheet
import com.github.zly2006.zhihu.ui.subscreens.AppearanceSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.BlockedFeedHistoryScreen
import com.github.zly2006.zhihu.ui.subscreens.ColorSchemeScreen
import com.github.zly2006.zhihu.ui.subscreens.ContentFilterSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.DeveloperSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.IdentityManagementScreen
import com.github.zly2006.zhihu.ui.subscreens.OpenSourceLicensesScreen
import com.github.zly2006.zhihu.ui.subscreens.ReadingSettingsScreen
import com.github.zly2006.zhihu.ui.subscreens.SettingsSearchScreen
import com.github.zly2006.zhihu.ui.subscreens.SystemAndUpdateSettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

private sealed class MainTabPage(
    val bottomDestination: TopLevelDestination,
    val key: String,
) {
    data object HomePage : MainTabPage(Home, "home")

    data object FollowPage : MainTabPage(Follow, "follow")

    data object HotListPage : MainTabPage(HotList, "hotlist")

    data object DailyPage : MainTabPage(Daily, "daily")

    data object OnlineHistoryPage : MainTabPage(OnlineHistory, "online_history")

    data object MyCollectionsPage : MainTabPage(MyCollections, "my_collections")

    data object AccountPage : MainTabPage(Account, "account")
}

internal val LocalReadingPlayerOverlayPadding = staticCompositionLocalOf { 0.dp }
internal val LocalArticleNavController = staticCompositionLocalOf<NavHostController?> { null }

/**
 * Zhihu++ 的共享应用主壳。
 *
 * 这个 composable 是顶层体验的唯一所有者：渲染可配置底部导航栏，承载横向主 tab pager，向子页面提供 [LocalNavigator]，
 * 并注册跨平台共享的 typed [NavDestination] route。设计上把顶层 tab 收在 [MainTabs] 内部，而不是把每个 tab
 * 都作为独立 NavHost 页面 push，这样 tab 重选、回到顶部、顶/底栏自动隐藏和持久化 tab 选择都能使用同一套状态模型。
 *
 * 用户可见的主壳设置通过 [preferenceState] 流入。设置页退出时只 reload 这份状态，不重建 NavHost，从而在应用底栏和主题相关变更时
 * 保留已加载页面、返回栈和滚动位置。
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("RestrictedApi")
@Composable
fun ZhihuMain(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainTabNavigationTarget: TopLevelDestination?,
    navigate: (NavDestination) -> Unit,
    setCurrentMainTabOpenFrom: (String?) -> Unit,
    consumeMainTabNavigationTarget: (TopLevelDestination) -> Unit,
    preferenceState: ZhihuMainPreferenceState,
    isDarkTheme: Boolean,
    articleContent: @Composable (Article, NavBackStackEntry) -> Unit,
    showMainNavigationBar: Boolean = true,
    showHomeTopActions: Boolean = true,
    onCurrentMainTabDestinationChange: (TopLevelDestination) -> Unit = {},
    sentenceSimilarityContent: @Composable () -> Unit = {
        error("$platformName 暂不支持句子相似度测试")
    },
    blocklistSettingsNlpContent: @Composable (onNavigateBack: () -> Unit) -> Unit = {
        error("$platformName 暂不支持 NLP 智能屏蔽设置")
    },
    articleEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    articleExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
) {
    val bottomPadding = ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateBottomPadding()
    val duo3HomeAccount = preferenceState.duo3HomeAccount
    val tapToScrollToTopEnabled = preferenceState.tapToScrollToTopEnabled
    val autoHideBottomBar = preferenceState.autoHideBottomBar
    val collectionDirectBrowseEnabled = preferenceState.collectionDirectBrowseEnabled
    val selectedBottomBarItemKeys = preferenceState.selectedBottomBarItemKeys
    val startDestination = preferenceState.startDestination
    val reloadBottomBarPreferences = preferenceState::reload
    val readingPlayer = rememberReadingPlayerController()
    val readingPlayerState by readingPlayer.state
    val settings = rememberSettingsStore()
    var showReadingQueue by rememberSaveable { mutableStateOf(false) }
    var isReadingPlayerExpandedByUser by rememberSaveable { mutableStateOf(false) }
    var readingPlayerHeightPx by remember { mutableIntStateOf(0) }
    val readingPlayerOverlayOffsetState = remember { ReadingPlayerOverlayOffsetState() }
    val density = LocalDensity.current
    val currentOnMainTabDestinationChange by rememberUpdatedState(onCurrentMainTabDestinationChange)

    val navEntry by navController.currentBackStackEntryAsState()
    val showMainNavigation = navEntry?.destination?.hasRoute<MainTabs>() == true
    PlatformBackHandler(enabled = navEntry != null && !showMainNavigation) {
        navController.popBackStack()
    }
    val isOnReadingDetail = navEntry?.destination?.hasRoute<Article>() == true ||
        navEntry?.destination?.hasRoute<Question>() == true ||
        navEntry?.destination?.hasRoute<Pin>() == true
    val isReadingPlayerExpanded = readingPlayerState.hasSession &&
        (isOnReadingDetail || isReadingPlayerExpandedByUser)
    val shouldCompactPlayerOnBackgroundInteraction by rememberUpdatedState(
        isReadingPlayerExpandedByUser && !isOnReadingDetail,
    )
    val readingPlayerOverlayPadding = when {
        !readingPlayerState.hasSession -> 0.dp
        !isReadingPlayerExpanded -> 0.dp
        readingPlayerHeightPx > 0 -> with(density) { readingPlayerHeightPx.toDp() } + 16.dp
        else -> 16.dp
    }

    LaunchedEffect(readingPlayerState.hasSession) {
        if (!readingPlayerState.hasSession) {
            showReadingQueue = false
            isReadingPlayerExpandedByUser = false
            readingPlayerOverlayOffsetState.resetOffset()
        }
    }
    var previousReadingItemKey by remember { mutableStateOf(readingPlayerState.currentItem?.key) }
    LaunchedEffect(readingPlayerState.currentItem?.key) {
        val currentItem = readingPlayerState.currentItem
        val currentItemKey = currentItem?.key
        val itemChanged = previousReadingItemKey != null && previousReadingItemKey != currentItemKey
        previousReadingItemKey = currentItemKey
        if (itemChanged && currentItem != null) {
            val currentDestination = when {
                navEntry?.destination?.hasRoute<Article>() == true -> runCatching {
                    navEntry?.toRoute<Article>()
                }.getOrNull()
                navEntry?.destination?.hasRoute<Pin>() == true -> runCatching {
                    navEntry?.toRoute<Pin>()
                }.getOrNull()
                navEntry?.destination?.hasRoute<Question>() == true -> runCatching {
                    navEntry?.toRoute<Question>()
                }.getOrNull()
                else -> null
            }
            val destination = currentItem.toDestination(readingPlayerState.sourceId)
            if (currentDestination != null && currentDestination != destination) {
                navController.popBackStack()
                navigate(destination)
            }
        }
    }

    // 离开文章页时恢复系统状态栏（只在实际切换时触发）
    val isOnArticle = navEntry?.destination?.hasRoute<Article>() == true
    LaunchedEffect(navEntry) {
        isReadingPlayerExpandedByUser = false
        if (!isOnArticle) readingPlayerOverlayOffsetState.clearRoute()
    }
    var wasOnArticle by remember { mutableStateOf(false) }
    if (!isOnArticle && wasOnArticle) {
        LeaveImmersiveModeCleanup()
    }
    SideEffect {
        wasOnArticle = isOnArticle
    }

    var scrollToTopTrigger by remember { mutableIntStateOf(0) }
    // 滚动时自动隐藏底部导航栏
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val bottomBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                when {
                    available.y < -3f -> isBottomBarVisible = false
                    available.y > 3f -> isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val allBottomBarItems = listOf(
        Triple(Home, "主页", Icons.Filled.Home),
        Triple(Follow, "关注", Icons.Filled.Group),
        Triple(HotList, "热榜", Icons.Filled.Whatshot),
        Triple(Daily, "日报", Icons.Filled.Newspaper),
        Triple(OnlineHistory, "历史", Icons.Filled.History),
        Triple(MyCollections, "收藏夹", Icons.Filled.Bookmarks),
        Triple(Account, "账号", Icons.Filled.ManageAccounts),
    )
    val bottomBarItems = selectedBottomBarItemKeys.mapNotNull { key ->
        allBottomBarItems.firstOrNull { it.first.name == key }
    }

    val mainTabPages = remember(bottomBarItems) {
        bottomBarItems.flatMap { item ->
            when (item.first) {
                Home -> listOf(MainTabPage.HomePage)
                Follow -> listOf(MainTabPage.FollowPage)
                HotList -> listOf(MainTabPage.HotListPage)
                Daily -> listOf(MainTabPage.DailyPage)
                OnlineHistory -> listOf(MainTabPage.OnlineHistoryPage)
                MyCollections -> listOf(MainTabPage.MyCollectionsPage)
                Account -> listOf(MainTabPage.AccountPage)
                else -> emptyList()
            }
        }
    }

    fun pageIndexForDestination(destination: TopLevelDestination): Int = mainTabPages
        .indexOfFirst {
            it.bottomDestination::class == destination::class
        }.takeIf { it >= 0 } ?: mainTabPages
        .indexOfFirst {
            it.bottomDestination::class == startDestination::class
        }.takeIf { it >= 0 } ?: 0

    val mainPagerState = rememberPagerState(
        initialPage = pageIndexForDestination(startDestination),
        pageCount = { mainTabPages.size },
    )
    val coroutineScope = rememberCoroutineScope()

    var currentMainTabDestination by remember { mutableStateOf(startDestination) }

    fun navigateTopLevel(destination: TopLevelDestination) {
        val targetPage = pageIndexForDestination(destination)
        coroutineScope.launch {
            mainPagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(navController) {
        loginNavigationRequestFlow.collect {
            navController.navigate(Login) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(mainPagerState.currentPage, mainTabPages) {
        mainTabPages.getOrNull(mainPagerState.currentPage)?.bottomDestination?.let { destination ->
            currentMainTabDestination = destination
            setCurrentMainTabOpenFrom(destination.openFrom)
            currentOnMainTabDestinationChange(destination)
        }
    }

    PlatformBackHandler(showMainNavigation && mainPagerState.currentPage != 0) {
        coroutineScope.launch {
            mainPagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(mainTabNavigationTarget, mainTabPages) {
        mainTabNavigationTarget?.let { destination ->
            // 平台适配层会把旧的顶层 route 请求映射到 MainTabs。这里消费该请求，
            // 让 deeplink 等调用方仍能选中 Home/Follow 等 tab，而不是把旧 route 压入返回栈。
            mainPagerState.scrollToPage(pageIndexForDestination(destination))
            consumeMainTabNavigationTarget(destination)
        }
    }

    LaunchedEffect(mainTabPages) {
        if (mainTabPages.isNotEmpty()) {
            val currentDestinationStillVisible = mainTabPages.any {
                it.bottomDestination::class == currentMainTabDestination::class
            }
            val targetDestination = if (currentDestinationStillVisible) {
                currentMainTabDestination
            } else {
                startDestination
            }
            val targetPage = pageIndexForDestination(targetDestination)
            if (mainPagerState.currentPage != targetPage || mainPagerState.currentPage !in mainTabPages.indices) {
                mainPagerState.scrollToPage(targetPage)
            }
        }
    }

    val windowLayout = rememberWindowLayout(preferenceState.largeScreenLayout)
    val useNavigationRail = showMainNavigationBar && windowLayout.useNavigationRail
    val twoPane = showMainNavigationBar && windowLayout.twoPane
    val layoutDirection = LocalLayoutDirection.current
    val shellInsets = ScaffoldDefaults.contentWindowInsets.asPaddingValues()
    val panesStartInWindow = if (twoPane) {
        shellInsets.calculateLeftPadding(layoutDirection) + NAVIGATION_RAIL_WIDTH_DP.dp
    } else {
        0.dp
    }
    val twoPaneMetrics = if (twoPane) {
        resolveTwoPaneMetrics(windowLayout, panesStartInWindow, density)
    } else {
        TwoPaneMetrics(0f, 0f)
    }
    // 双栏时详情栏从列表栏右侧开始，阅读播报条跟着详情栏居中，避免压在分栏缝上。
    val detailPaneStart = panesStartInWindow + twoPaneMetrics.listPaneWidthDp.dp + twoPaneMetrics.hingeGapDp.dp
    // 宽屏单栏时给正文列留宽度上限并居中，避免内屏横持和桌面窗口出现过长行长。
    val readingColumnBudget = windowLayout.widthDp - ReadingColumnMaxWidth.value
    val readingColumnInset = if (!twoPane && windowLayout.sizeClass == ZhihuWindowSizeClass.Expanded) {
        (readingColumnBudget / 2).coerceAtLeast(0f).dp
    } else {
        0.dp
    }
    val detailPaneInsets = PaddingValues(start = detailPaneStart + readingColumnInset, end = readingColumnInset)
    // 详情栏整体带让位留白，tag 挂在 NavHost 上，便于仪器测试断言双栏形态已经生效。
    val detailPaneTestTag = if (twoPane) Modifier.testTag("main_detail_pane") else Modifier
    val currentBottomDestination = mainTabPages
        .getOrNull(mainPagerState.targetPage)
        ?.bottomDestination
    val shellNavigator = Navigator(
        onNavigate = { destination ->
            navigate(destination)
        },
        onNavigateBack = navController::popBackStack,
        onNavigateTopLevel = ::navigateTopLevel,
    )
    val tabsStateHolder = rememberSaveableStateHolder()

    fun onSelectTopLevelDestination(destination: TopLevelDestination) {
        isReadingPlayerExpandedByUser = false
        if (currentBottomDestination?.let { it::class == destination::class } != true) {
            navigateTopLevel(destination)
        } else if (tapToScrollToTopEnabled) {
            scrollToTopTrigger++
        }
    }

    @Composable
    fun ReadingPlayerFab(horizontalOffset: Dp) {
        AnimatedVisibility(
            visible = isReadingPlayerExpanded,
            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.92f),
            exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.92f),
        ) {
            ReadingPlayerBar(
                state = readingPlayerState,
                onPrevious = readingPlayer::playPrevious,
                onTogglePlayPause = readingPlayer::togglePlayPause,
                onNext = readingPlayer::playNext,
                onStop = readingPlayer::stop,
                onOpenQueue = { showReadingQueue = true },
                onPlaybackSpeedChange = { speed ->
                    saveReadingPlaybackSpeed(settings, speed)
                    readingPlayer.setPlaybackSpeed(speed)
                },
                onBackgroundInteraction = {
                    if (!isOnReadingDetail) isReadingPlayerExpandedByUser = false
                },
                modifier = Modifier
                    .offset(x = horizontalOffset)
                    .onSizeChanged { readingPlayerHeightPx = it.height }
                    .graphicsLayer {
                        translationY = readingPlayerOverlayOffsetState.verticalOffsetPx
                    },
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(bottomBarScrollConnection),
            floatingActionButton = {
                ReadingPlayerFab(horizontalOffset = detailPaneStart / 2)
            },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                if (!useNavigationRail && showMainNavigationBar && navEntry != null) {
                    // 页面切换时重置底部导航栏可见状态
                    LaunchedEffect(navEntry) { isBottomBarVisible = true }
                    AnimatedVisibility(
                        visible = showMainNavigation && (!autoHideBottomBar || isBottomBarVisible),
                        enter = slideInVertically(tween(200)) { it },
                        exit = slideOutVertically(tween(200)) { it },
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.height(
                                64.dp + bottomPadding,
                            ),
                        ) {
                            @Composable
                            fun Item(
                                destination: TopLevelDestination,
                                label: String,
                                icon: ImageVector,
                            ) {
                                val tag = "nav_tab_${destination.name.lowercase()}"
                                NavigationBarItem(
                                    currentBottomDestination?.let { it::class == destination::class } == true,
                                    onClick = { onSelectTopLevelDestination(destination) },
                                    label = { Text(label) },
                                    alwaysShowLabel = true,
                                    colors = if (!isDarkTheme) {
                                        NavigationBarItemDefaults.colors().copy(
                                            selectedIndicatorColor =
                                                MaterialTheme.colorScheme.secondaryContainer
                                                    .copy(alpha = 0.92f)
                                                    .compositeOver(MaterialTheme.colorScheme.secondary),
                                        )
                                    } else {
                                        NavigationBarItemDefaults.colors()
                                    },
                                    icon = {
                                        Icon(icon, contentDescription = label)
                                    },
                                    modifier = Modifier.padding(top = 4.dp).testTag(tag),
                                )
                            }

                            bottomBarItems.forEach { item ->
                                Item(item.first, item.second, item.third)
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalArticleNavController provides navController,
                LocalNavigator provides shellNavigator,
                LocalReadingPlayerOverlayPadding provides readingPlayerOverlayPadding,
                LocalReadingPlayerOverlayOffsetState provides readingPlayerOverlayOffsetState,
            ) {
                NavHost(
                    navController,
                    modifier = Modifier.padding(detailPaneInsets).then(detailPaneTestTag).pointerInput(Unit) {
                        while (true) {
                            awaitPointerEventScope {
                                awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial,
                                )
                                while (
                                    awaitPointerEvent(PointerEventPass.Final)
                                        .changes
                                        .any { it.pressed }
                                ) {
                                    // 等手势完成后再重组，避免取消同一次背景点击或滚动。
                                }
                            }
                            if (shouldCompactPlayerOnBackgroundInteraction) {
                                delay(100)
                                isReadingPlayerExpandedByUser = false
                            }
                        }
                    },
                    startDestination = MainTabs,
                    enterTransition = {
                        slideInHorizontally(tween(300)) { it }
                    },
                    exitTransition = {
                        ExitTransition.None
                    },
                    popEnterTransition = {
                        EnterTransition.None
                    },
                    popExitTransition = {
                        slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                    },
                ) {
                    composable<MainTabs> {
                        if (twoPane) {
                            // 展开态下 tab 内容已经挪到左侧列表栏，详情栏只在没有选中内容时显示空态。
                            MainDetailPlaceholder(innerPadding)
                        } else {
                            tabsStateHolder.SaveableStateProvider(MAIN_TABS_SAVEABLE_KEY) {
                                MainTabsPager(
                                    pagerState = mainPagerState,
                                    pages = mainTabPages,
                                    scrollToTopTrigger = scrollToTopTrigger,
                                    innerPadding = innerPadding,
                                    collectionDirectBrowseEnabled = collectionDirectBrowseEnabled,
                                    showHomeTopActions = showHomeTopActions,
                                )
                            }
                        }
                    }
                    composable<Login> {
                        LoginScreen(
                            onLoginComplete = { navController.popBackStack() },
                            onOpenTelemetrySettings = {
                                navController.navigate(Account.SystemAndUpdateSettings("allowTelemetry"))
                            },
                        )
                    }
                    composable<Question> { navEntry ->
                        val question: Question = navEntry.toRoute()
                        QuestionScreen(question)
                    }
                    composable<Topic> { navEntry ->
                        TopicScreen(navEntry.toRoute())
                    }
                    composable<WriteAnswer> { navEntry ->
                        val args: WriteAnswer = navEntry.toRoute()
                        WriteAnswerScreen(args)
                    }
                    composable<WritePin> { navEntry ->
                        WritePinScreen(navEntry.toRoute())
                    }
                    composable<Article>(
                        typeMap = mapOf(typeOf<ArticleType>() to ArticleTypeNavType),
                        enterTransition = articleEnterTransition,
                        exitTransition = articleExitTransition,
                    ) { navEntry ->
                        val article: Article = navEntry.toRoute()
                        articleContent(article, navEntry)
                    }
                    composable<HotList> {
                        HotListScreen(innerPadding)
                    }
                    composable<Follow> {
                        FollowScreen(
                            scrollToTopTrigger = scrollToTopTrigger,
                            innerPadding = innerPadding,
                            parentPagerState = mainPagerState,
                        )
                    }
                    composable<Daily> {
                        DailyScreen()
                    }
                    composable<History> {
                        LegacyLocalHistoryScreen(innerPadding)
                    }
                    composable<OnlineHistory> {
                        OnlineHistoryScreen()
                    }
                    composable<Account> {
                        AccountSettingScreen(innerPadding)
                    }
                    composable<Search>(
                        enterTransition = {
                            if (initialState.destination.hasRoute<Search>()) {
                                EnterTransition.None
                            } else {
                                fadeIn(animationSpec = tween(durationMillis = 240)) +
                                    slideInVertically(animationSpec = tween(durationMillis = 280)) { it / 16 } +
                                    scaleIn(
                                        animationSpec = tween(durationMillis = 280),
                                        initialScale = 0.985f,
                                    )
                            }
                        },
                        popExitTransition = {
                            if (targetState.destination.hasRoute<Search>()) {
                                ExitTransition.None
                            } else {
                                fadeOut(animationSpec = tween(durationMillis = 180)) +
                                    slideOutVertically(animationSpec = tween(durationMillis = 220)) { it / 20 } +
                                    scaleOut(
                                        animationSpec = tween(durationMillis = 220),
                                        targetScale = 0.985f,
                                    )
                            }
                        },
                    ) { navEntry ->
                        val search: Search = navEntry.toRoute()
                        SearchScreen(search)
                    }
                    composable<Collections> { navEntry ->
                        val data: Collections = navEntry.toRoute()
                        CollectionScreen(
                            urlToken = data.userToken,
                            contentPadding = innerPadding,
                        )
                    }
                    composable<CollectionContent> { navEntry ->
                        val content: CollectionContent = navEntry.toRoute()
                        CollectionContentScreen(content.collectionId)
                    }
                    composable<Person> { navEntry ->
                        val person: Person = navEntry.toRoute()
                        PeopleScreen(person)
                    }
                    composable<Pin> { navEntry ->
                        val pin: Pin = navEntry.toRoute()
                        PinScreen(pin)
                    }
                    composable<Account.RecommendSettings.Blocklist> {
                        BlocklistSettingsScreen(blocklistSettingsNlpContent)
                    }
                    composable<Account.RecommendSettings.BlockedFeedHistory> {
                        BlockedFeedHistoryScreen()
                    }
                    composable<Notification> {
                        NotificationScreen()
                    }
                    composable<Notification.Entry> { navEntry ->
                        val entry: Notification.Entry = navEntry.toRoute()
                        NotificationTimelineScreen(entry.entryName, entry.title)
                    }
                    composable<Notification.Invitations> {
                        NotificationTimelineScreen("invite", "邀请回答")
                    }
                    composable<Notification.Message> { navEntry ->
                        PrivateMessageScreen(navEntry.toRoute())
                    }
                    composable<Notification.NotificationSettings> { navEntry ->
                        NotificationSettingsScreen(
                            setting = navEntry.toRoute<Notification.NotificationSettings>().setting,
                        )
                    }
                    composable<SentenceSimilarityTest> {
                        sentenceSimilarityContent()
                    }
                    composable<Account.AppearanceSettings> { navEntry ->
                        val args = navEntry.toRoute<Account.AppearanceSettings>()
                        AppearanceSettingsScreen(
                            setting = args.setting,
                            onExit = reloadBottomBarPreferences,
                        )
                    }
                    composable<Account.RecommendSettings> { navEntry ->
                        val args = navEntry.toRoute<Account.RecommendSettings>()
                        ContentFilterSettingsScreen(args.setting)
                    }
                    composable<Account.IdentityManagement> {
                        IdentityManagementScreen()
                    }
                    composable<Account.SystemAndUpdateSettings> { navEntry ->
                        SystemAndUpdateSettingsScreen(
                            setting = navEntry.toRoute<Account.SystemAndUpdateSettings>().setting,
                        )
                    }
                    composable<Account.ReadingSettings> {
                        ReadingSettingsScreen()
                    }
                    composable<Account.SettingsSearch> {
                        SettingsSearchScreen()
                    }
                    composable<Account.OpenSourceLicenses> {
                        OpenSourceLicensesScreen()
                    }
                    composable<Account.DeveloperSettings> {
                        DeveloperSettingsScreen()
                    }
                    composable<Account.DeveloperSettings.ColorScheme> {
                        ColorSchemeScreen()
                    }
                }
            }
        }

        if (useNavigationRail) {
            MainNavigationRail(
                items = bottomBarItems,
                currentDestination = currentBottomDestination,
                onSelect = ::onSelectTopLevelDestination,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .windowInsetsPadding(ScaffoldDefaults.contentWindowInsets)
                    .testTag("main_navigation_rail"),
            )
        }
        if (twoPane) {
            // 列表栏与详情栏同在导航栏右侧；详情栏由 NavHost 自带的 start padding 让位，两栏因此不会重叠。
            CompositionLocalProvider(LocalNavigator provides shellNavigator) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = panesStartInWindow)
                        .fillMaxHeight(),
                ) {
                    Box(
                        modifier = Modifier
                            .width(twoPaneMetrics.listPaneWidthDp.dp)
                            .fillMaxHeight()
                            .testTag("main_list_pane"),
                    ) {
                        tabsStateHolder.SaveableStateProvider(MAIN_TABS_SAVEABLE_KEY) {
                            MainTabsPager(
                                pagerState = mainPagerState,
                                pages = mainTabPages,
                                scrollToTopTrigger = scrollToTopTrigger,
                                innerPadding = shellInsets,
                                collectionDirectBrowseEnabled = collectionDirectBrowseEnabled,
                                showHomeTopActions = showHomeTopActions,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = readingPlayerState.hasSession && !isReadingPlayerExpanded,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CompactReadingPlayerButton(
                    state = readingPlayerState,
                    onExpand = { isReadingPlayerExpandedByUser = true },
                )
            }
        }
    }

    if (showReadingQueue && readingPlayerState.hasSession) {
        ReadingQueueSheet(
            state = readingPlayerState,
            onDismissRequest = {
                showReadingQueue = false
                if (!isOnReadingDetail) isReadingPlayerExpandedByUser = false
            },
            onItemClick = { index, item ->
                previousReadingItemKey = item.key
                if (index != readingPlayerState.currentIndex) {
                    readingPlayer.playAt(index)
                }
                showReadingQueue = false
                val destination = item.toDestination(readingPlayerState.sourceId)
                val currentDestination = when {
                    navEntry?.destination?.hasRoute<Article>() == true -> runCatching {
                        navEntry?.toRoute<Article>()
                    }.getOrNull()
                    navEntry?.destination?.hasRoute<Pin>() == true -> runCatching {
                        navEntry?.toRoute<Pin>()
                    }.getOrNull()
                    navEntry?.destination?.hasRoute<Question>() == true -> runCatching {
                        navEntry?.toRoute<Question>()
                    }.getOrNull()
                    else -> null
                }
                if (currentDestination != destination) {
                    if (currentDestination != null) {
                        navController.popBackStack()
                    }
                    navigate(destination)
                }
            },
            onOpenSettings = {
                showReadingQueue = false
                isReadingPlayerExpandedByUser = false
                if (navEntry?.destination?.hasRoute<Account.ReadingSettings>() != true) {
                    navigate(Account.ReadingSettings)
                }
            },
        )
    }
}

/**
 * 渲染可配置底部导航主壳内的页面。
 *
 * 每个页面都接收主壳给出的 [innerPadding]，保证系统栏、底部栏和子页面之间的留白一致。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsPager(
    pagerState: PagerState,
    pages: List<MainTabPage>,
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    collectionDirectBrowseEnabled: Boolean,
    showHomeTopActions: Boolean,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        pageNestedScrollConnection = NoOpPagerNestedScrollConnection,
    ) { pageIndex ->
        val page = pages.getOrNull(pageIndex) ?: return@HorizontalPager
        when (page) {
            MainTabPage.HomePage -> HomeScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                showTopActions = showHomeTopActions,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.FollowPage -> FollowScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                parentPagerState = pagerState,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.HotListPage -> HotListScreen(
                innerPadding = innerPadding,
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.DailyPage -> DailyScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.OnlineHistoryPage -> OnlineHistoryScreen(
                scrollToTopTrigger = scrollToTopTrigger,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.MyCollectionsPage -> MyCollectionsTopLevelPage(
                scrollToTopTrigger = scrollToTopTrigger,
                innerPadding = innerPadding,
                collectionDirectBrowseEnabled = collectionDirectBrowseEnabled,
                isActive = pagerState.currentPage == pageIndex,
            )
            MainTabPage.AccountPage -> AccountSettingScreen(
                innerPadding = innerPadding,
                isActive = pagerState.currentPage == pageIndex,
            )
        }
    }
}

@Composable
private fun MyCollectionsTopLevelPage(
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    collectionDirectBrowseEnabled: Boolean,
    isActive: Boolean,
) {
    val account = rememberAccountSettingsAccountState().value
    if (collectionDirectBrowseEnabled) {
        CollectionBrowseScreen(
            urlToken = account.urlToken,
            contentPadding = innerPadding,
            showBackButton = false,
            scrollToTopTrigger = scrollToTopTrigger,
            isActive = isActive,
        )
    } else {
        CollectionScreen(
            urlToken = account.urlToken,
            contentPadding = innerPadding,
            showBackButton = false,
            isActive = isActive,
        )
    }
}

private const val MAIN_TABS_SAVEABLE_KEY = "main_tabs"

/**
 * 计算双栏的横向分配。
 *
 * 列表栏与详情栏都排在导航栏右侧；铰链矩形使用窗口坐标系，减去两栏区域左边界后得到铰链在双栏内的位置。
 * 详情栏起点由列表栏宽度和铰链留白共同决定，保证详情内容不压在铰链上。
 */
private fun resolveTwoPaneMetrics(
    windowLayout: WindowLayout,
    panesStartInWindow: Dp,
    density: Density,
): TwoPaneMetrics {
    val hingeBounds = windowLayout.verticalSeparatingHinge?.boundsInWindowPx
    val hingeLeftInWindowDp = hingeBounds?.let { with(density) { it.left.toDp().value } }
    val hingeRightInWindowDp = hingeBounds?.let { with(density) { it.right.toDp().value } }
    return twoPaneMetricsFor(
        availableWidthDp = (windowLayout.widthDp - panesStartInWindow.value).coerceAtLeast(0f),
        panesLeftInWindowDp = panesStartInWindow.value,
        hingeLeftInWindowDp = hingeLeftInWindowDp,
        hingeRightInWindowDp = hingeRightInWindowDp,
    )
}

/**
 * 宽屏下的左侧导航栏。
 *
 * 与底部导航栏共用同一套 `nav_tab_*` test tag，UI 测试和自动化脚本在两种形态下都能选中同一个入口。
 */
@Composable
private fun MainNavigationRail(
    items: List<Triple<TopLevelDestination, String, ImageVector>>,
    currentDestination: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        items.forEach { (destination, label, icon) ->
            NavigationRailItem(
                selected = currentDestination?.let { it::class == destination::class } == true,
                onClick = { onSelect(destination) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}"),
            )
        }
    }
}

/** 展开态双栏右侧的空态：还没有从左侧列表选择内容。 */
@Composable
private fun MainDetailPlaceholder(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag("main_detail_placeholder"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Article,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "从左侧选择内容开始阅读",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val TopLevelDestination.openFrom: String?
    get() = when (this) {
        Home -> ContentOpenFrom.HOME_FEED
        OnlineHistory -> ContentOpenFrom.HISTORY
        else -> null
    }

internal fun NavBackStackEntry?.hasRoute(cls: KClass<out NavDestination>): Boolean {
    val dest = this?.destination ?: return false
    return dest.hierarchy.any { it.hasRoute(cls) }
}
