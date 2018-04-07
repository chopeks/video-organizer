package services

import db.MovieTable
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import model.MoviePojo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Route.duplicatesService() {
  get("/duplicates") {
    call.respond(transaction {
      val joinColumn: Column<*> = MovieTable.duration
      val inner = MovieTable.slice(MovieTable.duration, MovieTable.duration.count()).selectAll()
        .groupBy(MovieTable.duration)
        .having { MovieTable.id.count() greater 1 }
        .alias("q1")

      Join(MovieTable).slice(MovieTable.id, MovieTable.duration).source.join(inner, JoinType.INNER, MovieTable.duration, inner[joinColumn])
        .selectAll()
        .orderBy(MovieTable.duration)
        .groupBy({
          it[MovieTable.duration]
        }, {
          MoviePojo(it[MovieTable.id].value, it[MovieTable.name], it[MovieTable.duration], "%.2f MB".format(File(it[MovieTable.path]).length() / 1024.0 / 1024.0))
        })
        .toList()
        .map {
          mapOf(
            "duration" to it.first,
            "movie" to it.second
          )
        }
    })
  }
}