package com.example.inomasandbox.`FlexBox-Grid-lab`

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.rows
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Compose 1.13 で stable になった Grid のデモ画面。
 *
 * Grid は CSS Grid に近い 2 次元レイアウト。
 * トラック (列・行) を `config` で宣言し、子は `Modifier.gridItem(...)` で
 * どのセルに乗るかを指定する。指定しない子は自動で空きセルに流れる。
 */
@Composable
fun GridLab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LabIntro(
            title = "Grid",
            body = "列と行のサイズを fr (比率) / dp (固定) / Auto (中身依存) / minmax で宣言し、" +
                "子をセルに割り付ける 2 次元レイアウト。" +
                "LazyVerticalGrid と違いスクロールしないので、画面骨格そのものを組むのに向く。",
        )
        GridTrackSizeCard()
        GridSpanCard()
        GridFlowCard()
        GridNamedAreaCard()
        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// 1. トラックサイズ
// ---------------------------------------------------------------------------

private const val TRACK_FR = 0
private const val TRACK_FIXED = 1
private const val TRACK_AUTO = 2
private const val TRACK_PERCENT = 3

private val trackOptions = listOf(
    "1.fr" to TRACK_FR,
    "Fixed(96.dp)" to TRACK_FIXED,
    "Auto" to TRACK_AUTO,
    "Percentage(0.25f)" to TRACK_PERCENT,
)

private val trackLabels = mapOf(
    TRACK_FR to "余りを均等に分け合う。合計が常に親幅ぴったりになる。",
    TRACK_FIXED to "どの列も 96dp 固定。足りなければはみ出す。",
    TRACK_AUTO to "中身の幅に合わせて列幅が決まる。",
    TRACK_PERCENT to "親幅の 25% ずつ。",
)

private val gridWords = listOf("東京", "大阪", "名古屋", "札幌", "福岡", "仙台", "広島", "那覇")

