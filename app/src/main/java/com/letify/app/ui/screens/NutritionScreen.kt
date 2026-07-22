package com.letify.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.NoFeedbackButton
import com.letify.app.ui.components.ScreenHeader
import com.letify.app.ui.components.ScreenScaffold
import com.letify.app.ui.components.SectionTitle
import com.letify.app.ui.components.SegItem
import com.letify.app.ui.components.SegmentedTabs
import com.letify.app.ui.components.StackedRing
import com.letify.app.ui.components.WCard
import com.letify.app.ui.components.screenHPad
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.components.noFeedbackClick
import com.letify.app.ui.state.LocalAppState
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(onAddMeal: () -> Unit = {}, onWaterHistory: () -> Unit = {}) {
    var tab by remember { mutableStateOf("water") }

    ScreenScaffold(
        pinnedHeader = {
            ScreenHeader(
                title = "питание",
                // Plain "+" glyph to match the create button on the План tab —
                // one consistent create affordance across the app instead of a
                // different circle-plus icon here.
                trailingIcon = "add-bold",
                trailingAccent = true,
                onTrailingClick = onAddMeal,
            )
        }
    ) {
        Box(Modifier.screenHPad()) {
            SegmentedTabs(
                items = listOf(
                    SegItem("water", "Вода", "bottle-bold-duotone"),
                    SegItem("food", "Еда", "apple-bold-duotone"),
                ),
                selected = tab,
                onSelect = { tab = it },
            )
        }
        Box(Modifier.height(14.dp))
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                (fadeIn(tween(180)) + slideInHorizontally(tween(260)) { it / 12 })
                    .togetherWith(fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { -it / 24 })
            },
            label = "nutrition_pane"
        ) { current ->
            if (current == "water") WaterPane(onHistory = onWaterHistory) else FoodPane(onAddMeal = onAddMeal)
        }
    }
}

@Composable
private fun WaterPane(onHistory: () -> Unit = {}) {
    val state = LocalAppState.current
    val tiltProvider = rememberDeviceTilt()
    Column {
        // Water level now reads as a filling vessel (per request: "как будто
        // стакан, который наполняется") instead of a ring — reuses the same
        // LiquidVessel already built and battle-tested for habits on «План»,
        // just with the bottle icon and the water tint.
        Box(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                LiquidVessel(
                    fill = (state.waterMl.toFloat() / state.waterTarget).coerceIn(0f, 1f),
                    color = LetifyColors.Water,
                    icon = "bottle-bold-duotone",
                    size = 200.dp,
                    tiltProvider = tiltProvider,
                )
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.waterMl.toString(), color = Letify.colors.text, style = Letify.typography.displayLarge)
                Text("из ${state.waterTarget} мл", color = Letify.colors.muted, style = Letify.typography.bodySmall)
            }
        }
        SectionTitle("Добавить воду")
        Box(Modifier.screenHPad()) {
            WaterAmountPicker { ml ->
                // Don't cap at the goal — let the counter exceed the target so
                // over-drinking is reflected truthfully and matches what gets
                // logged in the history.
                state.addWater(ml, labelFor(ml), "bottle-bold-duotone")
            }
        }
        SectionTitle("История")
        WCard(
            modifier = Modifier.screenHPad().noFeedbackClick(onClick = onHistory),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).background(LetifyColors.Water.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    SolarIcon(name = "calendar-bold-duotone", tint = LetifyColors.Water, size = 20.dp)
                }
                Box(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Вся история", color = Letify.colors.text, style = Letify.typography.titleSmall)
                    Text("Записи по дням и статистика", color = Letify.colors.muted, style = Letify.typography.bodySmall)
                }
                SolarIcon(name = "alt-arrow-right-outline", tint = Letify.colors.muted, size = 18.dp)
            }
        }
    }
}

private fun labelFor(ml: Int): String = when {
    ml <= 250 -> "Глоток"
    ml <= 400 -> "Стакан воды"
    ml <= 600 -> "Бутылка"
    else -> "Большая бутылка"
}

// Replaces the old 2x2 quick-add button grid: drag the pimple (пимпочка) to
// dial in an amount, see it called out in the bubble above the thumb, then
// tap «Добавить» to log it — one adjustable control instead of four fixed
// presets. Range/step tuned for a single glass/bottle-sized pour; repeat
// taps of «Добавить» cover anything bigger.
private const val WaterPickerMinMl = 50
private const val WaterPickerMaxMl = 750
private const val WaterPickerStepMl = 25

