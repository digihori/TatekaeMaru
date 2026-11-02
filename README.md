# たてかえ丸（Tatekaemaru）

仲間内のイベントやレジャーで発生した立替払いを、簡単に精算するためのAndroid／iOSアプリです。
Android版はGoogle Playで公開済みです。iOS版は実機での動作確認を進めています。

Kotlin Multiplatformでデータモデル・精算ロジック・編集処理を共有し、Compose Multiplatformでホーム・イベント編集・精算結果などの画面を共有しています。

## 主な機能

- イベントの作成・名前編集・保存・再読込・削除
- 参加メンバーの追加・名前変更・削除
- 支払項目の追加・修正・削除
- 支払者と、費用を負担するメンバーの指定
- メンバーごとの支払額・負担額・差額と、精算先・精算額の表示
- 走行距離・燃費・ガソリン単価からのガス代計算
- メンバー名・支払項目名の入力履歴候補
- 精算結果・支払い明細の他アプリへの共有
- 端末内へのデータ保存
- アプリ内でのプライバシーポリシー表示

ログインや開発者サーバーとの同期は不要です。初回起動時は、使い方を確認できるサンプルイベントを作成します。

## 対応プラットフォーム

| 項目 | Android | iOS |
| --- | --- | --- |
| 最低対応バージョン | Android 8.0（API 26） | iOS 16.0 |
| 対象 | Android端末 | iPhone |
| 保存方式 | Room／SQLite | JSONファイル |
| 設定保存 | SharedPreferences | NSUserDefaults |
| 共有 | 標準共有Intent、LINE指定共有 | 標準共有シート |
| ホーム画面のメニュー | 縦3点 | 横3点 |

iOSのビルドターゲットは実機用の`iosArm64`と、Apple Silicon MacのSimulator用の`iosSimulatorArm64`です。
Android／iOS間のデータ移行・同期機能はありません。

## 保存と精算

支払項目の確定、メンバーの追加・変更・削除、イベント編集画面から戻る操作などでイベントを保存します。
「精算結果」を開くときにも保存します。保存済みイベントは、アプリを終了した後もホーム画面から再度開けます。
未確定の支払入力フォームと、プロセス終了時の編集中データの復元は、追加の対応・確認が必要です。

保存先は次のとおりです。

- Android：端末内の`tatekae_maru.db`（Room）。既存のDB構造・保存形式を維持しています。
- iOS：アプリのApplication Support内の`events-v1.json`。ファイルを原子的に置き換え、保存成功後に一覧を更新します。破損データや未対応の形式を空データとして上書きしません。

金額は円単位で扱います。同じ負担対象者の支払いを合算してから割り、端数はメンバーIDのソート順に配分します。
メンバーを削除すると、そのメンバーが支払った項目や、負担対象者がゼロになる項目も削除されるため、確認画面を表示します。

### ガス代計算

支払項目の入力時に「ガス代計算を使う」を選び、以下の値を入力します。

- 走行距離（km）
- 燃費（km/L）
- ガソリン単価（円/L）

```text
ガス代 = 走行距離 ÷ 燃費 × ガソリン単価
```

計算結果の小数部分は切り捨てます。計算に使った値も両OSで保存し、再編集時に復元します。

## 他アプリへの共有

精算結果画面から、精算先・精算額・各自負担額・支払い明細・ガス代情報をテキストで共有できます。

- Android：「LINE」でLINEへ直接連携し、「共有」でほかの対応アプリを選択します。LINE未インストール時は通常の共有先選択に切り替わります。
- iOS：「共有」で標準共有シートを開き、LINE・メモ・メッセージなど、端末にインストールされている対応アプリを選びます。

共有テキストの生成は共通で、共有先アプリを開く処理はOSごとに実装しています。
iOS実機ではメモ・メッセージへの連携を確認済みです。LINEへの実際の受け渡しは未確認です。

## プライバシーポリシー

両OSとも、ホーム画面右上のメニューからアプリ内に表示します。表示に通信は不要です。

更新元はリポジトリ直下の[PRIVACY.ja.md](PRIVACY.ja.md)です。
ビルド時に`:shared:generatePrivacyPolicy`が共通Kotlinコードを生成し、両アプリに埋め込みます。
ポリシーを更新した場合は、アプリの再ビルド・配布が必要です。

## プロジェクト構成

| ディレクトリ | 役割 |
| --- | --- |
| `shared/src/commonMain` | モデル、精算計算、イベント編集、保存・設定のインターフェース、共有テキスト、共通画面 |
| `shared/src/commonTest` | 精算・編集・初回設定・JSON保存・共有テキストの共通テスト |
| `shared/src/androidMain` | Android向けのID生成・戻る操作・メニューアイコン |
| `shared/src/iosMain` | iOS向けの保存・設定・ID生成・共有シート、共通画面を表示するViewController |
| `app` | Androidアプリの起動・画面遷移、Room保存、設定、共有Intent |
| `iosApp` | Xcodeプロジェクト、SwiftUIの起動部分、アプリアイコン |

