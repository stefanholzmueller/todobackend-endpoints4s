package todobackend

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import doobie.quill.DoobieContext
import doobie.{ConnectionIO, LogHandler, Transactor}
import io.getquill.{idiom => _, _}
import doobie.h2.implicits._ // needed for SQL parameters

trait TodoRepository {
  def selectTodos: IO[Seq[Todo]]
  def deleteTodos: IO[Unit]
  def insertTodo(todo: Todo): IO[Todo]
  def selectTodo(id: UUID): ConnectionIO[Todo]
  def deleteTodo(id: UUID): IO[Unit]
  def changeTitle(id: UUID, title: String): ConnectionIO[Unit]
}

class DoobieTodoRepository(xa: Transactor[IO]) extends TodoRepository {

  implicit val logHandler: LogHandler = LogHandler.jdkLogHandler

  override def selectTodo(id: UUID): ConnectionIO[Todo] = {
    sql"""SELECT "id", "title", "completed", "order" FROM "Todo" WHERE "id" = $id"""
      .query[Todo]
      .unique
  }

  override def selectTodos: IO[Seq[Todo]] = {
    sql"""SELECT "id", "title", "completed", "order" FROM "Todo" ORDER BY "order", "title""""
      .query[Todo]
      .to[List]
      .transact(xa)
  }

  override def insertTodo(todo: Todo): IO[Todo] = {
    val id = todo.id
    val title = todo.title
    val completed = todo.completed
    val order = todo.order
    sql"""INSERT INTO "Todo" ("id", "title", "completed", "order") values ($id, $title, $completed, $order)"""
      .update
      .run
      .transact(xa)
      .map(_ => todo)
  }

  override def changeTitle(id: UUID, title: String): ConnectionIO[Unit] = {
    sql"""UPDATE "Todo" SET "title" = $title WHERE "id" = $id"""
      .update
      .run
      .map(_ => {})
  }

  override def deleteTodos: IO[Unit] =
    sql"""DELETE FROM "Todo""""
      .update
      .run
      .transact(xa)
      .map(_ => {})

  override def deleteTodo(id: UUID): IO[Unit] =
    sql"""DELETE FROM "Todo" WHERE "id" = $id"""
      .update
      .run
      .transact(xa)
      .map(_ => {})

}

class QuillTodoRepository(xa: Transactor[IO]) extends TodoRepository {

  val dc = new DoobieContext.H2(Escape) // Escape because 'order' is a reserved word
  import dc._

  override def selectTodos: IO[Seq[Todo]] = {
    val q = quote {
      query[Todo].sortBy(todo => (todo.order, todo.title))
    }
    run(q).transact(xa)
  }

  override def selectTodo(id: UUID): ConnectionIO[Todo] = {
    run(query[Todo].filter(_.id == lift(id))).map(_.head)
  }

  override def deleteTodos: IO[Unit] = {
    val q = quote {
      query[Todo].delete
    }
    run(q).transact(xa).map(_ => {})
  }

  override def deleteTodo(id: UUID): IO[Unit] = {
    val q = quote {
      query[Todo].filter(_.id == lift(id)).delete
    }
    run(q).transact(xa).map(_ => {})
  }

  override def insertTodo(todo: Todo): IO[Todo] = {
    val q = quote {
      query[Todo].insert(lift(todo))
    }
    run(q).transact(xa).map(_ => todo)
  }

  override def changeTitle(id: UUID, title: String): ConnectionIO[Unit] = {
    run(query[Todo].filter(_.id == lift(id)).update(_.title -> lift(title))).map(_ => {})
  }

}
