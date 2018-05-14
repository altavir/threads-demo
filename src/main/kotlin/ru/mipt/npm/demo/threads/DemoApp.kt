package ru.mipt.npm.demo.threads

import javafx.concurrent.Task
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.stage.Stage
import org.controlsfx.control.TaskProgressView
import tornadofx.*

val icon = Image(DemoApp::class.java.getResourceAsStream("/npm-logo-no-text-white.png"))

class DemoApp : App(ThreadView::class) {
    override fun start(stage: Stage) {
        stage.icons += icon
        super.start(stage)
    }
}

val taskView = TaskProgressView<Task<*>>()

fun <T> runTask(task: FXTask<*>.() -> T): Task<*> {
    return runAsync(true, null, task).also { runLater { taskView.tasks.add(it)} }
}

class ThreadView : View() {
    override val root: Parent = borderpane {
        left {
            vbox {
                hgrow = Priority.ALWAYS
                button("Loop task") {
                    action { runTask(loopTask) }
                    maxWidth = Double.MAX_VALUE
                }
                button("Async task") {
                    action { runTask(asyncCall) }
                    maxWidth = Double.MAX_VALUE
                }
                button("Callback task") {
                    action { runTask(callback) }
                    maxWidth = Double.MAX_VALUE
                }
                button("Parallel execution") {
                    action { runTask(parallel) }
                    maxWidth = Double.MAX_VALUE
                }
                button("Coroutines") {
                    action { runTask(coroutines) }
                    maxWidth = Double.MAX_VALUE
                }
            }
        }
        center = taskView
    }
}

