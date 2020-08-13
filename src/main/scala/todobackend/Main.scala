package todobackend

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import org.http4s.{Headers, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, _}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent.WebjarService.Config
import org.http4s.server.staticcontent.webjarService

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  private val todoServer = new TodoServer

  private val webjarRoutes: HttpRoutes[IO] = webjarService(
    Config(
      blocker = Blocker.liftExecutorService(Executors.newFixedThreadPool(4))
    )
  )
  private val documentationRoutes = HttpRoutes.of[IO] {
    case GET -> Root =>
      MovedPermanently(Location(uri"/swagger-ui/3.31.1/index.html?url=/openapi.json"))
    case GET -> Root / "openapi.json" =>
      Ok(TodoDocumentation.apiJson, `Content-Type`(MediaType.application.json))
  }
  private val httpApp = Router(
    "/" -> todoServer.routes,
    "/" -> webjarRoutes,
    "/" -> documentationRoutes
  ).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
