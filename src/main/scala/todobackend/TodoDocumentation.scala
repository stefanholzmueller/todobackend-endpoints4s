package todobackend

import cats.effect.IO
import endpoints4s.openapi.model.{Info, OpenApi}

object TodoDocumentation
  extends TodoEndpoints
    with endpoints4s.openapi.Endpoints
    with endpoints4s.openapi.JsonEntitiesFromSchemas {

  val api: OpenApi =
    openApi(
      Info(title = "Todo-Backend API", version = "1.0.0")
    )(getTodos, postTodo)

  val apiJson: String = OpenApi.stringEncoder.encode(api)

}
