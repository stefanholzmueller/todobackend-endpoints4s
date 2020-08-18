package todobackend

import java.util.UUID

import doobie.implicits._
import doobie.quill.DoobieContext
import doobie.{ConnectionIO, LogHandler}
import io.getquill.{idiom => _, _}
import doobie.h2.implicits._ // needed for SQL parameters

trait TodoRepository {
  def selectTodos: ConnectionIO[List[Todo]]
  def deleteTodos: ConnectionIO[Unit]
  def insertTodo(todo: Todo): ConnectionIO[Todo]
  def selectTodo(id: UUID): ConnectionIO[Todo]
  def deleteTodo(id: UUID): ConnectionIO[Unit]
  def changeTitle(id: UUID, title: String): ConnectionIO[Unit]
  def changeOrder(id: UUID, order: Int): ConnectionIO[Unit]
  def changeCompleted(id: UUID, completed: Boolean): ConnectionIO[Unit]
}

object DoobieTodoRepository extends TodoRepository {

  implicit val logHandler: LogHandler = LogHandler.jdkLogHandler

  override def selectTodo(id: UUID): ConnectionIO[Todo] =
    sql"""SELECT "id", "title", "completed", "order" FROM "Todo" WHERE "id" = $id"""
      .query[Todo]
      .unique

  override def selectTodos: ConnectionIO[List[Todo]] =
    sql"""SELECT "id", "title", "completed", "order" FROM "Todo" ORDER BY "order", "title""""
      .query[Todo]
      .to[List]

  override def insertTodo(todo: Todo): ConnectionIO[Todo] = {
    val id = todo.id
    val title = todo.title
    val completed = todo.completed
    val order = todo.order
    sql"""INSERT INTO "Todo" ("id", "title", "completed", "order") values ($id, $title, $completed, $order)"""
      .update
      .run
      .map(_ => todo)
  }

  override def deleteTodos: ConnectionIO[Unit] =
    sql"""DELETE FROM "Todo""""
      .update
      .run
      .map(_ => {})

  override def deleteTodo(id: UUID): ConnectionIO[Unit] =
    sql"""DELETE FROM "Todo" WHERE "id" = $id"""
      .update
      .run
      .map(_ => {})

  override def changeTitle(id: UUID, title: String): ConnectionIO[Unit] =
    sql"""UPDATE "Todo" SET "title" = $title WHERE "id" = $id"""
      .update
      .run
      .map(_ => {})

  override def changeOrder(id: UUID, order: Int): ConnectionIO[Unit] =
    sql"""UPDATE "Todo" SET "order" = $order WHERE "id" = $id"""
      .update
      .run
      .map(_ => {})

  override def changeCompleted(id: UUID, completed: Boolean): ConnectionIO[Unit] =
    sql"""UPDATE "Todo" SET "completed" = $completed WHERE "id" = $id"""
      .update
      .run
      .map(_ => {})

}

object QuillTodoRepository extends TodoRepository {

  val dc = new DoobieContext.H2(Escape) // Escape because 'order' is a reserved word
  import dc._

  override def selectTodos: ConnectionIO[List[Todo]] =
    run(query[Todo].sortBy(todo => (todo.order, todo.title)))

  override def selectTodo(id: UUID): ConnectionIO[Todo] =
    run(query[Todo].filter(_.id == lift(id))).map(_.head)

  override def deleteTodos: ConnectionIO[Unit] =
    run(query[Todo].delete).map(_ => {})

  override def deleteTodo(id: UUID): ConnectionIO[Unit] =
    run(query[Todo].filter(_.id == lift(id)).delete).map(_ => {})

  override def insertTodo(todo: Todo): ConnectionIO[Todo] =
    run(query[Todo].insert(lift(todo))).map(_ => todo)

  override def changeTitle(id: UUID, title: String): ConnectionIO[Unit] =
    run(query[Todo].filter(_.id == lift(id)).update(_.title -> lift(title))).map(_ => {})

  override def changeOrder(id: UUID, order: Int): ConnectionIO[Unit] =
    run(query[Todo].filter(_.id == lift(id)).update(_.order -> lift(order))).map(_ => {})

  override def changeCompleted(id: UUID, completed: Boolean): ConnectionIO[Unit] =
    run(query[Todo].filter(_.id == lift(id)).update(_.completed -> lift(completed))).map(_ => {})

}
