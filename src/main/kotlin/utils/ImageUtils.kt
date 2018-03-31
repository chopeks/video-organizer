package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.util.*

fun BufferedImage.normalizeImage() = this
  .let { Scalr.resize(it, Scalr.Method.QUALITY, 400, it.height * 400 / it.width) }
  .let {
    val newHeight = minOf(it.width * 9 / 16, it.height)
    val offset = (it.height - newHeight) / 2
    Scalr.crop(it, 0, offset, it.width, newHeight)
  }

fun String.urlImageToBase64(): String =
  "data:image/jpg;base64," + String(Base64.getMimeEncoder()
    .encode(OkHttpClient()
      .newCall(Request.Builder()
        .url(this)
        .build()
      )
      .execute()
      .body()
      ?.bytes()
    )
  )
