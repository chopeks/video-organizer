package utils

import java.io.File
import java.util.concurrent.TimeUnit

fun getVideoDuration(video: File) = "ffprobe -show_entries format=duration -sexagesimal \"${video.absolutePath}\""
  .executeCommand(File("./"))
  ?.also { println(it) }
  ?.lines()
  ?.get(1)
  ?.split("=")
  ?.get(1)
  ?.let {
    val parts = it.replace(".", ":").split(":").map { it.toLong() }
    TimeUnit.HOURS.toMillis(parts[0]) + TimeUnit.MINUTES.toMillis(parts[1]) + TimeUnit.SECONDS.toMillis(parts[2]) + TimeUnit.NANOSECONDS.toMillis(parts[3])
  } ?: 0

fun makeScreenshot(targetDir: File, video: File, percent: Long = 110): File {
  val interval: Long = getVideoDuration(video) * percent / 1000L
  "ffmpeg -ss ${TimeUnit.MILLISECONDS.toHours(interval)}:${TimeUnit.MILLISECONDS.toMinutes(interval) % 60}:${TimeUnit.MILLISECONDS.toSeconds(interval) % 60} -i \"${video.absolutePath}\" -vframes 1 0.jpg".runCommand(targetDir)
  return targetDir.listFiles().first()
}


fun makeScreenshots(targetDir: File, video: File): Array<File> {
  val interval = getVideoDuration(video) / 11L
  for (i in 1..9) {
    "ffmpeg -ss ${TimeUnit.MILLISECONDS.toHours(i * interval)}:${TimeUnit.MILLISECONDS.toMinutes(i * interval) % 60}:${TimeUnit.MILLISECONDS.toSeconds(i * interval) % 60} -i \"${video.absolutePath}\" -vframes 1 ${i}.jpg".runCommand(targetDir)
  }
  return targetDir.listFiles()
}
