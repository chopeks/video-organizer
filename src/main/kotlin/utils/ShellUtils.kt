package utils

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun String.executeCommand(workingDir: File): String? {
  try {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
      .directory(workingDir)
      .redirectOutput(ProcessBuilder.Redirect.PIPE)
      .redirectError(ProcessBuilder.Redirect.PIPE)
      .start()

    proc.waitFor(200, TimeUnit.MILLISECONDS)
    return proc.inputStream.bufferedReader().readText()
  } catch (e: IOException) {
    e.printStackTrace()
    return null
  }
}

fun String.runCommand(workingDir: File) {
  ProcessBuilder(*split(" ").toTypedArray())
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start()
    .waitFor(1, TimeUnit.SECONDS)
}