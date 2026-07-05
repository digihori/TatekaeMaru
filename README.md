# たてかえ丸

レジャー時の立替精算を行うAndroidアプリのベースプロジェクトです。

## 現在できること

- ホーム画面表示
- 新規イベント作成
- イベント名編集
- メンバー追加・削除
- 支払項目追加・修正・削除
- 支払者選択
- 立替対象メンバー選択
- 精算結果表示
- Android共有Intentによる明細共有
- Room Databaseによる端末内保存
  - イベント保存
  - 保存済みイベント一覧表示
  - 保存済みイベントの再読込
  - イベント削除

## 開発環境

- Android Studio
- Kotlin
- Jetpack Compose
- Room
- Gradle 8.13
- AGP 8.11.0

## 保存について

イベント編集画面で「保存」を押すと、端末内の `tatekae_maru.db` に保存されます。
アプリを終了してもホーム画面の「保存済みイベント」から再度開けます。

## 2026-07-05 追加: ガス代計算

支払項目入力で「ガス代計算を使う」を選ぶと、以下の値から金額を自動計算します。

- 走行距離 km
- 燃費 km/L
- ガソリン単価 円/L

計算式:

```text
走行距離 ÷ 燃費 × ガソリン単価
```

ガス代計算に使った値はRoomに保存され、再編集時にも復元されます。

## 共有機能

精算結果画面に以下のボタンがあります。

- LINEへ共有
- ほかのアプリへ共有

LINEへ共有は `jp.naver.line.android` を指定した Android 標準共有 Intent を使います。LINE が未インストールの場合は通常の共有先選択にフォールバックします。

## プライバシーポリシー

ホーム画面右上メニューから「プライバシーポリシー」を表示できます。
表示内容は GitHub 上の raw ファイルから取得します。

URL は以下のファイルで変更できます。

```kotlin
app/src/main/java/com/example/tatekaemaru/ui/PrivacyPolicyConfig.kt
```

初期値:

```text
https://raw.githubusercontent.com/digihori/TatekaeMaru/main/PRIVACY_POLICY.md
```

GitHub リポジトリ直下に `PRIVACY_POLICY.md` を置いてください。
