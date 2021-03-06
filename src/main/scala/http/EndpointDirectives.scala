package http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directives, MalformedRequestContentRejection, RejectionHandler, Route}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import model.ActionEnum.{DOWNLOAD, UPLOAD}
import model.StatusEnum.ERROR
import model._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait EndpointDirectives extends LazyLogging with FailFastCirceSupport with AutoDerivation with Directives {

  def responseFromFuture(future: Future[Either[SchemaValidatorError, SchemaValidatorResponse]]): Route =
    onComplete(future) {
      case Success(Right(value)) =>
        complete(StatusCodes.OK, value.asJson.deepDropNullValues)
      case Success(Left(error))  =>
        responseFromError(error)
      case Failure(exception)    =>
        logger.error(exception.getLocalizedMessage)
        complete(StatusCodes.InternalServerError, exception.getLocalizedMessage)
    }

  private def responseFromError(error: SchemaValidatorError): Route = error match {
    case e @ DuplicatedKeyError(id) =>
      complete(
        StatusCodes.BadRequest,
        SchemaValidatorResponse(UPLOAD, Some(id), ERROR, message = Some(e.msg)).asJson.deepDropNullValues
      )

    case e @ SchemaNotFoundError(id) =>
      complete(
        StatusCodes.BadRequest,
        SchemaValidatorResponse(DOWNLOAD, Some(id), ERROR, message = Some(e.msg)).asJson.deepDropNullValues
      )

    case _ => complete(StatusCodes.InternalServerError, error.msg)

  }

}

object EndpointDirectives extends FailFastCirceSupport {

  def contentMalFormedHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[MalformedRequestContentRejection] { rejections =>
        complete(
          StatusCodes.BadRequest,
          SchemaValidatorResponse(
            ActionEnum.VALIDATE_DOCUMENT,
            None,
            StatusEnum.ERROR,
            message = Some("Invalid JSON")
          ).asJson.deepDropNullValues
        )
      }.result()

}
