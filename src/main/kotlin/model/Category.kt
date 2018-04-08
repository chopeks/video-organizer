package model

import db.CategoryTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Category(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Category>(CategoryTable)

  var name by CategoryTable.name
  var image by CategoryTable.image

  val pojo
    get() = CategoryPojo(id.value, name, null)
}

data class CategoryPojo(
  val id: Int,
  val name: String,
  var image: String?
)