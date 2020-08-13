package todobackend

import java.util.UUID


trait TodoEndpoints
  extends endpoints4s.algebra.Endpoints
    with endpoints4s.algebra.JsonEntitiesFromSchemas {

  val getTodos = endpoint(get(path / "todos"), todosResponse)

  lazy val todosResponse =
    ok(jsonResponse[Seq[String]], docs = Some("The full list of todos"))

  // implicit lazy val jsonSchemaTodo: JsonSchema[Todo] = genericJsonSchema
  implicit lazy val jsonSchemaTodo: JsonSchema[Todo] = (
    field[UUID]("id") zip
      field[String]("title", Some("Description of what to do"))
    ).xmap((Todo.apply _).tupled)(Todo.unapply(_).get)

}
