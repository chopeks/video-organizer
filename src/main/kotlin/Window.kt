import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javafx.stage.Stage

class Window : javafx.application.Application() {
  override fun start(stage: Stage) {
    stage.title = "HTML"
    val scene = Scene(Group())

    val root = VBox()

    val browser = WebView()
    browser.setPrefSize(2520.0, 1440.0)
    browser.engine.isJavaScriptEnabled = true
    browser.engine.load("http://localhost:8080/index.html")
    root.children.addAll(browser)
    scene.root = root
    scene.addEventFilter(KeyEvent.KEY_PRESSED) {
      if (it.code == KeyCode.ESCAPE) {
        Platform.exit()
        System.exit(0)
      }
    }
    stage.scene = scene
    stage.isFullScreen = true
    stage.show()
  }
}