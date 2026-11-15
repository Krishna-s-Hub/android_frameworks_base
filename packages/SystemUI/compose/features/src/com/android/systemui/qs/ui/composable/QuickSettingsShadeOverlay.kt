/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.ui.composable

import com.android.systemui.brightness.ui.compose.RadiantVerticalSlider
import com.android.systemui.brightness.ui.compose.RadiantVerticalBrightnessSlider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableFloatStateOf
import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Text
import com.android.compose.ui.graphics.painter.rememberDrawablePainter
import com.android.systemui.qs.panels.ui.compose.TileListener
import com.android.systemui.qs.panels.ui.viewmodel.TileViewModel
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.PlatformSliderDefaults
import com.android.compose.animation.scene.ContentScope
import com.android.compose.animation.scene.ElementKey
import com.android.compose.animation.scene.UserAction
import com.android.compose.animation.scene.UserActionResult
import com.android.compose.animation.scene.content.state.TransitionState
import com.android.compose.gesture.gesturesDisabled
import com.android.compose.lifecycle.DisposableEffectWithLifecycle
import com.android.compose.lifecycle.LaunchedEffectWithLifecycle
import com.android.compose.modifiers.thenIf
import com.android.systemui.brightness.ui.compose.BrightnessSliderContainer
import com.android.systemui.brightness.ui.compose.BrightnessSliderDimensions
import com.android.systemui.brightness.ui.compose.ContainerColors
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.development.ui.compose.BuildNumber
import com.android.systemui.development.ui.viewmodel.BuildNumberViewModel
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.media.remedia.ui.compose.Media
import com.android.systemui.media.remedia.ui.compose.MediaPresentationStyle
import com.android.systemui.notifications.ui.composable.SnoozableHeadsUpNotificationPlaceholder
import com.android.systemui.qs.composefragment.ui.GridAnchor
import com.android.systemui.qs.flags.QsDetailedView
import com.android.systemui.qs.panels.ui.compose.EditMode
import com.android.systemui.qs.panels.ui.compose.TileDetails
import com.android.systemui.qs.panels.ui.compose.TileGrid
import com.android.systemui.qs.panels.ui.compose.toolbar.Toolbar
import com.android.systemui.qs.panels.ui.viewmodel.toolbar.ToolbarViewModel
import com.android.systemui.qs.tiles.dialog.AudioDetailsViewModel
import com.android.systemui.qs.ui.composable.QuickSettingsShade.systemGestureExclusionInShade
import com.android.systemui.qs.ui.viewmodel.QuickSettingsContainerViewModel
import com.android.systemui.qs.ui.viewmodel.QuickSettingsShadeOverlayActionsViewModel
import com.android.systemui.qs.ui.viewmodel.QuickSettingsShadeOverlayContentViewModel
import com.android.systemui.res.R
import com.android.systemui.scene.shared.model.Overlays
import com.android.systemui.scene.ui.composable.LocalSceneContainerPreloadedResources
import com.android.systemui.scene.ui.composable.Overlay
import com.android.systemui.shade.ui.composable.ChipHighlightModel
import com.android.systemui.shade.ui.composable.OverlayShade
import com.android.systemui.shade.ui.composable.OverlayShadeHeader
import com.android.systemui.shade.ui.composable.QuickSettingsOverlayHeader
import com.android.systemui.shade.ui.composable.QuickSettingsOverlayPrivacyChip
import com.android.systemui.statusbar.notification.stack.shared.model.ShadeScrimBounds
import com.android.systemui.statusbar.notification.stack.shared.model.ShadeScrimShape
import com.android.systemui.statusbar.notification.stack.ui.view.NotificationScrollView
import com.android.systemui.statusbar.notification.stack.ui.viewmodel.NotificationsPlaceholderViewModel
import com.android.systemui.volume.panel.component.volume.slider.ui.viewmodel.AudioStreamSliderViewModel
import com.android.systemui.volume.panel.component.volume.ui.composable.VolumeSlider
import com.android.systemui.volume.panel.component.volume.ui.composable.VolumeSliderDimensions
import dagger.Lazy
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import com.android.systemui.media.remedia.ui.compose.RadiantMedia
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.key

