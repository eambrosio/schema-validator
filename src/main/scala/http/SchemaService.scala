package http

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.typesafe.scalalogging.LazyLogging
import model.ActionEnum.VALIDATE_DOCUMENT
import model.{
  ActionEnum,
  DuplicatedKeyError,
  JsonReaderError,
  JsonSchemaReaderError,
  SchemaNotFoundError,
  SchemaValidatorError,
  SchemaValidatorResponse,
  StatusEnum,
  UnexpectedError,
  ValidateJsonError
}
import model.StatusEnum.{ERROR, SUCCESS}
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait SchemaService[F[_]] {

  def upload(schema: String, schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

  def download(schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

}

case class SchemaServiceImpl()(implicit db: DatabaseDef, ec: ExecutionContext)
    extends SchemaService[Future]
    with LazyLogging {

  override def upload(
      schema: String,
      schemaId: String
  ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val insertQuery = sqlu"INSERT INTO schemas(id,schema) VALUES($schemaId,$schema)"
    val selectQuery = sql"SELECT id FROM schemas WHERE id=$schemaId".as[String]

    val process = selectQuery
      .flatMap(result =>
        if (result.isEmpty) {
          insertQuery.map(_ => Right(SchemaValidatorResponse(ActionEnum.UPLOAD, schemaId, StatusEnum.SUCCESS)))
        } else DBIO.successful(Left(DuplicatedKeyError(schemaId)))
      )

    db.run(process.transactionally)
  }

  override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val selectQuery = sql"SELECT schema FROM schemas WHERE id=$schemaId".as[String]

    val result = selectQuery.asTry.map {
      case Success(value) if value.isEmpty =>
        Left(SchemaNotFoundError(schemaId))

      case Success(value) if value.nonEmpty =>
        Right(SchemaValidatorResponse(ActionEnum.DOWNLOAD, schemaId, StatusEnum.SUCCESS, document = value.headOption))

      case Failure(exception) =>
        logger.error(s"unexpected error while retrieving schema: ${exception.getMessage}")
        Left(UnexpectedError(exception.getMessage))
    }

    db.run(result)
  }

}
