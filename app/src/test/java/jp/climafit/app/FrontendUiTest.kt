package jp.climafit.app

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class FrontendUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun launcherLoadsAndRendersCustomIconInsteadOfAndroidDefault() {
        val app = RuntimeEnvironment.getApplication()
        val manager = app.packageManager
        val info = manager.getActivityInfo(ComponentName(app, MainActivity::class.java), 0)
        val icon = info.loadIcon(manager)
        assertTrue("Activity icon must inflate as an adaptive icon", icon is AdaptiveIconDrawable)
        fun render(drawable: Drawable): Bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888).also {
            drawable.setBounds(0, 0, 192, 192); drawable.draw(Canvas(it))
        }
        val rendered = render(icon)
        assertFalse("The launcher must not fall back to Android's default icon", rendered.sameAs(render(manager.defaultActivityIcon)))
        val output = File("build/reports/icon-render.png")
        output.parentFile?.mkdirs()
        output.outputStream().use { rendered.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun showAi(): ClimaFitViewModel {
        val model = ClimaFitViewModel(RuntimeEnvironment.getApplication())
        compose.setContent { ClimaFitTheme { AiScreen(model) } }
        return model
    }

    @Test fun aiEntranceHasTwoChoicesAndNoChatInput() {
        showAi()
        compose.onNodeWithText("写真でコーデ診断").assertExists()
        compose.onNodeWithText("AIにコーデを提案してもらう").assertExists()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test fun suggestionButtonUsesExactFixedJapanesePromptWithoutTextEntry() {
        val model = showAi()
        compose.onNodeWithText("AIにコーデを提案してもらう").performClick()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithText("今日の服装を提案して").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(AiMode.SUGGEST, model.latestAiRequest?.mode)
            assertEquals("今日の服装を提案して", model.latestAiRequest?.prompt)
            assertNull(model.latestAiRequest?.photo)
        }
        compose.onNodeWithText("今日のおすすめコーデ").assertExists()
    }

    @Test fun photoDiagnosisRequiresPhotoAndPassesItWithoutChatText() {
        val model = showAi()
        compose.onNodeWithText("写真でコーデ診断").performClick()
        compose.onNodeWithText("この写真で診断する").performScrollTo().assertIsNotEnabled()
        val photo = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        compose.runOnIdle { model.useCameraPreview(photo) }
        compose.onNodeWithText("この写真で診断する").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(AiMode.PHOTO, model.latestAiRequest?.mode)
            assertSame(photo, model.latestAiRequest?.photo)
            assertNull(model.latestAiRequest?.prompt)
        }
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithText("診断結果").assertExists()
    }

    @Test fun communityStillAcceptsJapaneseAndFullWidthCharacters() {
        val model = ClimaFitViewModel(RuntimeEnvironment.getApplication())
        compose.setContent { ClimaFitTheme { CommunityScreen(model) } }
        compose.onNodeWithText("いまの体感を投稿する").performClick()
        val japanese = "図書館は少し寒いです。全角ＡＢＣ１２３"
        compose.onNode(hasSetTextAction()).performClick().performTextInput(japanese)
        compose.onNode(hasSetTextAction()).assertTextContains(japanese)
    }
}
