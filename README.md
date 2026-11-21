# iNomaSandbox — Compose 1.13 Layout Lab

Compose 1.13 (Compose BOM `2026.11.01` / `androidx.compose.foundation:foundation:1.13.0`) で
**stable になった `FlexBox` と `Grid`** を、実機で触りながら理解するためのサンドボックスアプリ。

アプリを起動すると 3 つのタブが出る。

| タブ | 内容 |
| --- | --- |
| **FlexBox** | `config` の各項目 (direction / wrap / justifyContent / alignItems / alignContent / gap / maxItemsInEachLine) をチップとスライダーで切り替え、結果とコードが同時に変わるプレイグラウンド。続けて grow・shrink・basis・order の単体デモと、タグクラウド / レスポンシブツールバーの実用例。 |
| **Grid** | トラックサイズ (`1.fr` / `Fixed` / `Auto` / `Percentage`)、`rowSpan` / `columnSpan` による bento レイアウト、`GridFlow` による自動配置、`area()` を使った名前付きエリアのレスポンシブ骨格。 |
| **組み合わせ** | Grid が画面骨格を、FlexBox が行内の伸縮を担当する受注ダッシュボード。スライダーで幅を変えると 1 列 / 2 列 / 4 列に組み替わる。 |

各デモカードには「コードを見る」ボタンがあり、その場の設定に対応する実コードが読める。

## この 2 つが stable になって何が嬉しいか

### 1. 分岐が `config` の 1 か所に閉じる

`FlexBox` / `Grid` の `config` ブロックは **measure 時**に実行され、その中から
`constraints` を直接読める。つまり `BoxWithConstraints` でサブコンポジションを挟まなくても、
画面幅に応じてレイアウトを組み替えられる。

```kotlin
Grid(
    config = {
        val columnCount = when {
            constraints.maxWidth < 420.dp.roundToPx() -> 1
            constraints.maxWidth < 720.dp.roundToPx() -> 2
            else -> 4
        }
        repeat(columnCount) { column(1.fr) }
        area("header", row = 1, column = 1, columnSpan = columnCount)
    },
) {
    DashboardHeader(Modifier.gridItem("header"))  // 子は座標を知らない
}
```

### 2. 入れ子が減る

これまで `Row` / `Column` / `Box` / `weight` を何段も重ねて表現していた非対称な画面が、
`Grid` のトラック定義 + `gridItem(row, column, rowSpan, columnSpan)` でフラットに書ける。

### 3. レイアウト設定を値として配れる

`FlexBoxConfig` / `FlexConfig` は値なので、トップレベル定数やデザインシステムのトークンとして
持ち回せる。`then` や `FlexBoxConfig(a, b, c)` で合成もできる。

```kotlin
val ToolbarBase = FlexBoxConfig {
    alignItems(FlexAlignItems.Center)
    gap(8.dp)
}

FlexBox(config = ToolbarBase then FlexBoxConfig { wrap(FlexWrap.Wrap) }) { /* ... */ }
```

## ハマりどころ

- `1.fr` のトラックは子の intrinsic サイズを問い合わせる。`LazyColumn` のような
  `SubcomposeLayout` を直接置くとクラッシュするので、`minmax(0.dp, 1.fr)` を使う。
- `Modifier.gridItem(...)` の `alignment` の既定は `Alignment.TopStart`。
  セルいっぱいに広げたいときは `fillMaxSize()` を添える。
- `alignContent` は折り返しが発生しているときだけ効く。
- Compose 1.13 は **compileSdk 37.1** 以上を要求する
  (`compileSdk { version = release(37) { minorApiLevel = 1 } }`)。

## ソース構成

```
app/src/main/java/com/example/inomasandbox/
├── MainActivity.kt            エントリポイント + 各ラボの @Preview
└── FlexBox-Grid-lab/
    ├── LayoutLabApp.kt        タブ付きのアプリシェル
    ├── LabCommon.kt           DemoCard / Swatch / ChoiceRow など共通部品
    ├── FlexBoxLab.kt          FlexBox のデモ 6 本
    ├── GridLab.kt             Grid のデモ 4 本
    └── ResponsiveLab.kt       FlexBox + Grid の組み合わせ実例
```

## ビルド

```
./gradlew :app:assembleDebug
```
