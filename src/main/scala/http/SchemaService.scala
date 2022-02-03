package http

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import model.ActionEnum.{DOWNLOAD, UPLOAD}
import model.StatusEnum.SUCCESS
import model._
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SchemaService[F[_]] {

  def upload(schema: String, schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

  def download(schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

}

case class SchemaServiceImpl()(implicit db: DatabaseDef, ec: ExecutionContext)
    extends SchemaService[Future]
    with LazyLogging {

  val jsonMapper: ObjectMapper = new ObjectMapper()

  override def upload(
      schema: String,
      schemaId: String
  ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val insertQuery = sqlu"INSERT INTO schemas(id,schema) VALUES($schemaId,$schema)"
    val selectQuery = sql"SELECT id FROM schemas WHERE id=$schemaId".as[String]

    val process = for {
      _      <- DBIO.successful(JsonUtils.readJson(schema))
      result <- selectQuery
    } yield {
      if (result.isEmpty) {
        insertQuery.asTry.map {
          case Success(_) =>
            Right(SchemaValidatorResponse(UPLOAD, Some(schemaId), SUCCESS))

          case Failure(ex) =>
            logger.error(s"unexpected error while uploading a schema: ${ex.getMessage}")
            Left(UnexpectedError(ex.getMessage))
        }
      } else DBIO.successful(Left(DuplicatedKeyError(schemaId)))
    }

    db.run(process.flatten.transactionally)
  }

  override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val selectQuery = sql"SELECT schema FROM schemas WHERE id=$schemaId".as[String]

    val result = selectQuery.asTry.map {
      case Success(value) if value.isEmpty =>
        Left(SchemaNotFoundError(schemaId))

      case Success(value) if value.nonEmpty =>
        Right(SchemaValidatorResponse(DOWNLOAD, Some(schemaId), SUCCESS, data = value.headOption))

      case Failure(exception) =>
        logger.error(s"unexpected error while retrieving schema: ${exception.getMessage}")
        Left(UnexpectedError(exception.getMessage))
    }

    db.run(result)
  }

}
