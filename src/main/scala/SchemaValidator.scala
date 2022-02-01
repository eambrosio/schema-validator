import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import http.{SchemaServiceImpl, SchemaValidatorEndpoint, ValidatorServiceImpl}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object SchemaValidator extends App with LazyLogging {

  implicit val database: JdbcBackend.Database = Database.forConfig("postgres")
  implicit val as: ActorSystem                = ActorSystem("SchemaValidatorServer")
  val schemaService: SchemaServiceImpl        = SchemaServiceImpl()
  val validatorService: ValidatorServiceImpl  = ValidatorServiceImpl()
  val endpoint: SchemaValidatorEndpoint       = SchemaValidatorEndpoint(schemaService, validatorService)

  val futureBinding = Http().newServerAt("0.0.0.0", 8080).bind(endpoint.routes)

  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex)      =>
      logger.error("Failed to bind HTTP endpoint, terminating system", ex)
      as.terminate()
      as.registerOnTermination(() => database.close())
  }
}
