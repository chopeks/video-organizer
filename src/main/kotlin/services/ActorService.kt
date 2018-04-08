package services

import db.ActorTable
import db.MovieActors
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import model.Actor
import model.ActorPojo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.urlImageToBase64

fun Route.actorService() {
  // get all actors
  get("/actors") {
    call.respond(transaction { Actor.all().sortedBy { it.name }.map { it.pojo } })
  }
  //region crud
  get("/actor/{id}") {
    call.respond(transaction { ActorTable.select { ActorTable.id eq call.parameters["id"] } }.firstOrNull()
      ?: HttpStatusCode.NotFound)
  }
  post("/actor") {
    call.receiveOrNull<ActorPojo>()?.let {
      if (it.image?.startsWith("http") == true) {
        it.image = it.image?.urlImageToBase64()
      }
      call.respond(HttpStatusCode.OK, transaction {
        if (Actor.find { ActorTable.id eq it.id }.firstOrNull() != null) {
          ActorTable.update({ ActorTable.id eq it.id }) { obj ->
            obj[name] = it.name
            obj[image] = it.image
          }
        } else {
          ActorTable.insert { new ->
            new[name] = it.name
            new[image] = it.image
          }
        }
        "{}"
      })
    }
  }
  delete("/actor/{id}") {
    transaction { ActorTable.deleteWhere { ActorTable.id eq call.parameters["id"] } }
    call.respond(HttpStatusCode.OK, "{}")
  }
  //endregion

  //region binding
  post("/actors/{actor}/{movie}") {
    val row = transaction {
      MovieActors.select { (MovieActors.movie eq call.parameters["movie"]) and (MovieActors.actor eq call.parameters["actor"]) }
        .firstOrNull()
    }
    if (row != null) {
      call.respond(HttpStatusCode.Conflict)
    } else {
      transaction {
        MovieActors.insert {
          it[MovieActors.movie] = call.parameters["movie"]!!.toInt()
          it[MovieActors.actor] = call.parameters["actor"]!!.toInt()
        }
      }
      call.respond("{}")
    }
  }
  delete("/actors/{actor}/{movie}") {
    transaction {
      MovieActors.deleteWhere { (MovieActors.movie eq call.parameters["movie"]) and (MovieActors.actor eq call.parameters["actor"]) }
    }
    call.respond("{}")
  }
  //endregion
}

