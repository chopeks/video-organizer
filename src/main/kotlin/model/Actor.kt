package model

import db.ActorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Actor(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Actor>(ActorTable)

  var name by ActorTable.name
  var image by ActorTable.image

  val pojo
    get() = ActorPojo(id.value, name, null)
}

data class ActorPojo(
  val id: Int,
  val name: String,
  var image: String?
)