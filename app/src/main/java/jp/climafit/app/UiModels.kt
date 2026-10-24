package jp.climafit.app

/** Display samples only. Evaluation, suggestions, transport and storage belong to the backend. */
data class RoomUi(
    val name: String,
    val temperature: Double,
    val humidity: Int,
    val discomfort: String,
    val recommendationTitle: String,
    val recommendationDescription: String,
    val layers: List<String>,
    val photo: Int,
)
data class OutfitLogUi(
    val id: String,
    val date: String,
    val location: String,
    val indoorTemperature: Double,
    val outdoorTemperature: Double,
    val title: String,
    val comment: String,
    val photo: Int,
)
data class ReviewUi(val id: String, val location: String, val feel: String, val comment: String, val date: String)
enum class TemperatureMode(val label: String) {
    ALL("すべて"), OUTDOOR("今の金沢"), CURRENT("今の室温"), CUSTOM("温度を指定")
}
enum class AiMode(val title: String) {
    PHOTO("写真でコーデ診断"), SUGGEST("AIからのコーデ提案")
}
/** Frontend action payload only; this does not define a server API or send anything. */
data class AiRequestUi(
    val mode: AiMode,
    val prompt: String? = null,
    val photo: android.graphics.Bitmap? = null,
    val location: String,
    val indoorTemperature: Double,
    val outdoorTemperature: Double,
)
const val TODAY_OUTFIT_PROMPT = "今日の服装を提案して"
/** Fixed copy for the clothing guide preview; no temperature-based recommendation logic. */
data class ClothingGuideUi(val top: String, val bottom: String, val layer: String, val note: String)
object PreviewData {
    val clothingGuides = mapOf(
        "図書館" to ClothingGuideUi("Tシャツ", "薄手のズボン", "薄手のカーディガン", "図書館では羽織りを一枚。脱ぎ着しやすい服で、心地よく。"),
        "201教室" to ClothingGuideUi("Tシャツ", "薄手のズボン", "薄手のカーディガン", "教室では軽やかに。移動するときのために、羽織りを持って。"),
        "学生食堂" to ClothingGuideUi("Tシャツ", "薄手のズボン", "持ち歩けるカーディガン", "食堂では涼しく、屋外では一枚プラス。調整しやすい服装に。"),
        "実験室3" to ClothingGuideUi("長袖トップス", "薄手のズボン", "カーディガン", "ひんやりする部屋では、長袖と羽織りで調整を。"),
    )
    const val outdoorTemperature = 18.5
    val rooms = listOf(
        RoomUi("図書館", 21.0, 48, "66.4 · 快適", "羽織りを一枚、\n心地よく。", "長袖シャツに、軽いカーディガンを。屋外と室内の行き来も、脱ぎ着で調整できます。", listOf("カーディガン", "コットンシャツ", "ストレートパンツ"), R.drawable.outfit_warm),
        RoomUi("201教室", 24.2, 62, "71.9 · 快適", "軽やかに、\n教室も快適。", "シャツを主役に、羽織りものを一枚。暖かい教室では脱いで、すっきり過ごしましょう。", listOf("薄手のカーディガン", "長袖シャツ", "パンツ"), R.drawable.outfit_warm),
        RoomUi("学生食堂", 26.5, 70, "76.0 · やや暑い", "さらっと、\n涼しい一着。", "通気性のよいトップスを中心に。移動中に羽織れる一枚があると安心です。", listOf("薄手のトップス", "軽いパンツ"), R.drawable.outfit_warm),
        RoomUi("実験室3", 19.5, 42, "64.2 · 肌寒い", "冷える部屋には、\nやさしい一枚を。", "長袖にライトジャケットを重ねて。冷気が気になる場所でも調節しやすい組み合わせです。", listOf("ライトジャケット", "白いトップス", "ゆったりパンツ"), R.drawable.outfit_cool),
    )
    val logs = listOf(
        OutfitLogUi("preview-1", "10月24日", "201教室", 24.2, 18.5, "カーディガンで軽やかに", "生成したサンプル写真", R.drawable.outfit_warm),
        OutfitLogUi("preview-2", "10月23日", "実験室3", 19.5, 17.0, "肌寒い日のライトジャケット", "生成したサンプル写真", R.drawable.outfit_cool),
    )
    val reviews = listOf(
        ReviewUi("preview-1", "図書館", "少し寒い", "窓際の席は少しひんやり。薄いカーディガンがあるとちょうどよかったです。", "10分前"),
        ReviewUi("preview-2", "201教室", "快適", "今日は過ごしやすい室温！長袖シャツ一枚で大丈夫でした。", "25分前"),
        ReviewUi("preview-3", "学生食堂", "少し暑い", "お昼は人が多くてぽかぽか。上着は脱いで過ごしています。", "40分前"),
        ReviewUi("preview-4", "実験室3", "かなり寒い", "空調の近くは冷えやすいので、羽織りものがおすすめです。", "1時間前"),
        ReviewUi("preview-5", "図書館", "快適", "奥の席はちょうどいい感じ。落ち着いて作業できました。", "2時間前"),
    )
}
