package http

import akka.http.scaladsl.server.Route
import cats.data.EitherT
import io.circe.JsonObject
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

case class SchemaValidatorEndpoint(schemaService: SchemaService[Future], validatorService: ValidatorService[Future])(
    implicit ec: ExecutionContext
) extends EndpointDirectives {

  val routes: Route = validateSchema ~ downloadSchema ~ uploadSchema ~ listSchemas

  def downloadSchema: Route =
    path("schema" / Segment) { schemaId =>
      get {
        responseFromFuture(schemaService.download(schemaId))
      }
    }

  def uploadSchema: Route =
    path("schema" / Segment) { schemaId =>
      post {
        entity(as[JsonObject]) { schema =>
          responseFromFuture(schemaService.upload(schema.asJson.toString(), schemaId))
        }
      }
    }

  def validateSchema: Route =
    path("validate" / Segment) { schemaId =>
      post {

        entity(as[JsonObject]) { document =>
          responseFromFuture((for {
            schema   <- EitherT(schemaService.download(schemaId))
            response <- EitherT(validatorService.validate(
                          document.asJson.deepDropNullValues.toString(),
                          schemaId,
                          schema.data.get
                        ))
          } yield response).value)
        }
      }
    }

  def listSchemas: Route =
    path("schema") {
      get {
        responseFromFuture(schemaService.list)
      }
    }

}
