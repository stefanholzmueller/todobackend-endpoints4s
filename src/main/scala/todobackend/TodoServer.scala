package todobackend

import java.util.UUID

import cats.effect.IO
import doobie.{ConnectionIO, LogHandler, Transactor}
import doobie.implicits._
import doobie.h2.implicits._
import org.http4s.HttpRoutes

class TodoServer(xa: Transactor[IO])
  extends endpoints4s.http4s.server.Endpoints[IO]
    with TodoEndpoints
    with endpoints4s.http4s.server.JsonEntitiesFromSchemas { parent =>

  val routes: HttpRoutes[IO] = HttpRoutes.of(
    routesFromEndpoints(
      getTodos.implementedByEffect(_ => loadTodos),
      postTodo.implementedByEffect(createTodo),
      deleteTodos.implementedByEffect(_ => clearTodos)
    )
  )

  // implicit val logHandler = LogHandler.jdkLogHandler

  private def loadTodos: IO[List[Todo]] = {
    sql"SELECT id, title, completed, order_ FROM todo"
      .query[Todo]
      .to[List]
      .transact(xa)
  }

  private def createTodo(newTodo: NewTodo): IO[Todo] = {
    val todo = Todo(
      id = UUID.randomUUID(),
      title = newTodo.title,
      completed = false,
      order = newTodo.order.getOrElse(0)
    )
    insertTodo(todo).transact(xa)
  }

  private def insertTodo(todo: Todo): ConnectionIO[Todo] = {
    val id = todo.id
    val title = todo.title
    val completed = todo.completed
    val order = todo.order
    sql"INSERT INTO todo (id, title, completed, order_) values ($id, $title, $completed, $order)"
      .update
      .run
      .map(_ => todo)
  }

  private def clearTodos: IO[Unit] =
    sql"DELETE FROM todo"
      .update
      .run
      .transact(xa)
      .map(_ => {})

}
