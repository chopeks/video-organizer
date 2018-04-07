package services

import db.MovieTable
import db.PathsTable
import db.PathsTable.count
import db.PathsTable.path
import getFiles
import io.ktor.application.call
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import model.PathPojo
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utils.RefreshUtils
import java.io.File
import kotlin.concurrent.thread

fun Route.directoryService() {
  get("/directories") {
    call.respond(transaction {
      PathsTable.selectAll().map { PathPojo(it[path], it[count]) }
    })
  }
  post("/directory") {
    call.receiveOrNull<PathPojo>()?.let { json ->
      transaction {
        try {
          PathsTable.insert {
            it[path] = json.path
            it[count] = getFiles(File(json.path)).size
          }
        } catch (e: Throwable) {
        }
      }
      call.respond("{}")
    }
    thread { RefreshUtils.refresh() }
  }
  post("/directory/remove") {
    call.receiveOrNull<PathPojo>()?.let { json ->
      transaction {
        PathsTable.deleteWhere { PathsTable.path eq json.path }
        MovieTable.deleteWhere { MovieTable.path like "${json.path}%" }
      }
    }
    call.respond("{}")
  }
}