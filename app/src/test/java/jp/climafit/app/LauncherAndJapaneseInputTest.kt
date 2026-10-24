package jp.climafit.app

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
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
class LauncherAndJapaneseInputTest {
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

    @Test fun chatAcceptsJapaneseFullWidthCharactersAndKeepsThemVisible() {
        compose.setContent {
            var value by remember { mutableStateOf(TextFieldValue()) }
            ClimaFitTheme {
                JapaneseMessageField(value, { value = it }, "今日の服装を提案して", Modifier.testTag("message"))
            }
        }
        val japanese = "図書館に行く服を提案してください。全角ＡＢＣ１２３"
        compose.onNodeWithTag("message").performClick().performTextInput(japanese)
        compose.onNodeWithTag("message").assertTextContains(japanese)
    }
}
