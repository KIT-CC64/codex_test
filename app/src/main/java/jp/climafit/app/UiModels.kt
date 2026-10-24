package jp.climafit.app

/** Display-only sample data. No sensor access, AI logic, index calculation or storage. */
data class RoomUi(
    val name: String,
    val temperature: Double,
    val humidity: Int,
    val discomfort: String,
    val recommendationTitle: String,
    val recommendationDescription: String,
    val layers: List<String>,
)
data class OutfitLogUi(
    val id: String,
    val date: String,
    val location: String,
    val indoorTemperature: Double,
    val outdoorTemperature: Double,
    val title: String,
    val comment: String,
)
data class ReviewUi(val id: String, val location: String, val feel: String, val comment: String, val date: String)
enum class TemperatureMode(val label: String) { ALL("すべて"), CURRENT("現在の室温"), CUSTOM("指定した温度") }

object PreviewData {
    const val outdoorTemperature = 18.5
    val rooms = listOf(
        RoomUi("情報講義室A", 24.2, 62, "71.9 · 快適", "カーディガンを使った重ね着", "屋外の肌寒さに備えて、着脱しやすい羽織りものを持ちましょう。", listOf("カーディガン / 薄手ジャケット", "長袖シャツ")),
        RoomUi("図書館 2F", 21.0, 48, "66.4 · 快適", "図書館向けの服装提案", "長袖シャツを基本に、体感に合わせて調節しましょう。", listOf("薄手の羽織りもの", "長袖のトップス")),
        RoomUi("学生食堂", 26.5, 70, "76.0 · やや暑い", "食堂向けの服装提案", "提案内容は表示確認用の固定サンプルです。", listOf("通気性の良いシャツ")),
        RoomUi("実験室3", 19.5, 42, "64.2 · 肌寒い", "実験室向けの服装提案", "肌寒さに備えてジャケットを用意しましょう。", listOf("ジャケット", "長袖シャツ")),
    )
    val logs = listOf(
        OutfitLogUi("preview-1", "2026/10/24 12:30", "情報講義室A", 24.2, 18.5, "カーディガンを使った重ね着", "記録一覧の表示サンプルです。"),
        OutfitLogUi("preview-2", "2026/10/23 10:00", "実験室3", 19.5, 17.0, "長袖とジャケット", "温度条件による表示切り替えを確認できます。"),
    )
    val reviews = listOf(
        ReviewUi("preview-1", "情報講義室A", "快適", "エアコンの温度設定が丁度良く快適です！", "サンプル"),
        ReviewUi("preview-2", "実験室3", "かなり寒い", "羽織りものがあると安心です。", "サンプル"),
    )
}
