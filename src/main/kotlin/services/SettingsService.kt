package services

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import model.SettingsPojo
import utils.Cache

fun Route.settingsService() {
  get("/settings") {
    call.respond(HttpStatusCode.OK, Cache.settings)
  }
  post("/settings") {
    call.receiveOrNull<SettingsPojo>()?.let {
      Cache.settings = it
    }
    call.respond(HttpStatusCode.OK, Cache.settings)
  }
}