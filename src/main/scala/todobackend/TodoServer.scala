package todobackend

import java.util.UUID

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}
import io.getquill.{idiom => _}
import org.http4s.HttpRoutes


class TodoServer(xa: Transactor[IO], repository: TodoRepository)
  extends endpoints4s.http4s.server.Endpoints[IO]
    with TodoEndpoints
    with endpoints4s.http4s.server.JsonEntitiesFromSchemas { parent =>

  val routes: HttpRoutes[IO] = HttpRoutes.of(
    routesFromEndpoints(
      getTodos.implementedByEffect(_ => repository.selectTodos),
      postTodo.implementedByEffect(createTodo),
      deleteTodos.implementedByEffect(_ => repository.deleteTodos),
      getTodo.implementedByEffect { id =>
        repository.selectTodo(id).transact(xa)
      },
      deleteTodo.implementedByEffect(repository.deleteTodo),
      patchTodo.implementedByEffect { case (id, editTodo) =>
        val updates: List[ConnectionIO[Unit]] = if (editTodo.title.isDefined) List(repository.changeTitle(id, editTodo.title.get)) else List()
        val tx = updates
          .sequence_
          .flatMap(_ => repository.selectTodo(id))
        tx.transact(xa)
      }
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

}
