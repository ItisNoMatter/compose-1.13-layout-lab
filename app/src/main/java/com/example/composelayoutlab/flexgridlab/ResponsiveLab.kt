package com.example.composelayoutlab.flexgridlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FlexBox と Grid を組み合わせた、実際の画面に近いサンプル。
 *
 * ポイントは「レイアウトの分岐を 1 か所に閉じ込められる」こと。
 *
 * - 画面の骨格は [Grid] の `config` が持つ。`config` は measure 時に走るので
 *   `constraints` から実際の幅を読んで、列数とエリア定義をその場で組み替えられる。
 * - 子は `Modifier.gridItem("header")` のように *意味* だけを書く。
 *   どの行・どの列に行くか、何セルぶんまたぐかは一切知らない。
 * - 行内の細かい伸縮は [FlexBox] の grow / basis に任せる。
 *
 * 結果として、画面幅ごとに Composable を分岐させたり、
 * BoxWithConstraints でサブコンポジションを挟んだりする必要がなくなる。
 */
@Composable
fun ResponsiveLab(modifier: Modifier = Modifier) {
    var width by rememberSaveable { mutableFloatStateOf(360f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LabIntro(
            title = "組み合わせ: 受注ダッシュボード",
            body = "Grid が画面の骨格を、FlexBox が行内の伸縮を担当する。" +
                "スライダーで幅を変えると、1 列 / 2 列 / 4 列へ段階的に組み替わる。" +
                "切り替えているのは Grid の config だけで、子の Composable は 1 つも分岐していない。",
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BreakpointBadge(widthDp = width, compactBelow = 420, mediumBelow = 720)
            Text(
                "列数: " + columnCountFor(width).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ResizableStage(
            widthDp = width,
            onWidthChange = { width = it },
            range = 280f..900f,
        ) {
            OrderDashboard()
        }

        CodeSnippet(DASHBOARD_CODE)

        Hint(
            "ブレークポイントの判定も、どのエリアが何セルぶんかも、すべて config の中だけに書いてある。" +
                "子を増やしたいときは area() を 1 行足して gridItem(\"...\") を付けるだけ。"
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** 表示上の目安 (実際の判定は Grid の config の中で constraints を見て行う)。 */
private fun columnCountFor(widthDp: Float): Int = when {
    widthDp < 420f -> 1
    widthDp < 720f -> 2
    else -> 4
}

private data class Kpi(val label: String, val value: String, val delta: String)

private val kpis = listOf(
    Kpi("本日の受注", "128", "+12%"),
    Kpi("出荷待ち", "34", "-4%"),
    Kpi("返品", "5", "+1"),
    Kpi("売上", "¥2.4M", "+8%"),
)

private val orders = listOf(
    "#10231  山田商事      ¥120,000",
    "#10232  のま物産      ¥86,400",
    "#10233  サンプル工業  ¥54,000",
    "#10234  テスト建設    ¥310,500",
)

@Composable
private fun OrderDashboard() {
    Grid(
        config = {
            // --- ここが唯一の分岐点 ---
            val w = constraints.maxWidth
            val columnCount = when {
                w < 420.dp.roundToPx() -> 1
                w < 720.dp.roundToPx() -> 2
                else -> 4
            }
            repeat(columnCount) { column(1.fr) }
            gap(10.dp)

            // 1 行目: ヘッダーは常に全幅
            area(AREA_HEADER, row = 1, column = 1, columnSpan = columnCount)

            // 2 行目以降: KPI カードを列数に合わせて流し込む
            val kpiRowCount = (kpis.size + columnCount - 1) / columnCount
            kpis.indices.forEach { index ->
                area(
                    areaId = kpiAreaId(index),
                    row = 2 + index / columnCount,
                    column = 1 + index % columnCount,
                )
            }

            // 最後: 一覧とサイドパネル。1 列のときだけ縦に積む
            val bodyRow = 2 + kpiRowCount
            if (columnCount == 1) {
                area(AREA_MAIN, row = bodyRow, column = 1)
                area(AREA_SIDE, row = bodyRow + 1, column = 1)
            } else {
                area(AREA_MAIN, row = bodyRow, column = 1, columnSpan = columnCount - 1)
                area(AREA_SIDE, row = bodyRow, column = columnCount)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        DashboardHeader(Modifier.gridItem(AREA_HEADER))

        kpis.forEachIndexed { index, kpi ->
            KpiCard(kpi, LabPalette[index], Modifier.gridItem(kpiAreaId(index)).fillMaxSize())
        }

        Panel(title = "受注一覧", modifier = Modifier.gridItem(AREA_MAIN).fillMaxSize()) {
            orders.forEach { order ->
                Text(
                    order,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 3.dp),
                )
            }
        }

        Panel(title = "アクティビティ", modifier = Modifier.gridItem(AREA_SIDE).fillMaxSize()) {
            listOf("09:12 入金確認", "10:40 出荷登録", "13:05 返品受付").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 3.dp),
                )
            }
        }
    }
}

private const val AREA_HEADER = "header"
private const val AREA_MAIN = "main"
private const val AREA_SIDE = "side"

private fun kpiAreaId(index: Int): String = "kpi" + index

/**
 * ヘッダー行。
 *
 * 幅が足りなければ折り返し、余った幅は検索欄が grow で吸収する。
 * ここは 1 次元の伸縮なので Grid ではなく FlexBox の仕事。
 */
@Composable
private fun DashboardHeader(modifier: Modifier = Modifier) {
    FlexBox(
        modifier = modifier.fillMaxWidth(),
        config = {
            wrap(FlexWrap.Wrap)
            alignItems(FlexAlignItems.Center)
            justifyContent(FlexJustifyContent.SpaceBetween)
            gap(row = 8.dp, column = 8.dp)
        },
    ) {
        Text(
            "受注ダッシュボード",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
                .flex {
                    grow(1f)
                    basis(140.dp)
                },
        ) {
            Text(
                "受注番号・取引先で検索",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlexBox(config = { gap(6.dp) }) {
            OutlinedButton(onClick = {}) { Text("CSV", fontSize = 12.sp) }
            Button(onClick = {}) { Text("新規受注", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun KpiCard(
    kpi: Kpi,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                kpi.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                kpi.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(kpi.delta, style = MaterialTheme.typography.labelSmall, color = accent)
        }
    }
}

@Composable
private fun Panel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            content()
        }
    }
}

private val DASHBOARD_CODE = """
    Grid(
        config = {
            // 画面幅の判定は measure 時に 1 回だけ。ここが唯一の分岐点。
            val w = constraints.maxWidth
            val columnCount = when {
                w < 420.dp.roundToPx() -> 1
                w < 720.dp.roundToPx() -> 2
                else -> 4
            }
            repeat(columnCount) { column(1.fr) }
            gap(10.dp)

            area("header", row = 1, column = 1, columnSpan = columnCount)

            val kpiRowCount = (kpis.size + columnCount - 1) / columnCount
            kpis.indices.forEach { i ->
                area("kpi${'$'}i", row = 2 + i / columnCount, column = 1 + i % columnCount)
            }

            val bodyRow = 2 + kpiRowCount
            if (columnCount == 1) {
                area("main", row = bodyRow, column = 1)
                area("side", row = bodyRow + 1, column = 1)
            } else {
                area("main", row = bodyRow, column = 1, columnSpan = columnCount - 1)
                area("side", row = bodyRow, column = columnCount)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        DashboardHeader(Modifier.gridItem("header"))
        kpis.forEachIndexed { i, kpi ->
            KpiCard(kpi, Modifier.gridItem("kpi${'$'}i").fillMaxSize())
        }
        Panel("受注一覧", Modifier.gridItem("main").fillMaxSize()) { ... }
        Panel("アクティビティ", Modifier.gridItem("side").fillMaxSize()) { ... }
    }

    // ヘッダー行の伸縮は FlexBox に任せる
    @Composable
    fun DashboardHeader(modifier: Modifier) {
        FlexBox(
            modifier = modifier.fillMaxWidth(),
            config = {
                wrap(FlexWrap.Wrap)
                alignItems(FlexAlignItems.Center)
                justifyContent(FlexJustifyContent.SpaceBetween)
                gap(row = 8.dp, column = 8.dp)
            },
        ) {
            Text("受注ダッシュボード")
            SearchField(Modifier.flex { grow(1f); basis(140.dp) })  // 余白を吸収
            FlexBox(config = { gap(6.dp) }) { CsvButton(); NewOrderButton() }
        }
    }
""".trimIndent()
