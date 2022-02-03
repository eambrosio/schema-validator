package http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler, Route}
import cats.data.EitherT
import io.circe.JsonObject
import io.circe.syntax._
import model.{ActionEnum, SchemaValidatorResponse, StatusEnum}

import scala.concurrent.{ExecutionContext, Future}

case class SchemaValidatorEndpoint(schemaService: SchemaService[Future], validatorService: ValidatorService[Future])(
    implicit ec: ExecutionContext
) extends EndpointDirectives {

  val routes: Route = validateSchema ~ downloadSchema ~ uploadSchema

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

  def contentMalFormedHandler(id: String): RejectionHandler = RejectionHandler.newBuilder().handle {
    case MalformedRequestContentRejection(_, _) =>
      complete(
        StatusCodes.BadRequest,
        SchemaValidatorResponse(ActionEnum.VALIDATE_DOCUMENT, None, StatusEnum.ERROR, message = Some("Invalid JSON"))
      )
  }.result()

  def validateSchema: Route =
    path("validate" / Segment) { schemaId =>
      post {
        entity(as[JsonObject]) { document =>
          handleRejections(contentMalFormedHandler(schemaId)) {
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
    }

}