@SysUISingleton
class QuickSettingsShadeOverlay
@Inject
constructor(
    private val actionsViewModelFactory: QuickSettingsShadeOverlayActionsViewModel.Factory,
    private val contentViewModelFactory: QuickSettingsShadeOverlayContentViewModel.Factory,
    private val quickSettingsContainerViewModelFactory: QuickSettingsContainerViewModel.Factory,
    private val notificationStackScrollView: Lazy<NotificationScrollView>,
    private val notificationsPlaceholderViewModelFactory: NotificationsPlaceholderViewModel.Factory,
) : Overlay {

    override val key = Overlays.QuickSettingsShade

    private val actionsViewModel: QuickSettingsShadeOverlayActionsViewModel by lazy {
        actionsViewModelFactory.create()
    }

    override val userActions: Flow<Map<UserAction, UserActionResult>> = actionsViewModel.actions

    override val alwaysCompose: Boolean = false

    override suspend fun activate(): Nothing {
        actionsViewModel.activate()
    }

    @Composable
    override fun ContentScope.Content(modifier: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val contentViewModel =
            rememberViewModel("QuickSettingsShadeOverlayContent", key = coroutineScope) {
                contentViewModelFactory.create(coroutineScope)
            }
        val useBrightnessMirrorInOverlay = useBrightnessMirrorInOverlay()
        val quickSettingsContainerViewModel =
            rememberViewModel("QuickSettingsShadeOverlayContainer") {
                quickSettingsContainerViewModelFactory.create(
                    supportsBrightnessMirroring = useBrightnessMirrorInOverlay
                )
            }
        val hunPlaceholderViewModel =
            rememberViewModel("QuickSettingsShadeOverlayPlaceholder") {
                notificationsPlaceholderViewModelFactory.create(Overlays.QuickSettingsShade)
            }

        val showBrightnessMirror =
            quickSettingsContainerViewModel.brightnessSliderViewModel.showMirror
        val contentAlphaFromBrightnessMirror by
            animateFloatAsState(if (showBrightnessMirror) 0f else 1f)

        val targetBlurRadiusPx: Float by
            remember(layoutState) {
                derivedStateOf {
                    contentViewModel.calculateTargetBlurRadius(layoutState.transitionState)
                }
            }
        val animatedBlurRadiusPx: Float by
            animateFloatAsState(targetValue = targetBlurRadiusPx, label = "NSOverlay-blurRadius")

        // Set the bounds to null when the QuickSettings overlay disappears.
        DisposableEffectWithLifecycle(Unit) {
            onDispose {
                contentViewModel.onPanelShapeInWindowChanged(null)
                contentViewModel.onShadeOverlayBoundsChanged(null)
            }
        }

        LaunchedEffectWithLifecycle(key1 = Unit) { contentViewModel.detectShadeModeChanges() }

        Box(
            modifier =
                modifier
                    .graphicsLayer { alpha = contentAlphaFromBrightnessMirror }
                    .blur(with(LocalDensity.current) { animatedBlurRadiusPx.toDp() })
                    .thenIf(showBrightnessMirror) { Modifier.gesturesDisabled() }
        ) {
            OverlayShade(
                panelElement = QuickSettingsShade.Elements.Panel,
                alignmentOnWideScreens = Alignment.End,
                statusBarHeightPx = contentViewModel.statusBarHeightPx,
                enableTransparency = contentViewModel.isTransparencyEnabled,
                onScrimClicked = contentViewModel::onScrimClicked,
                onBackgroundPlaced = { bounds, topCornerRadius, bottomCornerRadius ->
                    contentViewModel.onShadeOverlayBoundsChanged(bounds)
                    contentViewModel.onPanelShapeInWindowChanged(
                        ShadeScrimShape(
                            bounds = ShadeScrimBounds(bounds),
                            topRadius = topCornerRadius.roundToInt(),
                            bottomRadius = bottomCornerRadius.roundToInt(),
                        )
                    )
                },
                header = {
                    if (contentViewModel.showHeader) {
                        val headerViewModel = quickSettingsContainerViewModel.shadeHeaderViewModel
                        OverlayShadeHeader(
                            viewModel = headerViewModel,
                            notificationsHighlight = headerViewModel.inactiveChipHighlight,
                            quickSettingsHighlight = ChipHighlightModel.Strong,
                            showClock = true,
                            modifier = Modifier.element(QuickSettingsShade.Elements.StatusBar),
                        )
                    }
                },
            ) {
                QuickSettingsContainer(
                    contentViewModel = contentViewModel,
                    containerViewModel = quickSettingsContainerViewModel,
                )
            }
            SnoozableHeadsUpNotificationPlaceholder(
                tag = "QSShadeOverlay",
                stackScrollView = notificationStackScrollView.get(),
                viewModel = hunPlaceholderViewModel,
            )
        }
    }
}

