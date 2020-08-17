package todobackend

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent.WebjarService.Config
import org.http4s.server.staticcontent.webjarService
import org.http4s.{HttpRoutes, MediaType}

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  private val todoServer = new TodoServer

  private val todoRoutes = CORS(
    todoServer.routes
  )
  private val webjarRoutes: HttpRoutes[IO] = webjarService(
    Config(Blocker.liftExecutorService(Executors.newFixedThreadPool(4)))
  )
  private val customRoutes = HttpRoutes.of[IO] {
    case OPTIONS -> _ =>
      Ok()
    case GET -> Root =>
      MovedPermanently(Location(uri"/swagger-ui/3.31.1/index.html?url=/openapi.json"))
    case GET -> Root / "openapi.json" =>
      Ok(TodoDocumentation.apiJson, `Content-Type`(MediaType.application.json))
  }
  private val httpApp = (todoRoutes <+> webjarRoutes <+> customRoutes).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(httpPort, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

  val httpPort: Int = sys.env.get("PORT").map(Integer.parseInt).getOrElse(8080)

}
