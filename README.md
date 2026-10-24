# ClimaFit Android フロントエンド

添付HTMLを元にした Android Studio プロジェクトです。Java の Activity 内で、同梱HTML/CSS/JavaScriptを WebView に表示します。Kotlin/Compose 版ではありません。画面は通信なしで表示できます。

## Windows の Android Studio で開く

1. ZIP を展開します。
2. Android Studio の **File → Open** で、`settings.gradle.kts` のある `codex_test` フォルダーを選びます。`Documents` 全体や `app` 単体を開かないでください。
3. Gradle Sync の完了を待ちます。初回はネット接続が必要です。
4. JDK エラーが出たら **Use Embedded JDK** を選びます。または Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK を Android Studio の Embedded JDK / jbr（JDK 17以上）にします。
5. SDK Manager で Android SDK Platform 35 をインストールします。Android Studio は Android Gradle Plugin 8.9 に対応する Meerkat 以降を使ってください。
6. Device Manager で API 26 以上のエミュレーターを作成して起動するか、USB デバッグを有効にした実機を接続します。
7. 上部で `app` と端末を選択し、▶ Run を押します。

Gradle 8.11.1 / Android Gradle Plugin 8.9.2 / compile・target SDK 35 / min SDK 26。SDK の場所は Android Studio が作る `local.properties` に設定され、Git 管理しません。

## 実装した機能

- 提案・コーデAI・記録・口コミの4タブ。ユーザーの指定に合わせ「服整理（服管理）」タブと服の登録画面を削除。
- コーデAIは残し、画像選択・プレビュー・解除を実装。Androidの画像選択ダイアログに対応。診断APIは未接続のため、診断結果は表示しません。
- 場所に応じた室温・湿度・不快指数・服装提案の更新。
- 記録は「すべて / 現在の室温 / 指定した温度」で絞り込み。記録時の室温 ±2℃を対象にします。
- 口コミの登録と服装提案の記録。端末内 localStorage に保存し、再起動後も保持。共有・サーバー送信は未実装。
- センサー・天気・AI・サーバーには未接続。服装提案は温度の条件分岐です。

## バックエンド連携

[docs/backend-contract.md](docs/backend-contract.md) を参照してください。現状は外部リクエストをブロックしたオフライン版です。バックエンド接続時には通信処理・Android権限・接続先の許可を別途実装します。

## フロントエンドをブラウザーで確認

Python 3 がある場合、プロジェクトルートで次を実行しブラウザーから確認できます。

```sh
python -m http.server 8765 --directory app/src/main/assets
```

自動テストとCSS再生成には Node.js 18 以上、Python 3 が必要です。Androidアプリの実行にNode.jsは不要です。

```sh
npm ci
npx playwright install chromium
npm test
npm run build:css
```

システム Chromium を使用する場合は `CHROMIUM_PATH` に実行ファイルを設定します。CSS は生成済みを同梱しています。Font Awesome Free 6.4.0 のCSS・フォント・ライセンスも同梱しています。

## このクラウドでの検証

Chromium による操作テスト **4件成功**：4タブと気温に応じた表示更新、服管理画面の削除、口コミのHTMLエスケープと永続化、記録の温度フィルターと永続化、コーデAIの画像プレビュー・解除と未接続案内、外部通信なし。

Android APK のビルドは未確認です。`./gradlew :app:assembleDebug` を試しましたが、この環境では Java からの Gradle 配布先の名前解決に失敗しました。Android SDK もこの環境にはありません。Android Studio の実機・エミュレーター上の表示とシステムバー・キーボード動作は手元で確認してください。
