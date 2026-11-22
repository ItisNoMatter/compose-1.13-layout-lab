package com.example.composelayoutlab.flexgridlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexAlignContent
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexAlignSelf
import androidx.compose.foundation.layout.FlexBasis
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexConfig
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Compose 1.13 で stable になった FlexBox のデモ画面。
 *
 * FlexBox は Row / Column / FlowRow / FlowColumn の上位互換で、
 * 「方向・折り返し・主軸の配置・交差軸の配置・行間の配置」を
 * 1 つの `config` ブロックにまとめて書ける。
 */
@Composable
fun FlexBoxLab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LabIntro(
            title = "FlexBox",
            body = "1 つの Composable で Row / Column / FlowRow / FlowColumn を表現できる。" +
                "子は grow / shrink / basis で余白の取り合いを宣言し、" +
                "親は config で方向・折り返し・配置をまとめて決める。",
        )
        FlexPlaygroundCard()
        FlexGrowShrinkCard()
        FlexBasisCard()
        FlexOrderCard()
        FlexTagCloudCard()
        FlexResponsiveToolbarCard()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun LabIntro(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// 1. プレイグラウンド: config の各項目を触って結果を見る
// ---------------------------------------------------------------------------

private val directions = listOf(
    "Row" to FlexDirection.Row,
    "Column" to FlexDirection.Column,
    "RowReverse" to FlexDirection.RowReverse,
    "ColumnReverse" to FlexDirection.ColumnReverse,
)

private val wraps = listOf(
    "NoWrap" to FlexWrap.NoWrap,
    "Wrap" to FlexWrap.Wrap,
    "WrapReverse" to FlexWrap.WrapReverse,
)

private val justifies = listOf(
    "Start" to FlexJustifyContent.Start,
    "Center" to FlexJustifyContent.Center,
    "End" to FlexJustifyContent.End,
    "SpaceBetween" to FlexJustifyContent.SpaceBetween,
    "SpaceAround" to FlexJustifyContent.SpaceAround,
    "SpaceEvenly" to FlexJustifyContent.SpaceEvenly,
)

private val alignItemsOptions = listOf(
    "Start" to FlexAlignItems.Start,
    "Center" to FlexAlignItems.Center,
    "End" to FlexAlignItems.End,
    "Stretch" to FlexAlignItems.Stretch,
    "Baseline" to FlexAlignItems.Baseline,
)

private val alignContents = listOf(
    "Start" to FlexAlignContent.Start,
    "Center" to FlexAlignContent.Center,
    "End" to FlexAlignContent.End,
    "Stretch" to FlexAlignContent.Stretch,
    "SpaceBetween" to FlexAlignContent.SpaceBetween,
    "SpaceAround" to FlexAlignContent.SpaceAround,
)

private fun <T> nameOf(options: List<Pair<String, T>>, value: T): String =
    options.firstOrNull { it.second == value }?.first ?: value.toString()

/** 高さがバラバラの方が交差軸の配置の違いが分かりやすいので、文字サイズを変えてある。 */
private val playgroundItems = listOf(
    Triple("1", 64.dp, 12),
    Triple("2", 88.dp, 26),
    Triple("3", 56.dp, 11),
    Triple("4", 104.dp, 18),
    Triple("5", 72.dp, 34),
    Triple("6", 60.dp, 13),
    Triple("7", 92.dp, 22),
)

@Composable
private fun FlexPlaygroundCard() {
    var direction by remember { mutableStateOf(FlexDirection.Row) }
    var wrap by remember { mutableStateOf(FlexWrap.Wrap) }
    var justify by remember { mutableStateOf(FlexJustifyContent.Start) }
    var alignItemsValue by remember { mutableStateOf(FlexAlignItems.Start) }
    var alignContentValue by remember { mutableStateOf(FlexAlignContent.Start) }
    var gapDp by rememberSaveable { mutableFloatStateOf(8f) }
    var maxPerLine by rememberSaveable { mutableIntStateOf(0) }
    var itemCount by rememberSaveable { mutableIntStateOf(7) }

    val maxPerLineText = if (maxPerLine == 0) "制限なし" else maxPerLine.toString()
    val code = buildString {
        appendLine("FlexBox(")
        appendLine("    modifier = Modifier.fillMaxWidth().height(240.dp),")
        appendLine("    config = {")
        appendLine("        direction(FlexDirection." + nameOf(directions, direction) + ")")
        appendLine("        wrap(FlexWrap." + nameOf(wraps, wrap) + ")")
        appendLine("        justifyContent(FlexJustifyContent." + nameOf(justifies, justify) + ")")
        appendLine("        alignItems(FlexAlignItems." + nameOf(alignItemsOptions, alignItemsValue) + ")")
        appendLine("        alignContent(FlexAlignContent." + nameOf(alignContents, alignContentValue) + ")")
        appendLine("        gap(" + gapDp.roundToInt() + ".dp)")
        if (maxPerLine > 0) appendLine("        maxItemsInEachLine(" + maxPerLine + ")")
        appendLine("    },")
        appendLine(") {")
        appendLine("    repeat(" + itemCount + ") { Swatch(label = \"\$it\") }")
        append("}")
    }

    DemoCard(
        title = "1. config プレイグラウンド",
        description = "FlexBox の config はすべて関数呼び出し。チップを切り替えると、" +
            "そのまま下のコードが生成される。",
        code = code,
    ) {
        DemoStage {
            FlexBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                config = {
                    direction(direction)
                    wrap(wrap)
                    justifyContent(justify)
                    alignItems(alignItemsValue)
                    alignContent(alignContentValue)
                    gap(gapDp.dp)
                    if (maxPerLine > 0) maxItemsInEachLine(maxPerLine)
                },
            ) {
                playgroundItems.take(itemCount).forEachIndexed { index, (label, width, font) ->
                    Swatch(
                        label = label,
                        color = LabPalette[index],
                        fontSize = font,
                        modifier = Modifier.width(width),
                    )
                }
            }
        }

        ChoiceRow("direction", directions, direction) { direction = it }
        ChoiceRow("wrap", wraps, wrap) { wrap = it }
        ChoiceRow("justifyContent (主軸)", justifies, justify) { justify = it }
        ChoiceRow("alignItems (交差軸・行内)", alignItemsOptions, alignItemsValue) {
            alignItemsValue = it
        }
        ChoiceRow("alignContent (交差軸・行同士)", alignContents, alignContentValue) {
            alignContentValue = it
        }
        LabeledSlider(
            label = "gap",
            value = gapDp,
            valueRange = 0f..32f,
            steps = 7,
            onValueChange = { gapDp = it },
            valueLabel = gapDp.roundToInt().toString() + "dp",
        )
        LabeledSlider(
            label = "maxItemsInEachLine",
            value = maxPerLine.toFloat(),
            valueRange = 0f..5f,
            steps = 4,
            onValueChange = { maxPerLine = it.roundToInt() },
            valueLabel = maxPerLineText,
        )
        LabeledSlider(
            label = "アイテム数",
            value = itemCount.toFloat(),
            valueRange = 1f..7f,
            steps = 5,
            onValueChange = { itemCount = it.roundToInt() },
            valueLabel = itemCount.toString(),
        )
        Hint(
            "alignContent は折り返しが起きているときだけ効く。" +
                "wrap を NoWrap にして幅が足りない場合は、shrink を持つ子が縮む。"
        )
    }
}

