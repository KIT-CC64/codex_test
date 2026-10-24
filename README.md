# ClimaFit — Kotlin / Jetpack Compose の画面モック

**フロントエンドの表面だけ**を作成したAndroid Studioプロジェクトです。Kotlin / Jetpack Composeで実装し、HTML・JavaScript・WebViewは使用しません。

API通信・AI診断・服装の判定ロジック・不快指数の計算・データベース・端末内のデータ保存は実装していません。これらはバックエンド担当者の実装に委ねます。

## Android Studioで開く・更新する

1. `settings.gradle.kts` がある **`codex_test` フォルダー**を開きます。親の `kz_2603` や `Documents` を開く必要はありません。
2. 既にクローン済みなら **Git → Pull** で `main` の更新を取得します。再クローンは不要です。
3. **File → Sync Project with Gradle Files** を実行します。
4. 上部で `app` と端末を選択し、▶ Run を押します。

初回の同期にはネット接続が必要です。JDKエラー時は Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK を Embedded JDK / jbr（JDK 17以上）に設定します。SDK Managerで Android SDK Platform 35 をインストールしてください。

対応：Android Studio Meerkat以降（Otter 3 対応）、Android API 26以上。Gradle 8.11.1 / Android Gradle Plugin 8.9.2 / Kotlin 2.1.20 / Compose BOM 2025.03.01。

## 画面の範囲

- **提案**：場所の選択、室温・湿度・外気温・不快指数・服装提案のサンプル表示。提案内容・指数は固定サンプルで、計算や判定は行いません。
- **コーデAI**：写真の選択・プレビュー・解除。画像は表示のためだけに読み込み、保存・アップロード・診断はしません。
- **記録**：サンプル記録の一覧。「すべて / 現在の室温 / 指定した温度」による画面上の絞り込み。指定温度±2℃に当たるサンプルを表示します。
- **口コミ**：場所・体感・コメントの入力フォームと固定サンプル一覧。

「服整理（服管理）」タブと登録フォームは削除済みです。記録・診断・投稿ボタンは、未実装の処理であることを案内するだけです。入力内容や選択した写真はアプリ終了後には保持しません。旧WebView版のデータは読み込みません。

## 編集箇所

- `app/src/main/java/jp/climafit/app/MainActivity.kt`：4タブ、Compose画面、フォーム、温度による表示切り替え。
- `app/src/main/java/jp/climafit/app/UiModels.kt`：表示用データの形と固定サンプル。
- `app/src/main/java/jp/climafit/app/ClimaFitViewModel.kt`：画面上の選択状態、メッセージ、写真プレビュー。通信・判定・保存機能はありません。
- `docs/ui-handoff.md`：バックエンド担当者への画面の引き継ぎメモ。API仕様は決めていません。

## ビルド

Windows：`gradlew.bat assembleDebug`

macOS / Linux：`./gradlew assembleDebug`

## 検証結果

このクラウドで `:app:assembleDebug` が成功し、デバッグAPKを生成できました。実機・エミュレーターでの画面操作は未確認です。旧HTML版のブラウザーテスト結果はKotlin版には適用していません。