`EventRepository`と`AppSettings`が保存・設定の境界です。Androidは`RoomEventRepository`と`AndroidAppSettings`、iOSは`JsonEventRepository`・`IosEventDocumentStorage`と`IosAppSettings`を使用します。

イベントの変更は共通の`EventEditState`、入力の解釈・検証とガス代計算は`ExpenseInput`が担当します。
画面のダイアログや入力中の表示状態はComposeで管理します。iOSではSwiftUIから共通のCompose画面を表示します。

## 開発環境

共通のビルドにJDK 21とAndroid SDKを使用します。Android SDKの場所は、`local.properties`の`sdk.dir`または環境変数で設定してください。

- Android開発：Android Studio
- iOS開発：macOS、Xcode、iOS Simulatorランタイム。実機では開発チームと署名の設定が必要です。
- 共通技術：Kotlin、Kotlin Multiplatform、Compose Multiplatform、Coroutines、kotlinx.serialization、kotlinx.datetime
- Android固有技術：Jetpack Navigation、Room、KSP

ライブラリとプラグインのバージョンは`gradle/libs.versions.toml`、Gradle本体のバージョンは`gradle/wrapper/gradle-wrapper.properties`で管理しています。
以下のGradleコマンドは、リポジトリのルートでJDK 21を選択して実行してください。

### Androidの起動・ビルド

1. Android Studioでリポジトリのルートを開きます。
2. Gradleを同期し、`app`と実機またはエミュレーターを選びます。
3. Runで起動します。

```sh
./gradlew :app:assembleDebug
```

デバッグ版のアプリIDには`.debug`、バージョン名には`-debug`が付きます。

### iOSの起動・ビルド

1. Xcodeで`iosApp/TatekaeMaru.xcodeproj`を開きます。
2. `TatekaeMaru`スキームとiPhone実機またはApple SiliconのiPhone Simulatorを選びます。
3. 実機の場合はSigning & Capabilitiesで自身の開発チーム・署名設定を確認します。
4. Runで起動します。

Xcodeのビルドフェーズが`:shared:embedAndSignAppleFrameworkForXcode`を実行します。
JDKは`/usr/libexec/java_home -v 21`で選択し、Gradleキャッシュは`.gradle-local`を使用します。初回のフレームワークのリンクには時間がかかります。

署名なしでSimulator向けにビルドする場合：

```sh
xcodebuild -project iosApp/TatekaeMaru.xcodeproj -scheme TatekaeMaru \
  -configuration Debug -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath iosApp/build CODE_SIGNING_ALLOWED=NO build
```

## テスト

共通テストをAndroidのJVM上で実行し、Android固有の保存変換テストも確認します。

```sh
./gradlew :shared:testDebugUnitTest :app:testDebugUnitTest
```

iOS実機・Simulator向けの共通コードのコンパイル確認：

```sh
./gradlew :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64
```

これらはiPhone上のUI操作テストではありません。共有先アプリへの受け渡しや日本語入力、中断復帰は実機でも確認してください。

## バージョン管理とリリース

両OSとも、ルートの[Version.xcconfig](Version.xcconfig)を参照します。現在のバージョンは**1.1.3（5）**です。

| 設定 | Android | iOS |
| --- | --- | --- |
| `APP_VERSION` | `versionName` | `CFBundleShortVersionString` |
| `APP_BUILD` | `versionCode` | `CFBundleVersion` |

両OSで同じアイコン画像を使用しています。iOS用のサイズ別画像は`iosApp/TatekaeMaru/Assets.xcassets/AppIcon.appiconset`に配置しています。

### Androidリリース

`release`ではR8によるコード最適化・難読化と不要リソースの削減を有効にしています。
標準の`proguard-android-optimize.txt`と依存ライブラリのconsumer rulesを使用します。

```sh
./gradlew :app:assembleRelease
```

難読化マッピングは`app/build/outputs/mapping/release/mapping.txt`に出力されます。
現在のGradle設定ではリリース署名を指定していないため、APKは未署名です。配布には署名を設定し、最適化したビルドで保存・精算・共有を確認してください。

### iOSリリース

Xcodeで実機向けの署名設定を確認し、Product → Archiveから配布用アーカイブを作成します。
TestFlight／App Storeへの配布前に、ストア用スクリーンショット・プライバシー情報などを整備してください。

## 動作確認状況

- AndroidのデバッグビルドとR8適用後のリリースビルドが成功。
- 共通テスト21件とAndroidの保存変換テスト1件が成功。
- iOS実機・Simulator向けKotlinコンパイルと、Simulator向けXcodeビルドが成功。
- iOS Simulatorでサンプル読込・精算表示・標準共有シート・保存後の再起動復元・ポリシー表示・アイコン・横3点メニューを確認。
- iPhone実機で一通りの操作とメモ・メッセージへの共有を確認済み。

長い明細の共有、未確定入力の中断復帰、R8適用後のAndroidアプリの実機操作は、追加の確認対象です。
