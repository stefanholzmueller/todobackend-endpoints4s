package todobackend

import java.util.UUID

import endpoints4s.algebra.Tag



trait TodoEndpoints
  extends endpoints4s.algebra.Endpoints
    with endpoints4s.algebra.JsonEntitiesFromSchemas {

  private val basePath = path / "todos"
  private val baseDocs = EndpointDocs().withTags(List(Tag("Todo")))

  val getTodos = endpoint(
    request = get(basePath),
    response = todosResponse,
    docs = baseDocs.withSummary(Some("Lists all todos")).withDescription(Some("Blah"))
  )

  lazy val todosResponse =
    ok(jsonResponse[Seq[String]], docs = Some("The full list of todos"))

  // implicit lazy val jsonSchemaTodo: JsonSchema[Todo] = genericJsonSchema
  implicit lazy val jsonSchemaTodo: JsonSchema[Todo] = (
    field[UUID]("id") zip
      field[String]("title", Some("Description of what to do"))
    ).xmap((Todo.apply _).tupled)(Todo.unapply(_).get)

}