/** The possible states of the `ShadeBody`. */
private sealed interface ShadeBodyState {
    data object Editing : ShadeBodyState

    data object TileDetails : ShadeBodyState

    data object Default : ShadeBodyState
}

@Composable
@ReadOnlyComposable
private fun useBrightnessMirrorInOverlay(): Boolean {
    // The `config_useBrightnessMirrorInOverlay` config is true by default. If false, the Quick
    // Settings shade overlay will remain visible during brightness adjustments.
    return LocalResources.current.getBoolean(R.bool.config_useBrightnessMirrorInOverlay)
}

@Composable
private fun ContentScope.QuickSettingsContainer(
    contentViewModel: QuickSettingsShadeOverlayContentViewModel,
    containerViewModel: QuickSettingsContainerViewModel,
    modifier: Modifier = Modifier,
) {
    val isEditing by containerViewModel.editModeViewModel.isEditing.collectAsStateWithLifecycle()
    val tileDetails =
        if (QsDetailedView.isEnabled) containerViewModel.detailsViewModel.activeTileDetails
        else null

    val accessibilityTitle =
        when {
            isEditing -> stringResource(R.string.accessibility_desc_quick_settings_edit)
            (QsDetailedView.isEnabled && tileDetails != null) -> tileDetails.title
            else -> stringResource(R.string.accessibility_desc_quick_settings)
        }

    val focusRequester = remember { FocusRequester() }

    var currentHeight by remember { mutableStateOf(0) }

    LaunchedEffectWithLifecycle(focusRequester) {
        // Request focus on the `QuickSettingsContainer` without user interaction so that the user
        // can press the tab key once to enter the Quick Settings area. Without this line, the user
        // has to tab through unrelated views of the higher view hierarchy level.
        focusRequester.requestFocus()
    }

    AnimatedContent(
        modifier =
            Modifier.focusRequester(focusRequester)
                .focusable()
                .onLayoutRectChanged { currentHeight = it.height }
                .semantics { paneTitle = accessibilityTitle }
                .sysuiResTag("quick_settings_container"),
        targetState =
            when {
                isEditing -> ShadeBodyState.Editing
                tileDetails != null -> ShadeBodyState.TileDetails
                else -> ShadeBodyState.Default
            },
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
    ) { state ->
        when (state) {
            // RCOS_UNIFIED_EDITOR_V19A
            ShadeBodyState.Editing -> {
                QuickSettingsLayout(
                    qsContainerViewModel = containerViewModel,
                    toolbarViewModelFactory = contentViewModel.toolbarViewModelFactory,
                    buildNumberViewModelFactory = contentViewModel.buildNumberViewModelFactory,
                    isTransparencyEnabled = contentViewModel.isTransparencyEnabled,
                    volumeSliderViewModel = contentViewModel.volumeSliderViewModel,
                    audioDetailsViewModelFactory = contentViewModel.audioDetailsViewModelFactory,
                    modifier = modifier.sysuiResTag("quick_settings_panel"),
                )
            }
            ShadeBodyState.TileDetails -> {
                TileDetails(
                    modifier = modifier,
                    containerViewModel.detailsViewModel,
                    initialHeight = { currentHeight },
                )
            }

            ShadeBodyState.Default -> {
                QuickSettingsLayout(
                    qsContainerViewModel = containerViewModel,
                    toolbarViewModelFactory = contentViewModel.toolbarViewModelFactory,
                    buildNumberViewModelFactory = contentViewModel.buildNumberViewModelFactory,
                    isTransparencyEnabled = contentViewModel.isTransparencyEnabled,
                    volumeSliderViewModel = contentViewModel.volumeSliderViewModel,
                    audioDetailsViewModelFactory = contentViewModel.audioDetailsViewModelFactory,
                    modifier = modifier.sysuiResTag("quick_settings_panel"),
                )
            }
        }
    }
}