// ---------------------------------------------------------------------------
// 2. grow / shrink
// ---------------------------------------------------------------------------

@Composable
private fun FlexGrowShrinkCard() {
    var growA by rememberSaveable { mutableFloatStateOf(1f) }
    var growB by rememberSaveable { mutableFloatStateOf(2f) }
    var growC by rememberSaveable { mutableFloatStateOf(0f) }
    var shrinkEnabled by rememberSaveable { mutableStateOf(true) }

    val total = growA + growB + growC
    val share = { g: Float -> if (total == 0f) "-" else ((g / total) * 100).roundToInt().toString() + "%" }

    DemoCard(
        title = "2. grow — 余った幅の分配",
        description = "grow は「余白をどの比率で奪うか」。0 なら伸びない。" +
            "合計に対する比率でそのまま配られるので、重み付きの Row をそのまま表現できる。",
        code = """
            FlexBox(modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.flex { grow(${growA.roundToInt()}f) })  // ${share(growA)}
                Box(Modifier.flex { grow(${growB.roundToInt()}f) })  // ${share(growB)}
                Box(Modifier.flex { grow(${growC.roundToInt()}f) })  // ${share(growC)}
            }
        """.trimIndent(),
    ) {
        DemoStage {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    alignItems(FlexAlignItems.Stretch)
                    gap(6.dp)
                },
            ) {
                Swatch(
                    label = "grow " + growA.roundToInt() + "\n" + share(growA),
                    color = LabPalette.Indigo,
                    modifier = Modifier
                        .width(48.dp)
                        .flex { grow(growA) },
                )
                Swatch(
                    label = "grow " + growB.roundToInt() + "\n" + share(growB),
                    color = LabPalette.Teal,
                    modifier = Modifier
                        .width(48.dp)
                        .flex { grow(growB) },
                )
                Swatch(
                    label = "grow " + growC.roundToInt() + "\n" + share(growC),
                    color = LabPalette.Amber,
                    modifier = Modifier
                        .width(48.dp)
                        .flex { grow(growC) },
                )
            }
        }
        LabeledSlider("A の grow", growA, 0f..4f, { growA = it }, steps = 3)
        LabeledSlider("B の grow", growB, 0f..4f, { growB = it }, steps = 3)
        LabeledSlider("C の grow", growC, 0f..4f, { growC = it }, steps = 3)

        Text(
            "shrink — 入りきらないときに誰が縮むか",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            "幅 220dp の箱に 120dp の子を 3 つ入れている。shrink(0f) の子だけが元の幅を守る。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = shrinkEnabled, onCheckedChange = { shrinkEnabled = it })
            Spacer(Modifier.width(8.dp))
            Text(
                if (shrinkEnabled) "左端だけ shrink(0f)" else "全員 shrink(1f) (既定)",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        DemoStage {
            FlexBox(
                modifier = Modifier.width(220.dp),
                config = { gap(6.dp) },
            ) {
                Swatch(
                    label = if (shrinkEnabled) "固定" else "可変",
                    color = LabPalette.Rose,
                    modifier = Modifier
                        .width(120.dp)
                        .flex { shrink(if (shrinkEnabled) 0f else 1f) },
                )
                Swatch("可変", LabPalette.Violet, Modifier.width(120.dp))
                Swatch("可変", LabPalette.Sky, Modifier.width(120.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. basis
// ---------------------------------------------------------------------------

private val basisOptions = listOf(
    "Auto" to 0,
    "Dp(120.dp)" to 1,
    "Percent(0.5f)" to 2,
)

@Composable
private fun FlexBasisCard() {
    var mode by rememberSaveable { mutableIntStateOf(0) }

    val basisConfig = remember(mode) {
        when (mode) {
            1 -> FlexConfig { basis(FlexBasis.Dp(120.dp)) }
            2 -> FlexConfig { basis(FlexBasis.Percent(0.5f)) }
            else -> FlexConfig { basis(FlexBasis.Auto) }
        }
    }

    DemoCard(
        title = "3. basis — 分配前の出発点",
        description = "grow / shrink の計算に入る前の「主軸方向の初期サイズ」。" +
            "Auto なら子の実寸、Dp なら固定値、Percent なら親に対する割合から始まる。",
        code = """
            val Half = FlexConfig { basis(FlexBasis.Percent(0.5f)) }

            FlexBox(modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.flex(Half))           // 親の 50% から開始
                Box(Modifier.flex { basis(120.dp) })  // 120dp から開始
            }
        """.trimIndent(),
    ) {
        ChoiceRow("左ブロックの basis", basisOptions, mode) { mode = it }
        DemoStage {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    alignItems(FlexAlignItems.Stretch)
                    gap(6.dp)
                },
            ) {
                Swatch(
                    label = nameOf(basisOptions, mode),
                    color = LabPalette.Indigo,
                    modifier = Modifier
                        .width(60.dp)
                        .flex(basisConfig),
                )
                Swatch(
                    label = "grow(1f)",
                    color = LabPalette.Slate,
                    modifier = Modifier.flex { grow(1f) },
                )
            }
        }
        Hint("FlexConfig / FlexBoxConfig は値として持ち回せる。デザインシステムのトークンとして共有し、then で合成できる。")
    }
}

// ---------------------------------------------------------------------------
// 4. order
// ---------------------------------------------------------------------------

@Composable
private fun FlexOrderCard() {
    var reorder by rememberSaveable { mutableStateOf(false) }

    DemoCard(
        title = "4. order — 宣言順と表示順を切り離す",
        description = "読み上げ順 (コンポジションの順序) を保ったまま、見た目の順序だけ入れ替えられる。" +
            "画面幅でカードの並びだけ変えたい、といった要求に強い。",
        code = """
            FlexBox {
                Box(Modifier.flex { order(if (compact) 1 else 0) })   // 本文
                Box(Modifier.flex { order(if (compact) -1 else 0) })  // サマリ
            }
        """.trimIndent(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = reorder, onCheckedChange = { reorder = it })
            Spacer(Modifier.width(8.dp))
            Text(
                if (reorder) "order を適用中" else "宣言順のまま",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        DemoStage {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    wrap(FlexWrap.Wrap)
                    gap(6.dp)
                },
            ) {
                val orders = if (reorder) listOf(2, 0, -1, 1) else listOf(0, 0, 0, 0)
                listOf("宣言 1", "宣言 2", "宣言 3", "宣言 4").forEachIndexed { index, label ->
                    Swatch(
                        label = label + "\norder=" + orders[index],
                        color = LabPalette[index],
                        fontSize = 11,
                        modifier = Modifier
                            .width(80.dp)
                            .flex { order(orders[index]) },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. 実用例: タグクラウド
// ---------------------------------------------------------------------------

private val sampleTags = listOf(
    "Compose", "Kotlin", "FlexBox", "Grid", "Material 3", "Adaptive",
    "Layout", "Modifier", "Preview", "Multiplatform",
)

@Composable
private fun FlexTagCloudCard() {
    var limitPerLine by rememberSaveable { mutableStateOf(false) }

    DemoCard(
        title = "5. 実用例: タグクラウド",
        description = "幅に応じて折り返すチップ列。行間と列間を別々に指定できるので、" +
            "詰まって見えがちなタグ列も gap(row, column) で整う。",
        code = """
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    wrap(FlexWrap.Wrap)
                    gap(row = 8.dp, column = 6.dp)
                    ${if (limitPerLine) "maxItemsInEachLine(3)" else "// maxItemsInEachLine(3) で 1 行の個数も固定できる"}
                },
            ) {
                tags.forEach { Tag(it) }
            }
        """.trimIndent(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = limitPerLine, onCheckedChange = { limitPerLine = it })
            Spacer(Modifier.width(8.dp))
            Text("maxItemsInEachLine(3)", style = MaterialTheme.typography.bodySmall)
        }
        DemoStage {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    wrap(FlexWrap.Wrap)
                    alignItems(FlexAlignItems.Center)
                    gap(row = 8.dp, column = 6.dp)
                    if (limitPerLine) maxItemsInEachLine(3)
                },
            ) {
                sampleTags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(16.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            "#" + tag,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 6. 実用例: レスポンシブなツールバー
// ---------------------------------------------------------------------------

/** 再利用できる設定値。トップレベルの定数として置けるのが FlexBoxConfig の強み。 */
private val ToolbarBase = FlexBoxConfig {
    alignItems(FlexAlignItems.Center)
    gap(8.dp)
}

@Composable
private fun FlexResponsiveToolbarCard() {
    var width by rememberSaveable { mutableFloatStateOf(360f) }

    DemoCard(
        title = "6. 実用例: 幅で組み替わるツールバー",
        description = "config は measure 時に走り、constraints を直接読める。" +
            "BoxWithConstraints でサブコンポジションを挟まなくても、" +
            "同じ Composable のまま横並び / 縦積みを切り替えられる。",
        code = """
            val ToolbarBase = FlexBoxConfig {
                alignItems(FlexAlignItems.Center)
                gap(8.dp)
            }

            FlexBox(
                config = ToolbarBase then FlexBoxConfig {
                    // measure 時に実際の制約を見て決める
                    direction(
                        if (constraints.maxWidth < 420.dp.roundToPx()) FlexDirection.Column
                        else FlexDirection.Row
                    )
                    wrap(FlexWrap.Wrap)
                },
            ) {
                Text("受注一覧")
                SearchField(Modifier.flex { grow(1f); basis(160.dp) })  // 余白を独り占め
                Button(...)
                OutlinedButton(...)
            }
        """.trimIndent(),
    ) {
        BreakpointBadge(widthDp = width, compactBelow = 420, mediumBelow = 640)
        ResizableStage(widthDp = width, onWidthChange = { width = it }) {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = ToolbarBase then FlexBoxConfig {
                    direction(
                        if (constraints.maxWidth < 420.dp.roundToPx()) FlexDirection.Column
                        else FlexDirection.Row
                    )
                    wrap(FlexWrap.Wrap)
                    alignItems(
                        if (constraints.maxWidth < 420.dp.roundToPx()) FlexAlignItems.Stretch
                        else FlexAlignItems.Center
                    )
                },
            ) {
                Text(
                    "受注一覧",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.flex { alignSelf(FlexAlignSelf.Center) },
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(20.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .flex {
                            grow(1f)
                            basis(160.dp)
                        },
                ) {
                    Text(
                        "検索…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = {}) { Text("絞り込み", fontSize = 13.sp) }
                Button(onClick = {}) { Text("新規作成", fontSize = 13.sp) }
            }
        }
        Hint("幅を 420dp より狭くすると縦積みに切り替わり、検索欄は grow(1f) のまま全幅に伸びる。")
    }
}

@Composable
internal fun Hint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = "💡 " + text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
