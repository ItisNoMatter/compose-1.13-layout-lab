package com.example.inomasandbox.`FlexBox-Grid-lab`

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 画面共通の小さな部品たち。
 *
 * ここ自体も Compose 1.13 で stable になった [FlexBox] を使っている
 * ([ChoiceRow] のチップは行があふれると自動で折り返す)。
 */

/** デモ用のカラーパレット。ライト/ダークどちらでも白文字が読める明度に揃えてある。 */
object LabPalette {
    val Indigo = Color(0xFF4F46E5)
    val Teal = Color(0xFF0D9488)
    val Amber = Color(0xFFB45309)
    val Rose = Color(0xFFBE123C)
    val Violet = Color(0xFF7C3AED)
    val Sky = Color(0xFF0369A1)
    val Lime = Color(0xFF4D7C0F)
    val Slate = Color(0xFF475569)

    /** 添字でぐるぐる回して使う。 */
    val cycle = listOf(Indigo, Teal, Amber, Rose, Violet, Sky, Lime, Slate)

    operator fun get(index: Int): Color = cycle[index.mod(cycle.size)]
}

/** タイトル・説明・コードスニペット付きのデモ枠。 */
@Composable
fun DemoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    code: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
            if (code != null) CodeSnippet(code)
        }
    }
}

/** 折りたたみ式のコード表示。横に長い行はスクロールできる。 */
@Composable
fun CodeSnippet(code: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        TextButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            Text(if (expanded) "コードを隠す" else "コードを見る")
        }
        if (expanded) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                )
            }
        }
    }
}

/** デモの中に置く色付きブロック。 */
@Composable
fun Swatch(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: Int = 13,
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** デモの土台。中身の広がりが見えるように薄い枠と背景を敷く。 */
@Composable
fun DemoStage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(8.dp),
    ) {
        content()
    }
}

/** ラベル + チップ列。選択肢が多いときは FlexBox が勝手に折り返す。 */
@Composable
fun <T> ChoiceRow(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlexBox(
            modifier = Modifier.fillMaxWidth(),
            config = {
                wrap(FlexWrap.Wrap)
                alignItems(FlexAlignItems.Center)
                gap(row = 4.dp, column = 6.dp)
            },
        ) {
            options.forEach { (name, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(name, fontSize = 12.sp) },
                )
            }
        }
    }
}

/** 値を数字でも見せるスライダー。 */
@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    valueLabel: String = value.roundToInt().toString(),
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                valueLabel,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.height(28.dp),
        )
    }
}

/**
 * 幅を自由に変えられるデモ枠。
 *
 * 端末を回さなくても「狭い画面 / 広い画面」を再現できるので、
 * `constraints` を見てレイアウトを切り替える FlexBox / Grid の挙動がその場で確認できる。
 */
@Composable
fun ResizableStage(
    widthDp: Float,
    onWidthChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 260f..900f,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LabeledSlider(
            label = "コンテナ幅",
            value = widthDp,
            valueRange = range,
            onValueChange = onWidthChange,
            valueLabel = widthDp.roundToInt().toString() + "dp",
        )
        Text(
            "画面より広くするとこの枠は横スクロールできる",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            DemoStage(modifier = Modifier.width(widthDp.dp)) { content() }
        }
    }
}

/** 現在の幅がどのブレークポイントに入っているかを示すバッジ。 */
@Composable
fun BreakpointBadge(
    widthDp: Float,
    compactBelow: Int,
    mediumBelow: Int,
    modifier: Modifier = Modifier,
) {
    val name = when {
        widthDp < compactBelow -> "Compact (< " + compactBelow + "dp)"
        widthDp < mediumBelow -> "Medium (< " + mediumBelow + "dp)"
        else -> "Expanded (>= " + mediumBelow + "dp)"
    }
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
