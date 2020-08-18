package todobackend

import java.util.UUID

case class NewTodo(
                    title: String,
                    order: Option[Int]
                  )

case class Todo(
                 id: UUID,
                 title: String,
                 completed: Boolean,
                 order: Int
               ) {
  def url: String = "https://todobackend-endpoints4s.herokuapp.com/todos/" + id
}

case class EditTodo(
                     title: Option[String],
                     completed: Option[Boolean],
                     order: Option[Int]
                   )