/** Column containing Brightness and QS tiles. */
@Composable
private fun ContentScope.QuickSettingsLayout(
    qsContainerViewModel: QuickSettingsContainerViewModel,
    toolbarViewModelFactory: ToolbarViewModel.Factory,
    buildNumberViewModelFactory: BuildNumberViewModel.Factory,
    isTransparencyEnabled: Boolean,
    volumeSliderViewModel: AudioStreamSliderViewModel?,
    audioDetailsViewModelFactory: AudioDetailsViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = QuickSettingsShade.Dimensions.HorizontalPadding),
    ) {
        if (LocalSceneContainerPreloadedResources.current.isFullWidthShade) {
            QuickSettingsOverlayPrivacyChip(qsContainerViewModel.shadeHeaderViewModel)
            VerticalSeparator(QuickSettingsShade.Dimensions.ShortPadding)
            QuickSettingsOverlayHeader(
                viewModel = qsContainerViewModel.shadeHeaderViewModel,
                modifier = Modifier.element(QuickSettingsShade.Elements.Header),
            )

            VerticalSeparator(QuickSettingsShade.Dimensions.ShortPadding)
        } else {
            VerticalSeparator(QuickSettingsShade.Dimensions.VerticalPadding)
            QuickSettingsOverlayPrivacyChip(
                qsContainerViewModel.shadeHeaderViewModel,
                modifier = Modifier.padding(bottom = QuickSettingsShade.Dimensions.ShortPadding),
            )
        }

        val toolbarViewModel =
            rememberViewModel("QuickSettingsLayout") { toolbarViewModelFactory.create() }

        Toolbar(
            modifier =
                Modifier.fillMaxWidth()
                    .requiredHeight(QuickSettingsShade.Dimensions.ToolbarHeight)
                    .sysuiResTag("quick_settings_toolbar"),
            viewModel = toolbarViewModel,
            isFullyVisible = { layoutState.isIdle(contentKey) },
        )

        VerticalSeparator(QuickSettingsShade.Dimensions.ToolbarBottomPadding)


        // RCOS_MODULAR_CENTER_V15
        // Independent RCOS Control Center composition.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            val allTiles =
                qsContainerViewModel.tileGridViewModel.tileViewModels

            val connectivityOrder = listOf(
                "wifi", "internet", "bt", "cell", "airplane"
            )

            // Prefer Wi-Fi over the combined Internet tile
            // when both are available.
            val wifiTile = allTiles.firstOrNull {
                it.spec.spec == "wifi"
            } ?: allTiles.firstOrNull {
                it.spec.spec == "internet"
            }

            val connectivityTiles = listOfNotNull(
                wifiTile,
                allTiles.firstOrNull { it.spec.spec == "bt" },
                allTiles.firstOrNull { it.spec.spec == "cell" },
                allTiles.firstOrNull { it.spec.spec == "airplane" },
            )

            val connectivityIds =
                connectivityTiles.map { it.spec }.toSet()



            // Maintain the normal Android QS listening lifecycle.
            TileListener(allTiles, listeningEnabled = { true })


            // RCOS_EDITABLE_GRID_V18
            val focusTile = allTiles.firstOrNull {
                it.spec.spec in setOf("modes", "dnd")
            }

            val fixedTileSpecs = setOf(
                "wifi", "internet", "bt", "cell", "airplane"
            )

            val shortcutTiles = allTiles.filter {
                it.spec.spec !in fixedTileSpecs &&
                    it != focusTile
            }

            val moduleIds = buildList {
                add("connectivity")
                add("media")
                if (focusTile != null) add("focus")
                add("brightness")
                add("volume")
                addAll(
                    shortcutTiles.map { "tile:" + it.spec.spec }
                )
            }

            val radiantContext = LocalContext.current
            val radiantAudioManager = remember(radiantContext) {
                radiantContext.getSystemService(
                    AudioManager::class.java
                )
            }

            val radiantMaxVolume = remember(radiantAudioManager) {
                radiantAudioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    .coerceAtLeast(1)
            }

            var radiantMediaVolume by remember(radiantAudioManager) {
                mutableFloatStateOf(
                    radiantAudioManager.getStreamVolume(
                        AudioManager.STREAM_MUSIC
                    ).toFloat()
                )
            }

            RadiantEditableGrid(
                editModeViewModel = qsContainerViewModel.editModeViewModel,
                availableIds = moduleIds,
                modifier = Modifier.fillMaxWidth(),
            ) { moduleId ->
                when (moduleId) {
                    "connectivity" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .radiantModuleSurface()
                                .padding(10.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(7.dp),
                        ) {
                            val wireless = listOfNotNull(
                                allTiles.firstOrNull {
                                    it.spec.spec == "wifi"
                                } ?: allTiles.firstOrNull {
                                    it.spec.spec == "internet"
                                },
                                allTiles.firstOrNull {
                                    it.spec.spec == "bt"
                                },
                                allTiles.firstOrNull {
                                    it.spec.spec == "cell"
                                },
                                allTiles.firstOrNull {
                                    it.spec.spec == "airplane"
                                },
                            )

                            wireless.chunked(2).forEach { rowTiles ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(7.dp),
                                ) {
                                    rowTiles.forEach { tile ->
                                        RadiantModuleTile(
                                            tile = tile,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                        )
                                    }
                                    repeat(2 - rowTiles.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    "media" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .radiantModuleSurface(),
                        ) {
                            RadiantMedia(
                                viewModelFactory =
                                    qsContainerViewModel.mediaViewModelFactory,
                                behavior =
                                    QuickSettingsContainerViewModel.mediaUiBehavior,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    "focus" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .radiantModuleSurface()
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                        ) {
                            if (focusTile != null) {
                                RadiantModuleTile(
                                    tile = focusTile,
                                    modifier = Modifier.size(48.dp),
                                )
                            }
                            Text(
                                text = "Focus",
                                color = Color.White,
                                maxLines = 1,
                            )
                        }
                    }

                    "brightness" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (
                                qsContainerViewModel
                                    .isBrightnessSliderVisible
                            ) {
                                RadiantVerticalBrightnessSlider(
                                    viewModel =
                                        qsContainerViewModel
                                            .brightnessSliderViewModel,
                                )
                            }
                        }
                    }

                    "volume" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            RadiantVerticalSlider(
                                value = radiantMediaVolume,
                                valueRange =
                                    0f..radiantMaxVolume.toFloat(),
                                symbol = "♪",
                                onValueChange = { value ->
                                    radiantMediaVolume = value
                                    radiantAudioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        value.roundToInt(),
                                        0,
                                    )
                                },
                                onValueChangeFinished = {},
                            )
                        }
                    }

                    else -> {
                        val tile = shortcutTiles.firstOrNull {
                            moduleId == "tile:" + it.spec.spec
                        }

                        if (tile != null) {
                            RadiantModuleTile(
                                tile = tile,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            GridAnchor()

            val buildNumberViewModel =
                rememberViewModel("QuickSettingsShadeOverlay.BuildNumber") {
                    buildNumberViewModelFactory.create()
                }

            if (buildNumberViewModel.buildNumber != null) {
                VerticalSeparator(QuickSettingsShade.Dimensions.ShortPadding)
                BuildNumber(
                    viewModel = buildNumberViewModel,
                    modifier =
                        Modifier.align(Alignment.Start)
                            .padding(start = QuickSettingsShade.Dimensions.HorizontalPadding),
                )
            }

            VerticalSeparator(QuickSettingsShade.Dimensions.VerticalPadding)
        }
    }
}



