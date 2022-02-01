package http

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import cats.data.EitherT
import io.circe.JsonObject
import io.circe.parser.decode

import scala.concurrent.{ExecutionContext, Future}
import io.circe.syntax._

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

  def validateSchema: Route =
    path("validate" / Segment) { schemaId =>
      post {

        entity(as[JsonObject]) { document =>
//          responseFromFuture(schemaService.validate(document.asJson.toString(), schemaId))
          responseFromFuture((for {
            schema   <- EitherT(schemaService.download(schemaId))
            response <- EitherT(validatorService.validate(document.asJson.toString(), schemaId, schema.document.get))
          } yield response).value)
        }
      }
    }

}
