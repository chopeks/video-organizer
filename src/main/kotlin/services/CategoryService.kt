package services

import db.CategoryTable
import db.MovieCategories
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import model.Category
import model.CategoryPojo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.urlImageToBase64

fun Route.categoryService() {
  get("/categories") { call.respond(transaction { Category.all().sortedBy { it.name }.map { it.pojo } }) }

  //region crud
  post("/category") {
    call.receiveOrNull<CategoryPojo>()?.let {
      call.respond(HttpStatusCode.OK, transaction {
        if (it.image?.startsWith("http") == true) {
          it.image = it.image?.urlImageToBase64()
        }
        if (Category.find { CategoryTable.id eq it.id }.firstOrNull() != null) {
          CategoryTable.update({ CategoryTable.id eq it.id }) { obj ->
            obj[CategoryTable.name] = it.name
            obj[CategoryTable.image] = it.image
          }
        } else {
          CategoryTable.insert { new ->
            new[CategoryTable.name] = it.name
            new[CategoryTable.image] = it.image
          }
        }
        "{}"
      })
    }
  }
  delete("/category/{id}") {
    transaction {
      CategoryTable.deleteWhere { CategoryTable.id eq call.parameters["id"] }
      MovieCategories.deleteWhere { MovieCategories.category eq call.parameters["id"] }
    }
    call.respond(HttpStatusCode.OK, "{}")
  }
  //endregion

  //region binding
  post("/categories/{category}/{movie}") {
    val row = transaction {
      MovieCategories.select { (MovieCategories.movie eq call.parameters["movie"]) and (MovieCategories.category eq call.parameters["category"]) }
        .firstOrNull()
    }
    if (row != null) {
      call.respond(HttpStatusCode.Conflict)
    } else {
      transaction {
        MovieCategories.insert {
          it[MovieCategories.movie] = call.parameters["movie"]!!.toInt()
          it[MovieCategories.category] = call.parameters["category"]!!.toInt()
        }
      }
      call.respond("{}")
    }
  }
  delete("/categories/{category}/{movie}") {
    transaction {
      MovieCategories.deleteWhere { (MovieCategories.movie eq call.parameters["movie"]) and (MovieCategories.category eq call.parameters["category"]) }
    }
    call.respond("{}")
  }
  //endregion
}