// RCOS_MODULAR_CENTER_V15
// Shared glass surface for independent Control Center modules.
private fun Modifier.radiantModuleSurface(): Modifier =
    this
        .clip(RoundedCornerShape(27.dp))
        .background(Color(0x44272D36))
        .border(
            1.dp,
            Color.White.copy(alpha = 0.10f),
            RoundedCornerShape(27.dp),
        )

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RadiantModuleTile(
    tile: TileViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by tile.state.collectAsStateWithLifecycle(
        initialValue = tile.currentState
    )

    val active =
        state.state == android.service.quicksettings.Tile.STATE_ACTIVE

    val available =
        state.state != android.service.quicksettings.Tile.STATE_UNAVAILABLE

    val spec = tile.spec.spec.lowercase()

    val activeBackground = when (spec) {
        "wifi", "internet", "bt" -> Color(0xFF3478F6)
        "airplane" -> Color(0xFFFF9F0A)
        "cell" -> Color(0xFF32C76A)
        else -> Color(0xFFE8EBF2)
    }

    val background =
        if (active) activeBackground else Color(0x55323843)

    val foreground =
        if (active && spec !in setOf(
                "wifi", "internet", "bt", "airplane", "cell"
            )
        ) {
            Color(0xFF20242C)
        } else {
            Color.White
        }

    val drawable = remember(state.icon, context) {
        state.icon?.getDrawable(context)
    }

    val title = state.label?.toString() ?: tile.spec.spec

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(
                1.dp,
                Color.White.copy(alpha = .12f),
                CircleShape,
            )
            .combinedClickable(
                enabled = available,
                // RCOS_WIRELESS_DIALOG_FIX_V16
                // These controls may open dialogs. Our custom circular
                // renderer does not provide a valid TransitionSource.
                onClick = {
                    if (spec in setOf("wifi", "internet", "bt")) {
                        tile.mainClick(null)
                    } else {
                        tile.mainClick(tile.expandable)
                    }
                },
                onLongClick = {
                    if (spec in setOf("wifi", "internet", "bt")) {
                        tile.settingsClick(null)
                    } else {
                        tile.settingsClick(tile.expandable)
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (drawable != null) {
            Icon(
                painter = rememberDrawablePainter(drawable),
                contentDescription = title,
                tint = foreground,
                modifier = Modifier.size(29.dp),
            )
        } else {
            Text(
                text = title.take(1),
                color = foreground,
            )
        }
    }
}

// RCOS_IOS26_CONTROL_CENTER_V09
// Neutral glass module surface; native window blur remains separate.
private fun Modifier.radiantGlassCard(): Modifier =
    this
        .clip(RoundedCornerShape(28.dp))
        .background(Color(0x38232930))
        .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(28.dp),
        )
        .padding(12.dp)

@Composable
private fun VerticalSeparator(height: Dp) {
    Spacer(Modifier.height(height = height))
}

object QuickSettingsShade {
    object Elements {
        val StatusBar = ElementKey("QuickSettingsShadeOverlayStatusBar")
        val Panel = ElementKey("QuickSettingsShadeOverlayPanel")
        val Header = ElementKey("QuickSettingsShadeOverlayHeader")
    }

    object Dimensions {
        val brightnessSliderDimensions: BrightnessSliderDimensions
            @Composable
            @ReadOnlyComposable
            get() =
                BrightnessSliderDimensions(
                    DpSize(sliderIconSize, sliderIconSize),
                    brightnessThumbHeight,
                    brightnessThumbWidth,
                    brightnessTrackHeight,
                    brightnessVerticalPadding,
                    brightnessRoundedCorner,
                    brightnessFrameWidth,
                    brightnessFrameHeight,
                )

        val HorizontalPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_horizontal_padding)

        val VerticalPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_vertical_padding)

        val VolumeSliderDimensions: VolumeSliderDimensions
            @Composable
            @ReadOnlyComposable
            get() =
                VolumeSliderDimensions(
                    sliderIconSize,
                    volumeThumbHeight,
                    volumeThumbWidth,
                    volumeTrackHeight,
                    volumeVerticalPadding,
                )

        val ToolbarBottomPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.toolbar_bottom_padding)

        val VolumeSliderExtraPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_volume_extra_padding)

        val ToolbarHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_toolbar_height)

        // This is used around the header and toolbar
        val ShortPadding = 8.dp

        private val brightnessThumbHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_thumb_height)

        private val brightnessThumbWidth: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_thumb_width)

        private val brightnessTrackHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_track_height)

        private val brightnessVerticalPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_vertical_padding)

        private val brightnessRoundedCorner: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_rounded_corner)

        private val brightnessFrameWidth: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_frame_width)

        private val brightnessFrameHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_frame_height)

        private val sliderIconSize: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_brightness_icon_size)

        private val volumeVerticalPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_volume_vertical_padding)

        private val volumeThumbHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_volume_thumb_height)

        private val volumeThumbWidth: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_volume_thumb_width)

        private val volumeTrackHeight: Dp
            @Composable
            @ReadOnlyComposable
            get() = dimensionResource(id = R.dimen.overlay_qs_layout_volume_track_height)
    }

    /**
     * Applies system gesture exclusion to a component adding [Dimensions.Padding] to left and
     * right.
     */
    @Composable
    fun Modifier.systemGestureExclusionInShade(enabled: () -> Boolean): Modifier {
        val density = LocalDensity.current
        val padding = Dimensions.HorizontalPadding
        return thenIf(enabled()) {
            Modifier.systemGestureExclusion { layoutCoordinates ->
                val sidePadding = with(density) { padding.toPx() }
                Rect(
                    offset = Offset(x = -sidePadding, y = 0f),
                    size =
                        Size(
                            width = layoutCoordinates.size.width.toFloat() + 2 * sidePadding,
                            height = layoutCoordinates.size.height.toFloat(),
                        ),
                )
            }
        }
    }
}


