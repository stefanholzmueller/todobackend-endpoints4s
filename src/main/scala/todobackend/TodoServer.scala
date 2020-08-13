package todobackend

import cats.effect.IO
import org.http4s.HttpRoutes

class TodoServer
  extends endpoints4s.http4s.server.Endpoints[IO]
    with TodoEndpoints
    with endpoints4s.http4s.server.JsonEntitiesFromSchemas { parent =>

  val routes: HttpRoutes[IO] = HttpRoutes.of(
    routesFromEndpoints(
      getTodos.implementedBy(_ => Seq("a", "b"))
    )
  )

}
