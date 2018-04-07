package services

import db.MovieActors
import db.MovieCategories
import db.MovieTable
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import model.MoviePojo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Cache
import utils.runCommand
import java.io.File

fun Route.`movie service`() {
  get("/movie/play/{id}") {
    transaction {
      MovieTable.select { MovieTable.id eq call.parameters["id"] }.limit(1).firstOrNull().also {
        if (it != null) {
          "${Cache.moviePlayer} \"${it[MovieTable.path]}\"".runCommand(File(it[MovieTable.path]).parentFile)
        }
      }
    }
    call.respond(HttpStatusCode.OK)
  }
  get("/movie/{from}/{count}") {
    call.respond(transaction {
      val categories = call.request.queryParameters["category"]
      val actors = call.request.queryParameters["actor"]

      var columnCategory: Column<*> = MovieTable.id
      val viaCategory = when (categories) {
        null, "null" -> MovieTable.slice(MovieTable.id).selectAll()
        "0" -> MovieTable
          .join(MovieCategories, JoinType.LEFT, MovieTable.id, MovieCategories.movie)
          .slice(MovieTable.id)
          .select { MovieCategories.category.isNull() }
        else -> categories.split(",").let {
          columnCategory = MovieCategories.movie
          MovieCategories
            .slice(MovieCategories.movie)
            .select { MovieCategories.category inList it }
            .groupBy(MovieCategories.movie)
            .having { MovieCategories.movie.count() eq it.size }
        }
      }.alias("q1")

      var columnActor: Column<*> = MovieTable.id
      val viaActor = when (actors) {
        null, "null" -> MovieTable.slice(MovieTable.id).selectAll()
        "0" -> MovieTable
          .join(MovieActors, JoinType.LEFT, MovieTable.id, MovieActors.movie)
          .slice(MovieTable.id)
          .select { MovieActors.actor.isNull() }
        else -> actors.split(",").let {
          columnActor = MovieActors.movie
          MovieActors
            .slice(MovieActors.movie)
            .select { MovieActors.actor inList it }
            .groupBy(MovieActors.movie)
            .having { MovieActors.movie.count() eq it.size }
        }
      }.alias("q2")

      mapOf(
        "movies" to Join(MovieTable)
          .join(viaActor, JoinType.LEFT, MovieTable.id, viaActor[columnActor])
          .join(viaCategory, JoinType.LEFT, MovieTable.id, viaCategory[columnCategory])
          .select { viaActor[columnActor].isNotNull() and viaCategory[columnCategory].isNotNull() }
          .groupBy(MovieTable.id)
          .apply {
            when (call.request.queryParameters["filter"]?.toIntOrNull()) {
              1 -> orderBy(MovieTable.duration, false)
              else -> orderBy(MovieTable.id, false)
            }
          }
          .limit(call.parameters["count"]!!.toInt(), call.parameters["from"]!!.toInt())
          .map { MoviePojo(it[MovieTable.id].value, it[MovieTable.name], it[MovieTable.duration]) },
        "count" to Join(MovieTable)
          .join(viaActor, JoinType.LEFT, MovieTable.id, viaActor[columnActor])
          .join(viaCategory, JoinType.LEFT, MovieTable.id, viaCategory[columnCategory])
          .select { viaActor[columnActor].isNotNull() and viaCategory[columnCategory].isNotNull() }
          .groupBy(MovieTable.id)
          .orderBy(MovieTable.id, false)
          .count()
      )
    })
  }
  get("/movie/{id}") {
    call.respond(transaction {
      mapOf<String, Any>(
        "categories" to MovieCategories.select { MovieCategories.movie eq call.parameters["id"]?.toInt() }
          .map { it[MovieCategories.category].toString() },
        "actors" to MovieActors.select { MovieActors.movie eq call.parameters["id"]?.toInt() }
          .map { it[MovieActors.actor].toString() }
      )
    })
  }
  delete("/movie/{id}") {
    call.parameters["id"]?.toIntOrNull()?.also { id ->
      val path = transaction {
        val ret = MovieTable.select { MovieTable.id eq id }.first()[MovieTable.path]
        MovieTable.deleteWhere { MovieTable.id eq id }
        MovieActors.deleteWhere { MovieActors.movie eq id }
        MovieCategories.deleteWhere { MovieCategories.movie eq id }
        ret
      }
      File(path).delete()
      call.respond("{}")
    }
  }
}