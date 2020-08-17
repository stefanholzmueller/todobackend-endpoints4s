package todobackend

import java.util.UUID

import cats.effect.IO
import doobie.Transactor
import org.http4s.HttpRoutes

class TodoServer(xa: Transactor[IO])
  extends endpoints4s.http4s.server.Endpoints[IO]
    with TodoEndpoints
    with endpoints4s.http4s.server.JsonEntitiesFromSchemas { parent =>

  val routes: HttpRoutes[IO] = HttpRoutes.of(
    routesFromEndpoints(
      getTodos.implementedBy(_ => Seq()),
      postTodo.implementedBy(newTodo => Todo(UUID.randomUUID(), newTodo.title, false, 0)),
      deleteTodos.implementedBy(_ => {})
    )
  )

}
