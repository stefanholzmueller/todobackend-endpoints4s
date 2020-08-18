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
      getTodos.implementedByEffect { _ =>
        repository.selectTodos.transact(xa)
      },
      postTodo.implementedByEffect { newTodo =>
        val todo = Todo(
          id = UUID.randomUUID(),
          title = newTodo.title,
          completed = false,
          order = newTodo.order.getOrElse(0)
        )
        repository.insertTodo(todo).transact(xa)
      },
      deleteTodos.implementedByEffect { _ =>
        repository.deleteTodos.transact(xa)
      },
      getTodo.implementedByEffect { id =>
        repository.selectTodo(id).transact(xa)
      },
      deleteTodo.implementedByEffect { id =>
        repository.deleteTodo(id).transact(xa)
      },
      patchTodo.implementedByEffect { case (id, editTodo) =>
        val options: List[Option[ConnectionIO[Unit]]] = List(
          editTodo.title.map(title => repository.changeTitle(id, title)),
          editTodo.order.map(order => repository.changeOrder(id, order)),
          editTodo.completed.map(completed => repository.changeCompleted(id, completed))
        )
        val updates: List[ConnectionIO[Unit]] = options.flatten
        val tx: ConnectionIO[Todo] = updates
          .sequence_
          .flatMap(_ => repository.selectTodo(id))
        tx.transact(xa)
      }
    )
  )

}
