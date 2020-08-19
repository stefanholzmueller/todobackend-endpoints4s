package todobackend

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent.WebjarService.Config
import org.http4s.server.staticcontent.webjarService
import org.http4s.{HttpRoutes, MediaType}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  val httpPort: Int = sys.env.get("PORT").map(Integer.parseInt).getOrElse(8080)
  val jdbcUrl: String = sys.env.getOrElse("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/todobackend?user=postgres&password=pass")
  /*
    CREATE TABLE todo (
      id uuid NOT NULL,
      title varchar NOT NULL,
      "order" int4 NOT NULL,
      completed bool NOT NULL,
      CONSTRAINT todo_pk PRIMARY KEY (id)
    );
   */


  def transactor(connectEC: ExecutionContext, blocker: Blocker): Resource[IO, Transactor[IO]] = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName("org.postgresql.Driver")
    hikariConfig.setJdbcUrl(jdbcUrl)

    HikariTransactor.fromHikariConfig(hikariConfig, connectEC, blocker)
  }

  def routing(todoServer: TodoServer): HttpRoutes[IO] = {
    val todoRoutes: HttpRoutes[IO] = CORS(
      todoServer.routes
    )
    val webjarRoutes: HttpRoutes[IO] = webjarService(
      Config(Blocker.liftExecutorService(Executors.newFixedThreadPool(4)))
    )
    val customRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case OPTIONS -> _ =>
        Ok()
      case GET -> Root =>
        MovedPermanently(Location(uri"/swagger-ui/3.31.1/index.html?url=/openapi.json"))
      case GET -> Root / "openapi.json" =>
        Ok(TodoDocumentation.apiJson, `Content-Type`(MediaType.application.json))
    }

    todoRoutes <+> webjarRoutes <+> customRoutes
  }

  def blaze(routes: HttpRoutes[IO]): Resource[IO, Server[IO]] =
    BlazeServerBuilder[IO](global)
      .bindHttp(httpPort, "0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .resource

  def resource: Resource[IO, Server[IO]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[IO](32)
      blocker <- Blocker[IO]
      xa <- transactor(connectEC, blocker)
      server = new TodoServer(xa, QuillTodoRepository)
      routes = routing(server)
      blaze <- blaze(routes)
    } yield blaze

  def run(args: List[String]): IO[ExitCode] =
    resource.use(_ => IO.never.as(ExitCode.Success))

}
