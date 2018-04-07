package services

import db.MovieTable
import db.MovieTable.thumbnail
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import model.Actor
import model.Category
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import random
import utils.makeScreenshot
import utils.makeScreenshots
import utils.normalizeImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun Route.imageService() {
  get("/image/category/{id}") {
    call.respond(transaction { arrayOf(Category.findById(call.parameters["id"]!!.toInt())?.image) })
  }
  get("/image/actor/{id}") {
    call.respond(transaction { arrayOf(Actor.findById(call.parameters["id"]!!.toInt())?.image) })
  }
  get("/image/movie/{id}") {
    val image = transaction {
      MovieTable.select { MovieTable.id eq call.parameters["id"] }.firstOrNull().also {
        if (it != null) {
          if (it[thumbnail] != null && call.request.queryParameters["refresh"] != "true") {
            return@transaction it[thumbnail]
          } else {
            val tempDir = File(UUID.randomUUID().toString().substring(0..7)).apply { mkdirs() }
            val images = mutableListOf<String>()
            makeScreenshot(tempDir, File(it[MovieTable.path]), (1..999).random().toLong()).also { img ->
              ImageIO.read(img.readBytes().inputStream())
                .normalizeImage()
                .let { ImageIO.write(it, "jpg", img) }
              transaction {
                MovieTable.update({ MovieTable.id eq call.parameters["id"] }, body = {
                  it[thumbnail] = "data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(img.readBytes()))
                })
              }
              images.add("data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(img.readBytes())))
              img.delete()
            }
            tempDir.delete()
            return@transaction images[0]
          }
        }
      }
    }
    call.respond(arrayOf(image))
  }
  get("/images/movie/{id}") {
    val images = transaction {
      MovieTable.select { MovieTable.id eq call.parameters["id"] }.firstOrNull().also {
        if (it != null) {
          val tempDir = File(UUID.randomUUID().toString().substring(0..7)).apply { mkdirs() }
          val images = mutableListOf<String>()
          makeScreenshots(tempDir, File(it[MovieTable.path])).forEach {
            images.add("data:image/jpg;base64," + String(Base64.getMimeEncoder().encode(it.readBytes())))
            it.delete()
          }
          tempDir.delete()
          return@transaction images
        }
      }
    }
    call.respond(images ?: HttpStatusCode.NotFound)
  }
}