package http

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait Directives extends LazyLogging with FailFastCirceSupport {

  def responseFromFuture[T](future: Future[Either[String, T]])(implicit marshaller: ToEntityMarshaller[T]): Route =
    onComplete(future) {
      case Success(Right(value)) =>
        complete(StatusCodes.OK, value)
      case Success(Left(error))  =>
        complete(StatusCodes.BadRequest, error)
      case Failure(exception)    =>
        logger.error(exception.getLocalizedMessage)
        complete(StatusCodes.InternalServerError, exception.getLocalizedMessage)
    }

}
