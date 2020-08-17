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
               )
