package db

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object CategoryTable : IntIdTable("category") {
  val name = text("name")
  val image = text("image").nullable()
}

object ActorTable : IntIdTable("actor") {
  val name = text("name")
  val image = text("image").nullable()
}

object MovieTable : IntIdTable("movie") {
  val name = text("name")
  val path = text("path")
  val thumbnail = text("thumbnail").nullable()
  val duration = integer("duration").nullable()
}

object MovieCategories : IntIdTable("movie_category") {
  val movie = integer("movie")
  val category = integer("category")
}

object MovieActors : IntIdTable("movie_actor") {
  val movie = integer("movie")
  val actor = integer("actor")
}

object PathsTable : Table("paths") {
  val path = text("path")
  val count = integer("files").default(0)
}

object SchemaVerionsTable : Table("schemasVer") {
  val version = integer("version")

  fun inc() {
    update({ version.isNotNull() }) {
      it[version] = selectAll().first()[version] + 1
    }
  }
}