@Composable
private fun GridTrackSizeCard() {
    var columnCount by rememberSaveable { mutableIntStateOf(3) }
    var trackKind by rememberSaveable { mutableIntStateOf(TRACK_FR) }
    var gapDp by rememberSaveable { mutableFloatStateOf(8f) }

    val trackExpr = when (trackKind) {
        TRACK_FIXED -> "column(GridTrackSize.Fixed(96.dp))"
        TRACK_AUTO -> "column(GridTrackSize.Auto)"
        TRACK_PERCENT -> "column(GridTrackSize.Percentage(0.25f))"
        else -> "column(1.fr)"
    }

    DemoCard(
        title = "1. トラックサイズ — fr / dp / Auto / %",
        description = "列の定義を変えるだけで、同じ子要素の並びがまったく違う見え方になる。" +
            "行は宣言していないので暗黙トラック (Auto) として中身の高さに合わせて増えていく。",
        code = """
            Grid(
                config = {
                    repeat($columnCount) { $trackExpr }
                    gap(${gapDp.roundToInt()}.dp)
                    // 行は宣言していない -> 暗黙トラック (Auto) が必要なだけ生える
                },
            ) {
                // gridItem を付けない子は空きセルへ自動配置される
                words.forEach { Cell(it) }
            }
        """.trimIndent(),
    ) {
        ChoiceRow("列のサイズ", trackOptions, trackKind) { trackKind = it }
        LabeledSlider(
            label = "列数",
            value = columnCount.toFloat(),
            valueRange = 1f..4f,
            steps = 2,
            onValueChange = { columnCount = it.roundToInt() },
            valueLabel = columnCount.toString(),
        )
        LabeledSlider(
            label = "gap",
            value = gapDp,
            valueRange = 0f..24f,
            steps = 5,
            onValueChange = { gapDp = it },
            valueLabel = gapDp.roundToInt().toString() + "dp",
        )
        DemoStage {
            Grid(
                config = {
                    repeat(columnCount) {
                        when (trackKind) {
                            TRACK_FIXED -> column(GridTrackSize.Fixed(96.dp))
                            TRACK_AUTO -> column(GridTrackSize.Auto)
                            TRACK_PERCENT -> column(GridTrackSize.Percentage(0.25f))
                            else -> column(1.fr)
                        }
                    }
                    gap(gapDp.dp)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                gridWords.forEachIndexed { index, word ->
                    Swatch(
                        label = word,
                        color = LabPalette[index],
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Hint(trackLabels.getValue(trackKind))
    }
}

// ---------------------------------------------------------------------------
// 2. スパン (bento レイアウト)
// ---------------------------------------------------------------------------

@Composable
private fun GridSpanCard() {
    DemoCard(
        title = "2. rowSpan / columnSpan — bento レイアウト",
        description = "1 つの子に複数セルをまたがせる。" +
            "これまで Row と Column を入れ子にして作っていた非対称なダッシュボードが、" +
            "1 枚の Grid でフラットに書ける。",
        code = """
            Grid(
                config = {
                    repeat(3) { column(1.fr) }
                    rows(Fixed(72.dp), Fixed(72.dp), Fixed(72.dp))
                    gap(8.dp)
                },
            ) {
                Card(Modifier.gridItem(row = 1, column = 1, rowSpan = 2))        // 縦長
                Card(Modifier.gridItem(row = 1, column = 2, columnSpan = 2))     // 横長
                Card(Modifier.gridItem(row = 2, column = 2))
                Card(Modifier.gridItem(row = 2, column = 3))
                Card(Modifier.gridItem(rows = 3..3, columns = 1..3))             // IntRange でも書ける
            }
        """.trimIndent(),
    ) {
        DemoStage {
            Grid(
                config = {
                    repeat(3) { column(1.fr) }
                    rows(
                        GridTrackSize.Fixed(72.dp),
                        GridTrackSize.Fixed(72.dp),
                        GridTrackSize.Fixed(72.dp),
                    )
                    gap(8.dp)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Swatch(
                    "売上グラフ\n(2 行ぶん)",
                    LabPalette.Indigo,
                    Modifier
                        .gridItem(row = 1, column = 1, rowSpan = 2)
                        .fillMaxSize(),
                    fontSize = 12,
                )
                Swatch(
                    "お知らせ (2 列ぶん)",
                    LabPalette.Teal,
                    Modifier
                        .gridItem(row = 1, column = 2, columnSpan = 2)
                        .fillMaxSize(),
                    fontSize = 12,
                )
                Swatch(
                    "新規",
                    LabPalette.Amber,
                    Modifier.gridItem(row = 2, column = 2).fillMaxSize(),
                    fontSize = 12,
                )
                Swatch(
                    "解約",
                    LabPalette.Rose,
                    Modifier.gridItem(row = 2, column = 3).fillMaxSize(),
                    fontSize = 12,
                )
                Swatch(
                    "フッター (rows = 3..3, columns = 1..3)",
                    LabPalette.Slate,
                    Modifier.gridItem(rows = 3..3, columns = 1..3).fillMaxSize(),
                    fontSize = 12,
                )
            }
        }
        Hint("gridItem には alignment も渡せる。既定は Alignment.TopStart なので、セルを埋めたいときは fillMaxSize() を添える。")
    }
}

// ---------------------------------------------------------------------------
// 3. 自動配置と flow
// ---------------------------------------------------------------------------

private val flowOptions = listOf(
    "GridFlow.Row (既定)" to GridFlow.Row,
    "GridFlow.Column" to GridFlow.Column,
)

@Composable
private fun GridFlowCard() {
    var flowValue by remember { mutableStateOf(GridFlow.Row) }

    DemoCard(
        title = "3. 自動配置と flow",
        description = "gridItem を付けない子は空きセルへ順に流れる。" +
            "flow を Column にすると、埋まる方向が縦優先に変わる。",
        code = """
            Grid(
                config = {
                    repeat(3) { column(1.fr) }
                    rows(Fixed(48.dp), Fixed(48.dp))
                    flow = ${if (flowValue == GridFlow.Column) "GridFlow.Column" else "GridFlow.Row"}
                    gap(6.dp)
                },
            ) {
                repeat(6) { Cell("${'$'}it") }
            }
        """.trimIndent(),
    ) {
        ChoiceRow("flow", flowOptions, flowValue) { flowValue = it }
        DemoStage {
            Grid(
                config = {
                    repeat(3) { column(1.fr) }
                    rows(GridTrackSize.Fixed(48.dp), GridTrackSize.Fixed(48.dp))
                    flow = flowValue
                    gap(6.dp)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(6) { index ->
                    Swatch(
                        label = (index + 1).toString(),
                        color = LabPalette[index],
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. 名前付きエリア + レスポンシブ
// ---------------------------------------------------------------------------

@Composable
private fun GridNamedAreaCard() {
    var width by rememberSaveable { mutableFloatStateOf(360f) }

    DemoCard(
        title = "4. 名前付きエリア — 画面骨格をレスポンシブに組み替える",
        description = "config の中で area(\"header\", ...) と座標を結び付けておけば、" +
            "子は Modifier.gridItem(\"header\") と意味だけを書けばよい。" +
            "座標は config 側 (= measure 時に constraints を見られる場所) にあるので、" +
            "子を一切書き換えずに骨格だけ差し替えられる。",
        code = """
            Grid(
                config = {
                    val wide = constraints.maxWidth >= 520.dp.roundToPx()
                    if (wide) {
                        column(140.dp); column(1.fr)
                        row(52.dp); row(minmax(120.dp, 1.fr)); row(44.dp)
                        area("header",  row = 1, column = 1, columnSpan = 2)
                        area("nav",     row = 2, column = 1)
                        area("content", row = 2, column = 2)
                        area("footer",  row = 3, column = 1, columnSpan = 2)
                    } else {
                        column(1.fr)
                        row(52.dp); row(44.dp); row(minmax(120.dp, 1.fr)); row(44.dp)
                        area("header",  row = 1, column = 1)
                        area("nav",     row = 2, column = 1)
                        area("content", row = 3, column = 1)
                        area("footer",  row = 4, column = 1)
                    }
                    gap(8.dp)
                },
            ) {
                // 子はどの座標かを知らない
                Header(Modifier.gridItem("header"))
                Nav(Modifier.gridItem("nav"))
                Content(Modifier.gridItem("content"))
                Footer(Modifier.gridItem("footer"))
            }
        """.trimIndent(),
    ) {
        BreakpointBadge(widthDp = width, compactBelow = 520, mediumBelow = 720)
        ResizableStage(widthDp = width, onWidthChange = { width = it }) {
            Grid(
                config = {
                    val wide = constraints.maxWidth >= 520.dp.roundToPx()
                    if (wide) {
                        column(140.dp)
                        column(1.fr)
                        row(52.dp)
                        row(minmax(120.dp, 1.fr))
                        row(44.dp)
                        area("header", row = 1, column = 1, columnSpan = 2)
                        area("nav", row = 2, column = 1)
                        area("content", row = 2, column = 2)
                        area("footer", row = 3, column = 1, columnSpan = 2)
                    } else {
                        column(1.fr)
                        row(52.dp)
                        row(44.dp)
                        row(minmax(120.dp, 1.fr))
                        row(44.dp)
                        area("header", row = 1, column = 1)
                        area("nav", row = 2, column = 1)
                        area("content", row = 3, column = 1)
                        area("footer", row = 4, column = 1)
                    }
                    gap(8.dp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            ) {
                Swatch("header", LabPalette.Indigo, Modifier.gridItem("header").fillMaxSize(), 12)
                Swatch("nav", LabPalette.Teal, Modifier.gridItem("nav").fillMaxSize(), 12)
                Swatch("content", LabPalette.Sky, Modifier.gridItem("content").fillMaxSize(), 12)
                Swatch("footer", LabPalette.Slate, Modifier.gridItem("footer").fillMaxSize(), 12)
            }
        }
        Hint(
            "1.fr のトラックは子の intrinsic サイズを問い合わせる。" +
                "LazyColumn のような SubcomposeLayout を直接置くときは minmax(0.dp, 1.fr) を使うこと。"
        )
    }
}
