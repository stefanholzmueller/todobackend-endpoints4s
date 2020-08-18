package todobackend

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import doobie.quill.DoobieContext
import doobie.{LogHandler, Transactor}
import io.getquill.{idiom => _, _}
import doobie.h2.implicits._

trait TodoRespository {
  def selectTodos: IO[Seq[Todo]]
  def selectTodo(id: UUID): IO[Todo]
  def deleteTodos: IO[Unit]
  def insertTodo(todo: Todo): IO[Todo]
  def deleteTodo(id: UUID): IO[Unit]
}

class DoobieTodoRepository(xa: Transactor[IO]) extends TodoRespository {

  implicit val logHandler = LogHandler.jdkLogHandler

  override def selectTodo(id: UUID): IO[Todo] = {
    sql"""SELECT id, title, completed, "order" FROM todo WHERE id = $id"""
      .query[Todo]
      .unique
      .transact(xa)
  }

  override def selectTodos: IO[Seq[Todo]] = {
    sql"""SELECT id, title, completed, "order" FROM todo ORDER BY "order", title"""
      .query[Todo]
      .to[List]
      .transact(xa)
  }

  override def insertTodo(todo: Todo): IO[Todo] = {
    val id = todo.id
    val title = todo.title
    val completed = todo.completed
    val order = todo.order
    sql"""INSERT INTO todo (id, title, completed, "order") values ($id, $title, $completed, $order)"""
      .update
      .run
      .transact(xa)
      .map(_ => todo)

  }

  override def deleteTodos: IO[Unit] =
    sql"""DELETE FROM todo"""
      .update
      .run
      .transact(xa)
      .map(_ => {})

  override def deleteTodo(id: UUID): IO[Unit] =
    sql"""DELETE FROM todo WHERE id = $id"""
      .update
      .run
      .transact(xa)
      .map(_ => {})

}

class QuillTodoRepository(xa: Transactor[IO]) extends TodoRespository {

  val dc = new DoobieContext.H2(Literal)
  import dc._

  override def selectTodos: IO[Seq[Todo]] = {
    val q = quote {
      query[Todo].sortBy(todo => (todo.order, todo.title))
    }
    run(q).transact(xa)
  }

  override def selectTodo(id: UUID): IO[Todo] = {
    val q = quote {
      query[Todo].filter(_.id == lift(id))
    }
    run(q).map(_.head).transact(xa)
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

}
