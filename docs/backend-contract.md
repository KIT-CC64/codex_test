# バックエンド担当者向けの連携案

APIはまだ実装・呼出していません。以下はフロントの現在のデータ構造に合わせた提案で、認証やエンドポイントは今後合意してください。サーバーでも入力検証・認可が必要です。

## 気象・室内データ

取得候補：`GET /v1/climate`。フロントで取得したJSONを次のように適用できます。

```js
window.ClimaFit.setClimate({
  outdoorTemp: 18.5,
  indoorData: {
    "情報講義室A": { temp: 24.2, humidity: 62 },
    "図書館 2F": { temp: 21.0, humidity: 48 },
    "学生食堂": { temp: 26.5, humidity: 70 },
    "実験室3": { temp: 19.5, humidity: 42 }
  }
});
```

温度は数値（℃）、湿度は0〜100の数値。現状は上記4室を必須としています。屋外天気の説明・最高最低温度・観測時刻・センサー接続状態は未実装です。実データの接続済み表示は取得成功・鮮度確認後に追加してください。

## 服管理

ユーザーの指定により服管理タブと登録フォームは削除しました。服管理APIは現在の画面では使用しません。

## 口コミ

`GET /v1/reviews`、`POST /v1/reviews` を想定。

```json
{"location":"情報講義室A","feel":"快適","comment":"過ごしやすいです","created_at":"2026-10-24T05:00:00Z"}
```

コメント1〜500文字。現在は端末内でのみ保存し、共有・送信しません。接続時はサーバーの作成時刻を使い、表示時にローカル時刻へ変換してください。

## 服装記録

`GET /v1/outfit-logs`、`POST /v1/outfit-logs` を想定。

```json
{"id":101,"date":"2026-10-24T05:00:00Z","location":"情報講義室A","indoor_temp":24.2,"weather_temp":18.5,"title":"カーディガン重ね着","comment":"寒暖差に備えましょう"}
```

現在の記録は服装提案のスナップショットです。コーデAIタブは画像プレビューまで実装し、診断APIには未接続です。記録の温度フィルターは `abs(indoor_temp - temperature) <= 2`。現時点でAI判定・スコアは記録に含めません。診断APIへの画像送信と結果の記録は、応答仕様の合意後に接続してください。

## Android WebView との接続時の変更点

- Manifest に `android.permission.INTERNET` を追加。
- `MainActivity.java` の `shouldInterceptRequest` を変更し、同梱アセットに加えて合意したHTTPS APIホストだけを許可。
- `https://appassets.androidplatform.net` の Origin に対するサーバー側CORSを設定。認証方式・許可ヘッダーを合意。
- `app.js` のデモ初期化・localStorage 書込みを通信アダプターに接続し、読み込み中・エラー・再試行・二重送信防止を実装。
- APIキー等の秘密情報をHTML・JavaScript・APKに埋め込まない。
