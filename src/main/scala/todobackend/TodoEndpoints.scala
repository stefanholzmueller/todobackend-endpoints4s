package todobackend

import endpoints4s.algebra.Tag


trait TodoEndpoints
  extends endpoints4s.algebra.Endpoints
    with endpoints4s.algebra.JsonEntitiesFromSchemas
    with endpoints4s.generic.JsonSchemas {

  private val basePath = path / "todos"
  private val baseDocs = EndpointDocs().withTags(List(Tag("Todo")))

  val getTodos = endpoint(
    request = get(basePath),
    response = ok(jsonResponse[Seq[Todo]], docs = Some("The full list of todos")),
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

  // lazy val titleJsonField: Record[String] = field[String]("title", Some("Description of what to do"))

  implicit lazy val todoJsonSchema: JsonSchema[Todo] = genericJsonSchema
  implicit lazy val newTodoJsonSchema: JsonSchema[NewTodo] = genericJsonSchema

}
