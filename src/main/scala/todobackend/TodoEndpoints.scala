package todobackend

import java.util.UUID

import endpoints4s.algebra.Tag


trait TodoEndpoints
  extends endpoints4s.algebra.Endpoints
    with endpoints4s.algebra.JsonEntitiesFromSchemas {

  private val basePath = path / "todos"
  private val deepPath = basePath / segment[UUID]("id")
  private val baseDocs = EndpointDocs().withTags(List(Tag("Todo")))

  val getTodos = endpoint(
    request = get(basePath),
    response = ok(jsonResponse[List[Todo]], docs = Some("The full list of todos")),
    docs = baseDocs
      .withSummary(Some("Lists all Todos"))
      .withDescription(Some("The returned Todos are ordered by 'order' and then 'title'"))
  )

  val postTodo = endpoint(
    request = post(basePath, jsonRequest[NewTodo]),
    response = response(Created, jsonResponse[Todo]),
    docs = baseDocs.withSummary(Some("Creates a new, uncompleted Todo"))
  )

  val deleteTodos = endpoint(
    request = delete(basePath),
    response = ok(emptyResponse),
    docs = baseDocs.withSummary(Some("Deletes all Todos"))
  )

  val getTodo = endpoint(
    request = get(deepPath),
    response = ok(jsonResponse[Todo]),
    docs = baseDocs.withSummary(Some("Shows a single Todo"))
  )

  val deleteTodo = endpoint(
    request = delete(deepPath),
    response = ok(emptyResponse),
    docs = baseDocs.withSummary(Some("Deletes this Todo"))
  )

  val patchTodo = endpoint(
    request = patch(deepPath, jsonRequest[EditTodo]),
    response = ok(jsonResponse[Todo]),
    docs = baseDocs.withSummary(Some("Modifies this Todo"))
  )

  lazy val titleJsonField: Record[String] = field[String]("title", Some("Description of what to do"))

  implicit lazy val todoJsonSchema: JsonSchema[Todo] = (
    titleJsonField zip
      field[Boolean]("completed") zip
      field[Int]("order") zip
      field[String]("url")
    ).xmap(notImplemented)(todo => (
    todo.title,
    todo.completed,
    todo.order,
    "https://todobackend-endpoints4s.herokuapp.com/todos/" + todo.id
  ))

  implicit lazy val newTodoJsonSchema: JsonSchema[NewTodo] = (
    titleJsonField zip
      optField[Int]("order")
    ).xmap((NewTodo.apply _).tupled)(notImplemented)

  implicit lazy val editTodoJsonSchema: JsonSchema[EditTodo] = (
    optField[String]("title") zip
      optField[Boolean]("completed") zip
      optField[Int]("order")
    ).xmap((EditTodo.apply _).tupled)(notImplemented)

  private def notImplemented[A, B]: A => B = _ => ???

}
