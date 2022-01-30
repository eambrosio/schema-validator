package http

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import model.{SchemaError, SchemaValidatorResponse}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait EndpointDirectives extends LazyLogging with FailFastCirceSupport with AutoDerivation with Directives {

  def responseFromFuture(future: Future[Either[SchemaError, SchemaValidatorResponse]]): Route =
    onComplete(future) {
      case Success(Right(value)) =>
        complete(StatusCodes.OK, value.asJson.deepDropNullValues)
      case Success(Left(error))  =>
        responseFromError(error)
      case Failure(exception)    =>
        logger.error(exception.getLocalizedMessage)
        complete(StatusCodes.InternalServerError, exception.getLocalizedMessage)
    }

  private def responseFromError[T](error: SchemaError): Route =
    complete(StatusCodes.BadRequest, error)

}