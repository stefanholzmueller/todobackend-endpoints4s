package todobackend

import java.util.UUID

import cats.effect.IO
import io.getquill.{idiom => _}
import org.http4s.HttpRoutes


class TodoServer(repository: TodoRespository)
  extends endpoints4s.http4s.server.Endpoints[IO]
    with TodoEndpoints
    with endpoints4s.http4s.server.JsonEntitiesFromSchemas { parent =>

  val routes: HttpRoutes[IO] = HttpRoutes.of(
    routesFromEndpoints(
      getTodos.implementedByEffect(_ => repository.selectTodos),
      postTodo.implementedByEffect(createTodo),
      deleteTodos.implementedByEffect(_ => repository.deleteTodos),
      getTodo.implementedByEffect(repository.selectTodo),
      deleteTodo.implementedByEffect(repository.deleteTodo),
      patchTodo.implementedByEffect((changeTodo _).tupled)
    )
  )

  private def createTodo(newTodo: NewTodo): IO[Todo] = {
    val todo = Todo(
      id = UUID.randomUUID(),
      title = newTodo.title,
      completed = false,
      order = newTodo.order.getOrElse(0)
    )
    repository.insertTodo(todo)
  }

  private def changeTodo(id: UUID, editTodo: EditTodo): IO[Todo] = {
    ???
  }

}
