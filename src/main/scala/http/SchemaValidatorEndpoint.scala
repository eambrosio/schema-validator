package http

import akka.http.scaladsl.server.Route
import io.circe.JsonObject
import io.circe.parser.decode
import scala.concurrent.Future
import io.circe.syntax._

case class SchemaValidatorEndpoint(schemaService: SchemaService[Future]) extends EndpointDirectives {

  val routes: Route = validateSchema

//  def downloadSchema: Route =
//    path("schema" / Segment) { schemaId =>
//      get {
//        responseFromFuture(schemaService.download(schemaId))
//      }
//    }
//
//  def uploadSchema: Route =
//    path("schema" / Segment) { schemaId =>
//      post {
//        entity(as[String]) { schema =>
//          responseFromFuture(schemaService.upload(schema, schemaId))
//        }
//      }
//    }

  def validateSchema: Route =
    path("validate" / Segment) { schemaId =>
      post {
        entity(as[JsonObject]) { document =>
          responseFromFuture(schemaService.validate(document.asJson.toString(), schemaId))
        }
      }
    }

}