@Composable
private fun WaterAmountPicker(onAdd: (Int) -> Unit) {
    val density = LocalDensity.current
    var trackSize by remember { mutableStateOf(IntSize.Zero) }
    var amountMl by remember { mutableStateOf(250) }
    val thumbDp = 32.dp
    val thumbPx = with(density) { thumbDp.toPx() }

    fun updateFromX(x: Float) {
        val usablePx = (trackSize.width - thumbPx).coerceAtLeast(1f)
        val f = ((x - thumbPx / 2f) / usablePx).coerceIn(0f, 1f)
        val raw = WaterPickerMinMl + f * (WaterPickerMaxMl - WaterPickerMinMl)
        amountMl = (raw / WaterPickerStepMl).roundToInt() * WaterPickerStepMl
    }

    val fraction = (amountMl - WaterPickerMinMl).toFloat() / (WaterPickerMaxMl - WaterPickerMinMl)
    val usablePx = (trackSize.width - thumbPx).coerceAtLeast(1f)
    val thumbCenterPx = thumbPx / 2f + fraction.coerceIn(0f, 1f) * usablePx
    val thumbOffsetDp by animateDpAsState(
        targetValue = with(density) { (thumbCenterPx - thumbPx / 2f).toDp() },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "waterThumb",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var bubbleWidthPx by remember { mutableFloatStateOf(0f) }
        val bubbleXDp by animateDpAsState(
            targetValue = with(density) {
                (thumbCenterPx - bubbleWidthPx / 2f)
                    .coerceIn(0f, (trackSize.width - bubbleWidthPx).coerceAtLeast(0f))
                    .toDp()
            },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "waterBubbleX",
        )
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            if (trackSize != IntSize.Zero) {
                Box(
                    Modifier
                        .offset(x = bubbleXDp, y = 0.dp)
                        .onSizeChanged { bubbleWidthPx = it.width.toFloat() },
                ) {
                    WaterAmountBubble(amountMl)
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Letify.colors.container, RoundedCornerShape(999.dp))
                .onSizeChanged { trackSize = it }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
                        updateFromX(down.position.x)
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            updateFromX(change.position.x)
                            change.consume()
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            if (trackSize != IntSize.Zero) {
                // Filled portion — rounded on both ends like the reference, so
                // it reads as its own little pill riding inside the track
                // rather than a hard-edged progress bar.
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .height(40.dp)
                        .width(with(density) { thumbCenterPx.toDp() })
                        .background(LetifyColors.Water, RoundedCornerShape(999.dp)),
                )
                Box(
                    Modifier
                        .offset(x = thumbOffsetDp)
                        .size(thumbDp)
                        .background(Color.White, RoundedCornerShape(999.dp)),
                )
            }
        }
        Box(Modifier.height(14.dp))
        NoFeedbackButton(
            onClick = { onAdd(amountMl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(LetifyColors.Water, RoundedCornerShape(18.dp))
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Добавить", color = Color.White, style = Letify.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun WaterAmountBubble(amountMl: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .background(LetifyColors.Water, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SolarIcon(name = "waterdrop-outline", tint = Color.White, size = 16.dp)
                Box(Modifier.width(6.dp))
                Text("$amountMl мл", color = Color.White, style = Letify.typography.titleSmall)
            }
        }
        // Small triangular tail — a 45°-rotated square clipped square, pulled
        // up under the bubble so it reads as one continuous speech-bubble
        // shape pointing straight down at the thumb.
        Box(
            Modifier
                .offset(y = (-5).dp)
                .size(10.dp)
                .graphicsLayer { rotationZ = 45f }
                .clip(RoundedCornerShape(2.dp))
                .background(LetifyColors.Water),
        )
    }
}

@Composable
private fun FoodPane(onAddMeal: () -> Unit = {}) {
    val state = LocalAppState.current
    Column {
        // Stacked macros ring sits directly on the page, no plate.
        Box(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                // Clamp each macro's fill to its third of the ring BEFORE the
                // 0.33 scale, so a macro that exceeds its target can't make its
                // segment overflow and wrap past the next one (total ≤ ~0.99).
                val pLen = (state.protein.toFloat() / state.proteinTarget).coerceIn(0f, 1f) * 0.33f
                val fLen = (state.fat.toFloat() / state.fatTarget).coerceIn(0f, 1f) * 0.33f
                val cLen = (state.carb.toFloat() / state.carbTarget).coerceIn(0f, 1f) * 0.33f
                StackedRing(
                    segments = listOf(
                        LetifyColors.Protein to pLen,
                        LetifyColors.Fat to fLen,
                        LetifyColors.Carb to cLen,
                    ),
                    size = 200.dp,
                    strokeWidth = 14.dp,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.kcal.toString(), color = Letify.colors.text, style = Letify.typography.displayLarge)
                    Text("из ${state.kcalTarget} ккал", color = Letify.colors.muted, style = Letify.typography.bodySmall)
                }
            }
        }
        Box(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().screenHPad(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MacroRow("Белки", state.protein, state.proteinTarget, "г", LetifyColors.Protein)
            MacroRow("Жиры", state.fat, state.fatTarget, "г", LetifyColors.Fat)
            MacroRow("Углеводы", state.carb, state.carbTarget, "г", LetifyColors.Carb)
        }
        SectionTitle("Сегодня")
        WCard(modifier = Modifier.screenHPad(), contentPadding = PaddingValues(8.dp)) {
            if (state.meals.isEmpty()) {
                EmptyHint("Пока нет приёмов пищи — добавь первый через «+» вверху")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    state.meals.forEach { meal ->
                        MealRow(meal.title, meal.icon, meal.color, meal.kcal, meal.description ?: "", onAdd = onAddMeal)
                    }
                }
            }
        }
    }
}

/** Friendly centred placeholder used when a list section has no rows yet. */
@Composable
private fun EmptyHint(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Letify.colors.muted,
            style = Letify.typography.bodySmall,
        )
    }
}

@Composable
private fun MacroRow(label: String, value: Int, target: Int, unit: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(10.dp).background(color, RoundedCornerShape(999.dp))
        )
        Box(Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value.toString(), color = Letify.colors.text, style = Letify.typography.titleMedium)
                Text(" / $target $unit", color = Letify.colors.muted, style = Letify.typography.bodySmall)
            }
            Text(label, color = Letify.colors.muted, style = Letify.typography.bodySmall)
        }
    }
}

@Composable
private fun MealRow(title: String, icon: String, color: Color, kcal: Int?, description: String, onAdd: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(color.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SolarIcon(name = icon, tint = color, size = 22.dp)
        }
        Box(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Letify.colors.text, style = Letify.typography.titleSmall)
            Text(
                if (kcal != null) "$description · $kcal ккал" else description,
                color = Letify.colors.muted, style = Letify.typography.bodySmall,
            )
        }
        NoFeedbackButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
            SolarIcon(name = "add-circle-bold-duotone", tint = Letify.colors.accent, size = 22.dp)
        }
    }
}