// RCOS_EDITABLE_GRID_V18
// Persistent, movable and resizable Radiant Control Center modules.
private data class RadiantGridEntry(
    val id: String,
    val columns: Int,
    val rows: Int,
)

private fun radiantDefaultEntry(id: String): RadiantGridEntry =
    when (id) {
        "connectivity", "media" ->
            RadiantGridEntry(id, 2, 2)
        "focus" ->
            RadiantGridEntry(id, 2, 1)
        "brightness", "volume" ->
            RadiantGridEntry(id, 1, 2)
        else ->
            RadiantGridEntry(id, 1, 1)
    }

@Composable
private fun RadiantEditableGrid(
    editModeViewModel:
        com.android.systemui.qs.panels.ui.viewmodel.EditModeViewModel,
    availableIds: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(
            "rcos_radiant_grid_v18",
            android.content.Context.MODE_PRIVATE,
        )
    }

    val availableKey = availableIds.joinToString("|")

    val entries = remember(availableKey) {
        val saved = preferences.getString("layout", null)

        val restored = saved
            ?.split("|")
            ?.mapNotNull { token ->
                val fields = token.split(";")
                if (fields.size != 3) return@mapNotNull null

                val id = fields[0]
                val cols = fields[1].toIntOrNull()
                    ?: return@mapNotNull null
                val rows = fields[2].toIntOrNull()
                    ?: return@mapNotNull null

                if (id !in availableIds) return@mapNotNull null

                RadiantGridEntry(
                    id,
                    cols.coerceIn(1, 4),
                    rows.coerceIn(1, 4),
                )
            }
            .orEmpty()
            .distinctBy { it.id }

        val hidden = preferences.getStringSet(
            "hidden",
            emptySet<String>(),
        ).orEmpty()

        mutableStateListOf<RadiantGridEntry>().apply {
            addAll(restored)

            availableIds.forEach { id ->
                if (
                    none { it.id == id } &&
                    id !in hidden
                ) {
                    add(radiantDefaultEntry(id))
                }
            }
        }
    }

    val editing by editModeViewModel.isEditing
        .collectAsStateWithLifecycle()

    var showAddMenu by remember {
        mutableStateOf(false)
    }

    val hiddenIds = availableIds.filter { id ->
        entries.none { it.id == id }
    }

    fun saveLayout() {
        preferences.edit()
            .putString(
                "layout",
                entries.joinToString("|") {
                    "${it.id};${it.columns};${it.rows}"
                },
            )
            .putStringSet(
                "hidden",
                availableIds.filter { id ->
                    entries.none { it.id == id }
                }.toSet(),
            )
            .apply()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (editing) {
                    "Edit and sort"
                } else {
                    "Radiant Controls"
                },
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0x44FFFFFF))
                    .clickable {
                        if (editing) {
                            saveLayout()
                            editModeViewModel.stopEditing()
                        } else {
                            editModeViewModel.startEditing()
                        }
                    }
                    .padding(
                        horizontal = 17.dp,
                        vertical = 8.dp,
                    ),
            ) {
                Text(
                    text = if (editing) "✓" else "+",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        val gapPx = with(LocalDensity.current) {
            9.dp.roundToPx()
        }

        val approximateCellPx = with(LocalDensity.current) {
            90.dp.toPx()
        }

        Layout(
            content = {
                entries.forEach { entry ->
                    key(entry.id) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            content(entry.id)

                            if (editing) {
                                var dragOffset by remember(entry.id) {
                                    mutableStateOf(Offset.Zero)
                                }

                                // The transparent overlay intercepts
                                // interactions only in Edit Mode.
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .pointerInput(entry.id, editing) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    dragOffset = Offset.Zero
                                                },
                                                onDrag = { change, amount ->
                                                    change.consume()
                                                    dragOffset += amount
                                                },
                                                onDragEnd = {
                                                    val shift =
                                                        (dragOffset.x /
                                                            approximateCellPx)
                                                            .roundToInt() +
                                                        (
                                                            dragOffset.y /
                                                                approximateCellPx
                                                        ).roundToInt() * 4

                                                    val index =
                                                        entries.indexOfFirst {
                                                            it.id == entry.id
                                                        }

                                                    if (index >= 0 && shift != 0) {
                                                        val target =
                                                            (index + shift)
                                                                .coerceIn(
                                                                    0,
                                                                    entries.lastIndex,
                                                                )

                                                        val moving =
                                                            entries.removeAt(index)

                                                        entries.add(
                                                            target,
                                                            moving,
                                                        )

                                                        saveLayout()
                                                    }

                                                    dragOffset = Offset.Zero
                                                },
                                            )
                                        },
                                )

                                // Remove control.
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F1F5))
                                        .clickable {
                                            entries.removeAll {
                                                it.id == entry.id
                                            }
                                            saveLayout()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "−",
                                        color = Color.Black,
                                    )
                                }

                                // Resize handle: drag outward or inward.
                                var resizeOffset by remember(entry.id) {
                                    mutableStateOf(Offset.Zero)
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(31.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xBBDDDAE5))
                                        .pointerInput(entry.id) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    resizeOffset = Offset.Zero
                                                },
                                                onDrag = { change, amount ->
                                                    change.consume()
                                                    resizeOffset += amount
                                                },
                                                onDragEnd = {
                                                    val dx =
                                                        (resizeOffset.x /
                                                            approximateCellPx)
                                                            .roundToInt()
                                                    val dy =
                                                        (resizeOffset.y /
                                                            approximateCellPx)
                                                            .roundToInt()

                                                    val index =
                                                        entries.indexOfFirst {
                                                            it.id == entry.id
                                                        }

                                                    if (index >= 0) {
                                                        val old = entries[index]

                                                        entries[index] =
                                                            old.copy(
                                                                columns =
                                                                    (old.columns + dx)
                                                                        .coerceIn(1, 4),
                                                                rows =
                                                                    (old.rows + dy)
                                                                        .coerceIn(1, 4),
                                                            )
                                                        saveLayout()
                                                    }

                                                    resizeOffset = Offset.Zero
                                                },
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "↘",
                                        color = Color(0xFF24242B),
                                    )
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val cellSize = (
                (width - 3 * gapPx) / 4
            ).coerceAtLeast(1)

            val occupied = ArrayList<BooleanArray>()
            val positions = ArrayList<Pair<Int, Int>>()

            fun ensureRows(count: Int) {
                while (occupied.size < count) {
                    occupied.add(BooleanArray(4))
                }
            }

            entries.forEach { entry ->
                val spanX = entry.columns.coerceIn(1, 4)
                val spanY = entry.rows.coerceIn(1, 4)

                var positionX = 0
                var positionY = 0
                var placed = false

                for (y in 0..200) {
                    ensureRows(y + spanY)

                    for (x in 0..(4 - spanX)) {
                        var fits = true

                        for (yy in y until y + spanY) {
                            for (xx in x until x + spanX) {
                                if (occupied[yy][xx]) {
                                    fits = false
                                }
                            }
                        }

                        if (fits) {
                            positionX = x
                            positionY = y
                            placed = true
                            break
                        }
                    }

                    if (placed) break
                }

                for (yy in positionY until positionY + spanY) {
                    for (xx in positionX until positionX + spanX) {
                        occupied[yy][xx] = true
                    }
                }

                positions.add(positionX to positionY)
            }

            val placeables = measurables.mapIndexed { index, measurable ->
                val entry = entries[index]

                val childWidth =
                    cellSize * entry.columns +
                    gapPx * (entry.columns - 1)

                val childHeight =
                    cellSize * entry.rows +
                    gapPx * (entry.rows - 1)

                measurable.measure(
                    Constraints.fixed(
                        childWidth,
                        childHeight,
                    )
                )
            }

            val usedRows = entries.indices.maxOfOrNull { index ->
                positions[index].second + entries[index].rows
            } ?: 0

            val gridHeight = (
                usedRows * cellSize +
                    (usedRows - 1).coerceAtLeast(0) * gapPx
            ).coerceAtLeast(0)

            layout(width, gridHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val (x, y) = positions[index]

                    placeable.placeRelative(
                        x * (cellSize + gapPx),
                        y * (cellSize + gapPx),
                    )
                }
            }
        }

        if (editing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box {
                    Text(
                        text = "⊕  Add a Control",
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable {
                                showAddMenu = true
                            }
                            .padding(12.dp),
                        color = Color.White,
                    )

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = {
                            showAddMenu = false
                        },
                    ) {
                        if (hiddenIds.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text("No hidden layout modules")
                                },
                                onClick = {
                                    showAddMenu = false
                                },
                            )
                        }

                        hiddenIds.forEach { id ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        id.removePrefix("tile:")
                                            .replaceFirstChar {
                                                it.uppercase()
                                            }
                                    )
                                },
                                onClick = {
                                    entries.add(
                                        radiantDefaultEntry(id)
                                    )
                                    saveLayout()
                                    showAddMenu = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
